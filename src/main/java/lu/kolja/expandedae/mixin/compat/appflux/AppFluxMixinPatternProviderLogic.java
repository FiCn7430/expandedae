package lu.kolja.expandedae.mixin.compat.appflux;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.IConfigManager;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.ConfigManager;
import com.llamalad7.mixinextras.sugar.Local;
import lu.kolja.expandedae.definition.ExpItems;
import lu.kolja.expandedae.definition.ExpSettings;
import lu.kolja.expandedae.enums.ADDONS;
import lu.kolja.expandedae.enums.BlockingMode;
import lu.kolja.expandedae.helper.pattern.IPatternProviderLogic;
import lu.kolja.expandedae.helper.pattern.PatternProviderTargetCache;
import lu.kolja.expandedae.mixin.accessor.AccessorCraftingCpuLogic;
import lu.kolja.expandedae.xmod.advancedae.AdvancedAE;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * AppFlux 兼容模式下的 PatternProviderLogic Mixin
 * 注意：当 AppFlux 加载时，它会为 PatternProviderLogic 添加升级槽
 * 这个 Mixin 只添加自动合成卡检测逻辑和其他功能
 */
@Mixin(value = PatternProviderLogic.class, remap = false)
public abstract class AppFluxMixinPatternProviderLogic implements IUpgradeableObject, IPatternProviderLogic {
    @Unique
    private static final boolean AAE_LOADED = ADDONS.ADV.isLoaded();

    @Unique
    private PatternProviderTargetCache[] expandedae$targetCaches;
    @Final
    @Shadow
    private PatternProviderLogicHost host;
    @Final
    @Shadow
    private IActionSource actionSource;

    @Shadow public abstract @Nullable IGrid getGrid();

    @Shadow @Final private IConfigManager configManager;

    @Inject(
            method = "<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/patternprovider/PatternProviderLogicHost;I)V",
            at = @At("TAIL")
    )
    private void eae_$initUpgrade(IManagedGridNode mainNode, PatternProviderLogicHost host, int patternInventorySize, CallbackInfo ci) {
        this.expandedae$targetCaches = new PatternProviderTargetCache[6];
    }

    @Inject(method = "<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/patternprovider/PatternProviderLogicHost;I)V",
            at = @At("TAIL"),
            remap = false)
    private void PatternProviderLogic(IManagedGridNode mainNode, PatternProviderLogicHost host, int patternInventorySize, CallbackInfo ci) {
        eae$getConfigManager().registerSetting(ExpSettings.BLOCKING_MODE, BlockingMode.DEFAULT);
    }

    @Override
    public BlockingMode expandedae$getBlockingMode() {
        return eae$getConfigManager().getSetting(ExpSettings.BLOCKING_MODE);
    }

    /**
     * @author Kolja
     * @reason Better blocking modes without invasive overwrites
     */
    @Overwrite
    @Nullable
    private PatternProviderTarget findAdapter(Direction side) {
        if (this.expandedae$targetCaches[side.get3DDataValue()] == null) {
            BlockEntity thisBe = this.host.getBlockEntity();
            this.expandedae$targetCaches[side.get3DDataValue()] = new PatternProviderTargetCache(
                    (ServerLevel) thisBe.getLevel(),
                    thisBe.getBlockPos().relative(side),
                    side.getOpposite(),
                    this.actionSource,
                    eae$getConfigManager()
            );
        }
        return this.expandedae$targetCaches[side.get3DDataValue()].find();
    }

    @Inject(
            method = "pushPattern",
            at = @At("RETURN")
    )
    private void expandedae$onPushPatternSuccess(IPatternDetails patternDetails, KeyCounter[] inputHolder, CallbackInfoReturnable<Boolean> cir) {
        // 只有推送成功时才检查自动合成卡
        if (Boolean.TRUE.equals(cir.getReturnValue())) {
            expandedae$tryAutoCompleteCraft(patternDetails);
        }
    }

    /**
     * 尝试自动完成合成任务
     * 当检测到自动合成卡已安装且 CPU 库存为空时，自动取消任务
     */
    @Unique
    private void expandedae$tryAutoCompleteCraft(IPatternDetails details) {
        // 当 AppFlux 加载时，它提供了升级槽，我们直接调用 getUpgrades()
        IUpgradeInventory upgrades = getUpgrades();
        if (upgrades == null || !upgrades.isInstalled(ExpItems.AUTO_COMPLETE_CARD)) return;

        var cpus = getGrid().getCraftingService().getCpus();
        for (var cpu : cpus) {
            if (!cpu.isBusy()) continue;
            if (cpu instanceof CraftingCPUCluster cluster) {
                var cpuLogic = (AccessorCraftingCpuLogic) cluster.craftingLogic;
                var job = cpuLogic.getJob();
                if (job == null) continue;

                var inventory = cpuLogic.getInventory();

                // 检测 CPU 库存是否为空
                if (!inventory.list.isEmpty()) {
                    continue;
                }

                // 库存为空，取消任务
                cluster.cancelJob();
                return;
            }
            if (!AAE_LOADED) continue;
            AdvancedAE.handleCpu(cpu, details);
        }
    }

    @ModifyArg(
            method = "pushPattern",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/helpers/patternprovider/PatternProviderTarget;containsPatternInput(Ljava/util/Set;)Z"
            )
    )
    private Set<AEKey> modifiedContainsPatternInput(Set<AEKey> patternInputs, @Local(argsOnly = true) IPatternDetails patternDetails) {
        if (expandedae$getBlockingMode() != BlockingMode.SMART) return patternInputs;
        // This is more efficient than streams, even tho it's a minimal difference,
        // since this is a high-frequency call I'd rather do it like this
        var result = new HashSet<AEKey>();
        for (var input : patternDetails.getInputs()) {
            result.add(input.getPossibleInputs()[0].what());
        }
        return result;
    }

    @Unique
    private ConfigManager eae$getConfigManager() {
        return (ConfigManager) configManager;
    }
}
