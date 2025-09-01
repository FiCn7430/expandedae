package lu.kolja.expandedae.block.entity;

import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ExpandedMatrixPatternBlockEntity extends TileAssemblerMatrixPattern {
    public ExpandedMatrixPatternBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(pos, blockState);
    }
}
