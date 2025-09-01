package lu.kolja.expandedae.block.block;

import com.glodblock.github.extendedae.common.blocks.matrix.BlockAssemblerMatrixBase;
import lu.kolja.expandedae.block.entity.ExpandedMatrixPatternBlockEntity;
import lu.kolja.expandedae.definition.ExpBlocks;
import net.minecraft.world.item.Item;

public class ExpandedMatrixPatternBlock extends BlockAssemblerMatrixBase<ExpandedMatrixPatternBlockEntity> {
    @Override
    public Item getPresentItem() {
        return ExpBlocks.EXP_MATRIX_PATTERN.asItem();
    }
}
