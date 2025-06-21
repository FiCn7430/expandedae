package lu.kolja.expandedae.item.dummy;

import appeng.items.AEBaseItem;
import lu.kolja.expandedae.xmod.XMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class DummyIntegrationItem extends AEBaseItem {
    private final XMod.ADDONS addon;

    public DummyIntegrationItem(Item.Properties properties, XMod.ADDONS addon) {
        super(properties);
        this.addon = addon;
    }

    @ParametersAreNonnullByDefault
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(addon.getUnavailableTooltip());
    }

    @Override
    public void addToMainCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {}
}
