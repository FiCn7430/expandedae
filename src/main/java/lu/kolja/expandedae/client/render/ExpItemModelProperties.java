package lu.kolja.expandedae.client.render;

import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.items.tools.powered.ColorApplicatorItem;
import lu.kolja.expandedae.definition.ExpItems;
import lu.kolja.expandedae.item.misc.InfinityColorApplicatorItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ExpItemModelProperties {
    public static final ResourceLocation COLORED_PREDICATE_ID = AppEng.makeId("colored");

    public static void init() {
        InfinityColorApplicatorItem colorApplicatorItem = ExpItems.INFINITY_COLOR_APPLICATOR.asItem();
        ItemProperties.register(colorApplicatorItem, COLORED_PREDICATE_ID,
                (itemStack, level, entity, seed) -> {
                    // If the stack has no color, don't use the colored model since the impact of
                    // calling getColor for every quad is extremely high, if the stack tries to
                    // re-search its
                    // inventory for a new paintball everytime
                    AEColor col = colorApplicatorItem.getActiveColor(itemStack);
                    return col != null ? 1 : 0;
                });
    }
}
