package lu.kolja.expandedae.block.block;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import lu.kolja.expandedae.block.ExpBlockBaseScreen;
import lu.kolja.expandedae.block.entity.ExpIOPortBlockEntity;
import lu.kolja.expandedae.definition.ExpMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class ExpIOPortBlock extends ExpBlockBaseScreen<ExpIOPortBlockEntity> {
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public ExpIOPortBlock() {
        super(metalProps());
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false));
    }

    @Override
    public void openScreen(ExpIOPortBlockEntity be, Player player) {
        MenuOpener.open(ExpMenus.EXP_IO_PORT.get(), player, MenuLocators.forBlockEntity(be));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        ExpIOPortBlockEntity be = this.getBlockEntity(level, pos);
        if (be != null) be.updateRedstoneState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.full();
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, ExpIOPortBlockEntity be) {
        return currentState.setValue(POWERED, be.isActive());
    }
}
