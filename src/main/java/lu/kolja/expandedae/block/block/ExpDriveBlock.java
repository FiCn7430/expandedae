package lu.kolja.expandedae.block.block;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import lu.kolja.expandedae.block.ExpBlockBaseScreen;
import lu.kolja.expandedae.block.entity.ExpDriveBlockEntity;
import net.minecraft.world.entity.player.Player;

public class ExpDriveBlock extends ExpBlockBaseScreen<ExpDriveBlockEntity> {
    @Override
    public void openScreen(ExpDriveBlockEntity be, Player player) {
        be.openMenu(player);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.full();
    }
}
