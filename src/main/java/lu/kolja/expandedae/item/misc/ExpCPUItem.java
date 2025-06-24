package lu.kolja.expandedae.item.misc;

import appeng.block.crafting.CraftingBlockItem;
import appeng.util.InteractionUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class ExpCPUItem extends CraftingBlockItem {
    private final ItemLike component;

    public ExpCPUItem(Block id, Properties props, ItemLike component) {
        super(id, props);
        this.component = component;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            /* TODO: CHANGE WHEN ADDING COMPONENTS
            int count = player.getItemInHand(hand).getCount();
            player.setItemInHand(hand, ItemStack.EMPTY);

            var inv = player.getInventory();
            inv.placeItemBackInInventory(new ItemStack(component, count));
            inv.placeItemBackInInventory(ExpBlocks.UNIT.stack(count));

            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
            */
        }
        return super.use(level, player, hand);
    }
}