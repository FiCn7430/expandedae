package lu.kolja.expandedae.mixin.accessor;

import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.ExecutingCraftingJob;
import appeng.crafting.inv.ListCraftingInventory;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CraftingCpuLogic.class, remap = false)
public interface AccessorCraftingCpuLogic {
    @Accessor("job")
    ExecutingCraftingJob getJob();

    @Accessor("cluster")
    CraftingCPUCluster getCluster();

    @Accessor("inventory")
    ListCraftingInventory getInventory();
}
