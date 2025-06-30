package lu.kolja.expandedae.mixin.accessor;

import appeng.api.storage.cells.CellState;
import appeng.blockentity.storage.DriveBlockEntity;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = DriveBlockEntity.class, remap = false)
public interface AccessorDriveBlockEntity {
    @Invoker("updateClientSideState")
    boolean invokeUpdateClientSideState();

    @Accessor("clientSideCellState")
    CellState[] getClientSideCellState();
    
    @Accessor("clientSideOnline")
    void setClientSideOnline(boolean clientSideOnline);

    @Accessor("clientSideOnline")
    boolean getClientSideOnline();

    @Accessor("clientSideCellItems")
    Item[] getClientSideCellItems();
}
