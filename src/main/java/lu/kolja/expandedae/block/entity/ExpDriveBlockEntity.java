package lu.kolja.expandedae.block.entity;

import appeng.api.storage.cells.CellState;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.core.AELog;
import appeng.core.definitions.AEBlocks;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import lu.kolja.expandedae.definition.ExpMenus;
import lu.kolja.expandedae.menu.ExpDriveMenu;
import lu.kolja.expandedae.mixin.accessor.AccessorDriveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ExpDriveBlockEntity extends DriveBlockEntity implements IUpgradeableObject {
    public ExpDriveBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public int getCellCount() {
        return 40;
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        var be = (AccessorDriveBlockEntity) this;
        be.invokeUpdateClientSideState();

        // Pack the enums into an int of 3 bit per cell state, using 30 bits total
        long packedState = 0;
        for (int i = 0; i < getCellCount(); i++) {
            packedState |= (long) be.getClientSideCellState()[i].ordinal() << (i * 3L);
        }
        // Then pack the online state into bit 31
        if (be.getClientSideOnline()) {
            packedState |= 1 << 31;
        }
        data.writeLong(packedState);

        for (int i = 0; i < getCellCount(); i++) {
            data.writeVarLong(BuiltInRegistries.ITEM.getId(getCellItem(i)));
        }
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        var changed = false;
        var be = (AccessorDriveBlockEntity) this;

        var packedState = data.readInt();
        for (int i = 0; i < getCellCount(); i++) {
            var cellStateOrdinal = (packedState >> (i * 3)) & 0b111;
            var cellState = CellState.values()[cellStateOrdinal];
            if (be.getClientSideCellState()[i] != cellState) {
                be.getClientSideCellState()[i] = cellState;
                changed = true;
            }
        }

        var online = (packedState & (1 << 31)) != 0;
        if (be.getClientSideOnline() != online) {
            be.setClientSideOnline(online);
            changed = true;
        }

        for (int i = 0; i < getCellCount(); i++) {
            var itemId = data.readVarInt();
            Item item = itemId == 0 ? null : BuiltInRegistries.ITEM.byId(itemId);
            if (itemId != 0 && item == Items.AIR) {
                AELog.warn("Received unknown item id from server for disk drive %s: %d", this, itemId);
            }
            if (be.getClientSideCellItems()[i] != item) {
                be.getClientSideCellItems()[i] = item;
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public void openMenu(Player player) {
        MenuOpener.open(ExpMenus.EXP_DRIVE.get(), player, MenuLocators.forBlockEntity(this));
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(ExpMenus.EXP_DRIVE.get(), player, MenuLocators.forBlockEntity(this));
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return AEBlocks.DRIVE.stack();
    }
}
