package lu.kolja.expandedae.xmod.advancedae;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.upgrades.Upgrades;
import lu.kolja.expandedae.mixin.compat.advancedae.AAEAccessorAdvCraftingCPULogic;
import lu.kolja.expandedae.mixin.compat.advancedae.AAEAccessorExecutingCraftingJob;
import net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPU;
import net.pedroksl.advanced_ae.common.definitions.AAEBlocks;
import net.pedroksl.advanced_ae.common.definitions.AAEItems;

import static lu.kolja.expandedae.definition.ExpItems.AUTO_COMPLETE_CARD;

public class AdvancedAE {
    public AdvancedAE() {
        Upgrades.add(AUTO_COMPLETE_CARD, AAEItems.SMALL_ADV_PATTERN_PROVIDER, 1, "group.adv_pattern_provider.name");
        Upgrades.add(AUTO_COMPLETE_CARD, AAEItems.ADV_PATTERN_PROVIDER, 1, "group.advanced_pattern_provider.name");

        Upgrades.add(AUTO_COMPLETE_CARD, AAEBlocks.SMALL_ADV_PATTERN_PROVIDER, 1, "group.adv_pattern_provider.name");
        Upgrades.add(AUTO_COMPLETE_CARD, AAEBlocks.ADV_PATTERN_PROVIDER, 1, "group.advanced_pattern_provider.name");
    }

    public static void handleCpu(ICraftingCPU cpu, IPatternDetails details) {
        if (cpu instanceof AdvCraftingCPU advCpu) {
            var task = ((AAEAccessorExecutingCraftingJob) ((AAEAccessorAdvCraftingCPULogic) advCpu.craftingLogic).getJob()).getTasks().get(details);
            if (task != null && task.getValue() <= 1) {
                advCpu.cancelJob();
            }
        }
    }
}
