package lu.kolja.expandedae.block.entity;

import appeng.api.implementations.blockentities.IColorableBlockEntity;
import appeng.api.util.AEColor;
import appeng.blockentity.storage.DriveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ColorableDriveBlockEntity extends DriveBlockEntity implements IColorableBlockEntity {
    private AEColor color = AEColor.TRANSPARENT;

    public ColorableDriveBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        if (data.contains("color")) {
            this.color = AEColor.valueOf(data.getString("color"));
        } else {
            this.color = AEColor.TRANSPARENT;
        }
        this.getMainNode().setGridColor(this.color);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        data.putString("color", this.color.name());
    }

    @Override
    public AEColor getColor() {
        return color;
    }

    @Override
    public boolean recolourBlock(Direction side, AEColor color, Player who) {
        if (this.color == color) return false;
        this.color = color;
        this.saveChanges();
        this.markForUpdate();
        this.getMainNode().setGridColor(color);
        return false;
    }
}
