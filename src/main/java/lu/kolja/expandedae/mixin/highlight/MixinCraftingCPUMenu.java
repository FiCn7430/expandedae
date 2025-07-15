package lu.kolja.expandedae.mixin.highlight;

import appeng.api.networking.IGrid;
import appeng.api.stacks.AEKey;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.menu.me.crafting.CraftingCPUMenu;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = CraftingCPUMenu.class, remap = false)
public abstract class MixinCraftingCPUMenu {
    @Shadow abstract IGrid getGrid();

    @Shadow @Final private IGrid grid;

    @Nullable
    public AEKey test(AEKey key) {
        for (var machine : this.grid.getActiveMachines(PatternProviderBlockEntity.class)) {
            machine.getLogic().getAvailablePatterns().forEach(
                    p -> p.getOutputs()[0].what() == key ? return AeKey.fromStack(p.getOutputs()[0]) : null
            );
            for (var inputs : machine.getLogic().getAvailablePatterns()) {

            }
        }
    }
}
