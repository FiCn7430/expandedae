package lu.kolja.expandedae.mixin.compat.advancedae;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.stacks.KeyCounter;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import lu.kolja.expandedae.definition.ExpItems;
import lu.kolja.expandedae.mixin.accessor.AccessorCraftingCpuLogic;
import lu.kolja.expandedae.mixin.accessor.AccessorExecutingCraftingJob;
import lu.kolja.expandedae.xmod.advancedae.AdvancedAE;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 为 AdvancedAE 的样板供应器添加自动合成卡支持
 *
 * 注意：当 AppFlux 加载时，AdvancedAE 自身会通过 Mixin 为 AdvPatternProviderLogic
 * 添加 IUpgradeableObject 接口和升级槽。
 *
 * 这个 Mixin 负责：
 * 1. 检测自动合成卡是否安装
 * 2. 在 pushPattern 成功后触发自动合成逻辑
 *
 * 阻塞模式功能已移除，保留 AE2 原版的阻塞行为
 */
@Mixin(value = AdvPatternProviderLogic.class, remap = false)
public abstract class MixinAdvPatternProviderLogic {

    @Shadow
    public abstract IGrid getGrid();

    /**
     * 在 pushPattern 成功后检查自动合成卡
     */
    @Inject(
            method = "pushPattern",
            at = @At("RETURN")
    )
    private void expandedae$onPushPatternSuccess(IPatternDetails patternDetails, KeyCounter[] inputHolder,
                                                  CallbackInfoReturnable<Boolean> cir) {
        // 只有推送成功时才处理
        if (Boolean.TRUE.equals(cir.getReturnValue())) {
            expandedae$tryAutoCompleteCraft(patternDetails);
        }
    }

    /**
     * 尝试自动完成合成任务
     * 当检测到自动合成卡已安装且合成任务剩余数量为1时，自动取消任务
     * 
     * 逻辑：在任务只剩最后一个且有样板产物正在返回途中时取消
     * 这样可以让最后一个样板正常推出，然后自动完成任务
     */
    @Unique
    private void expandedae$tryAutoCompleteCraft(IPatternDetails details) {
        // 获取升级槽 - 可能由 AdvancedAE 自身或 AdvancedAE+AppFlux 提供
        IUpgradeInventory upgrades;
        try {
            upgrades = ((IUpgradeableObject) this).getUpgrades();
        } catch (Exception e) {
            // 如果无法获取升级槽，直接返回
            return;
        }

        if (upgrades == null) {
            return;
        }

        // 检查是否安装了自动合成卡
        if (!upgrades.isInstalled(ExpItems.AUTO_COMPLETE_CARD)) {
            return;
        }

        var grid = getGrid();
        if (grid == null) return;

        var cpus = grid.getCraftingService().getCpus();
        for (var cpu : cpus) {
            if (!cpu.isBusy()) continue;

            // 处理原版 CPU
            if (cpu instanceof CraftingCPUCluster cluster) {
                var cpuLogic = (AccessorCraftingCpuLogic) cluster.craftingLogic;
                var job = cpuLogic.getJob();
                if (job == null) continue;
                
                var task = ((AccessorExecutingCraftingJob) job).getTasks().get(details);
                if (task == null) continue;
                
                // 检查任务是否只剩最后一个
                if (task.getValue() > 1) continue;
                
                // 检查是否有产物正在返回途中（waitingFor）
                // 当任务只剩最后一个且有产物正在返回时，取消任务
                // 这样可以让最后一个样板正常推出，然后自动完成任务
                boolean hasItemsWaiting = false;
                for (var output : details.getOutputs()) {
                    if (cpuLogic.invokeGetWaitingFor(output.what()) > 0) {
                        hasItemsWaiting = true;
                        break;
                    }
                }
                
                // 只有当有产物正在返回且任务只剩最后一个时才取消
                if (hasItemsWaiting) {
                    cluster.cancelJob();
                    return;
                }
                continue;
            }

            // 处理 AdvancedAE 的 CPU
            AdvancedAE.handleCpu(cpu, details);
        }
    }
}
