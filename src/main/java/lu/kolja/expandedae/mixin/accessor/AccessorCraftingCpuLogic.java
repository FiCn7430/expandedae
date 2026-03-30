package lu.kolja.expandedae.mixin.accessor;

import appeng.api.stacks.AEKey;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.ExecutingCraftingJob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = CraftingCpuLogic.class, remap = false)
public interface AccessorCraftingCpuLogic {
    @Accessor("job")
    ExecutingCraftingJob getJob();

    /**
     * 获取正在等待的物品数量
     * 用于检测是否还有产物正在返回途中
     */
    @Invoker("getWaitingFor")
    long invokeGetWaitingFor(AEKey template);
}
