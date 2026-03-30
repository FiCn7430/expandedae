package lu.kolja.expandedae.mixin.cpu;

import appeng.api.crafting.IPatternDetails;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import lu.kolja.expandedae.helper.pattern.IAutoCompletableCPU;
import lu.kolja.expandedae.mixin.accessor.AccessorCraftingCpuLogic;
import lu.kolja.expandedae.mixin.accessor.AccessorExecutingCraftingJob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

/**
 * Mixin for CraftingCPUCluster
 * 实现 IAutoCompletableCPU 接口以支持自动完成功能
 */
@Mixin(value = CraftingCPUCluster.class, remap = false)
public class MixinCraftingCPUCluster implements IAutoCompletableCPU {

    @Shadow
    public CraftingCpuLogic craftingLogic;

    @Redirect(
            method = "addBlockEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/blockentity/crafting/CraftingBlockEntity;getAcceleratorThreads()I",
                    ordinal = 1
            ),
            remap = false
    )
    private int modifyThreadLimit(CraftingBlockEntity instance) {
        return 1;
        //so any number of threads is allowed
    }

    /**
     * 检查指定的合成任务是否应该被自动完成
     * 通过 Accessor Mixin 访问私有字段
     */
    @Override
    public boolean expandedae$shouldAutoComplete(IPatternDetails details) {
        // 使用 Accessor Mixin 获取 job
        var job = ((AccessorCraftingCpuLogic) craftingLogic).getJob();
        if (job == null) return false;

        // 使用 Accessor Mixin 获取 tasks
        var tasks = ((AccessorExecutingCraftingJob) job).getTasks();
        var task = tasks.get(details);

        return task != null && task.getValue() <= 1;
    }
}