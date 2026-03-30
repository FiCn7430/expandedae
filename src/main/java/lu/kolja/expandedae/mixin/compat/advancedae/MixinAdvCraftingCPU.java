package lu.kolja.expandedae.mixin.compat.advancedae;

import appeng.api.crafting.IPatternDetails;
import lu.kolja.expandedae.helper.pattern.IAutoCompletableCPU;
import net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPU;
import net.pedroksl.advanced_ae.common.logic.AdvCraftingCPULogic;
import net.pedroksl.advanced_ae.common.logic.ExecutingCraftingJob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

/**
 * 让 AdvCraftingCPU 实现 IAutoCompletableCPU 接口
 * 同时在这个 Mixin 中直接定义所需的 Accessor 方法
 */
@Mixin(value = AdvCraftingCPU.class, remap = false)
public abstract class MixinAdvCraftingCPU implements IAutoCompletableCPU {

    @Shadow
    public AdvCraftingCPULogic craftingLogic;

    /**
     * 检查指定的合成任务是否应该被自动完成
     * 使用 Invoker 访问私有方法/字段
     */
    @Override
    public boolean expandedae$shouldAutoComplete(IPatternDetails details) {
        // 使用 AdvCraftingCPULogic 的 Accessor 获取 job
        ExecutingCraftingJob job = ((AdvCraftingCPULogicAccessor) craftingLogic).getJob();
        if (job == null) return false;

        // 使用 ExecutingCraftingJob 的 Accessor 获取 tasks
        Map<IPatternDetails, TaskProgressAccessor> tasks = ((ExecutingCraftingJobAccessor) job).getTasks();
        TaskProgressAccessor task = tasks.get(details);

        return task != null && task.getValue() <= 1;
    }

    /**
     * AdvCraftingCPULogic 的 Accessor 接口
     */
    @Mixin(value = AdvCraftingCPULogic.class, remap = false)
    public interface AdvCraftingCPULogicAccessor {
        @Accessor("job")
        ExecutingCraftingJob getJob();
    }

    /**
     * ExecutingCraftingJob 的 Accessor 接口
     */
    @Mixin(value = ExecutingCraftingJob.class, remap = false)
    public interface ExecutingCraftingJobAccessor {
        @Accessor("tasks")
        Map<IPatternDetails, TaskProgressAccessor> getTasks();
    }

    /**
     * TaskProgress 的 Accessor 接口
     */
    @Mixin(targets = "net.pedroksl.advanced_ae.common.logic.ExecutingCraftingJob$TaskProgress", remap = false)
    public interface TaskProgressAccessor {
        @Accessor("value")
        long getValue();
    }
}
