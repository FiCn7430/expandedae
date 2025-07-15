package lu.kolja.expandedae.mixin.highlight;

import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.me.service.helpers.NetworkCraftingProviders;
import java.util.Map;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = NetworkCraftingProviders.class, remap = false)
public class MixinNetworkCraftingProviders {
    @Shadow
    Map<IGridNode, NetworkCraftingProviders.ProviderState> craftingProviders;

    @Mixin(targets = "appeng.me.service.helpers.NetworkCraftingProviders$ProviderState", remap = false)
    private static class MixinNetworkCraftingProviderss {
        @Shadow @Final private ICraftingProvider provider;

        @Shadow private void mount(NetworkCraftingProviders methods) {
            this.provider
        }

        @Shadow private void unmount(NetworkCraftingProviders methods) {
        }
    }
}
