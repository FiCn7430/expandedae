package lu.kolja.expandedae.xmod.advancedae;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.upgrades.Upgrades;
import lu.kolja.expandedae.mixin.compat.advancedae.AAEAccessorAdvCraftingCPULogic;
import net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPU;
import net.pedroksl.advanced_ae.common.definitions.AAEBlocks;
import net.pedroksl.advanced_ae.common.definitions.AAEItems;

import static lu.kolja.expandedae.definition.ExpItems.AUTO_COMPLETE_CARD;

public class AdvancedAE {
    public AdvancedAE() {
        Upgrades.add(AUTO_COMPLETE_CARD, AAEItems.SMALL_ADV_PATTERN_PROVIDER, 1, "group.adv_pattern_provider.name");
        Upgrades.add(AUTO_COMPLETE_CARD, AAEItems.ADV_PATTERN_PROVIDER, 1, "group.adv_pattern_provider.name");

        Upgrades.add(AUTO_COMPLETE_CARD, AAEBlocks.SMALL_ADV_PATTERN_PROVIDER, 1, "group.adv_pattern_provider.name");
        Upgrades.add(AUTO_COMPLETE_CARD, AAEBlocks.ADV_PATTERN_PROVIDER, 1, "group.adv_pattern_provider.name");
    }

    public static void handleCpu(ICraftingCPU cpu, IPatternDetails details) {
        if (cpu instanceof AdvCraftingCPU advCpu) {
            var craftingLogic = advCpu.craftingLogic;
            var job = ((AAEAccessorAdvCraftingCPULogic) craftingLogic).getJob();

            // 没有任务，直接返回
            if (job == null) return;

            // 获取 AAE 的 inventory
            var inventory = craftingLogic.getInventory();

            // 检测库存是否为空
            if (!inventory.list.isEmpty()) {
                return;
            }

            // 库存为空，取消任务
            advCpu.cancelJob();
        }
    }
}
