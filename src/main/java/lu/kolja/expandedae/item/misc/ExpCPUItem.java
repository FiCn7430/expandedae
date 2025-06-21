package lu.kolja.expandedae.item.misc;

import appeng.block.crafting.CraftingBlockItem;
import appeng.util.InteractionUtil;
import lu.kolja.expandedae.definition.ExpBlocks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class ExpCPUItem extends CraftingBlockItem {
    public ExpCPUItem(Block id, Properties props) {
        super(id, props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            int count = player.getItemInHand(hand).getCount();
            player.setItemInHand(hand, ItemStack.EMPTY);

            player.getInventory().placeItemBackInInventory(ExpBlocks.EXP_PATTERN_PROVIDER.stack(count));
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
        }
        return super.use(level, player, hand);
    }
}