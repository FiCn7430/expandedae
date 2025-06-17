package lu.kolja.expandedae.cellhandler;

import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import lu.kolja.expandedae.item.cell.ArtUniverseCellItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ArtUniverseCellHandler implements ICellHandler {
    public static final ArtUniverseCellHandler INSTANCE = new ArtUniverseCellHandler();

    @Override
    public boolean isCell(ItemStack is) {
        return is.getItem() instanceof ArtUniverseCellItem;
    }

    @Override
    public @Nullable ArtUniverseCellInventory getCellInventory(ItemStack is, @Nullable ISaveProvider host) {
        return ArtUniverseCellInventory.createInventory(is, host);
    }
}
