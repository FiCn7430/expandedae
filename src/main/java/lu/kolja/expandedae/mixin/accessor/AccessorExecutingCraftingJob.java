package lu.kolja.expandedae.mixin.accessor;

import appeng.api.crafting.IPatternDetails;
import appeng.crafting.execution.ExecutingCraftingJob;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ExecutingCraftingJob.class, remap = false)
public interface AccessorExecutingCraftingJob {
    @Accessor("tasks")
    Map<IPatternDetails, AccessorTaskProgress> getTasks();

    @Mixin(targets = "appeng.crafting.execution.ExecutingCraftingJob$TaskProgress")
    interface AccessorTaskProgress {
        @Accessor("value")
        long getValue();
    }
}