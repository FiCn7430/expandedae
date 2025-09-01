package lu.kolja.expandedae.block.block;

import appeng.api.orientation.IOrientationStrategy;
import appeng.block.storage.DriveBlock;
import appeng.blockentity.storage.DriveBlockEntity;
import lu.kolja.expandedae.block.entity.ColorableDriveBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class ColorableDriveBlock extends DriveBlock {
    public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, 16);

    public ColorableDriveBlock() {
        this.registerDefaultState(this.defaultBlockState().setValue(COLOR, 16));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COLOR);
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, DriveBlockEntity be) {
        return currentState.setValue(COLOR, ((ColorableDriveBlockEntity) be).getColor().ordinal());
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return super.getOrientationStrategy();
    }
}
