package lu.kolja.expandedae.mixin.compat.appflux;

import appeng.api.config.Actionable;
import appeng.api.config.LockCraftingMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.networking.IGrid;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.IConfigManager;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.ConfigManager;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import lu.kolja.expandedae.definition.ExpItems;
import lu.kolja.expandedae.definition.ExpSettings;
import lu.kolja.expandedae.enums.BlockingMode;
import lu.kolja.expandedae.helper.pattern.IPatternProviderLogic;
import lu.kolja.expandedae.helper.pattern.PatternProviderTarget;
import lu.kolja.expandedae.helper.pattern.PatternProviderTargetCache;
import lu.kolja.expandedae.mixin.accessor.AccessorCraftingCpuLogic;
import lu.kolja.expandedae.mixin.accessor.AccessorExecutingCraftingJob;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mixin(value = PatternProviderLogic.class, remap = false)
public abstract class AppFluxMixinPatternProviderLogic implements IUpgradeableObject, IPatternProviderLogic {
    @Unique
    private PatternProviderTargetCache[] expandedae$targetCaches;

    @Shadow
    @Final
    private IActionSource actionSource;

    @Final
    @Shadow
    private PatternProviderLogicHost host;

    @Final
    @Shadow
    private IManagedGridNode mainNode;

    @Shadow
    @Final
    private IConfigManager configManager;

    @Shadow @Final private Set<AEKey> patternInputs;

    @Shadow private int roundRobinIndex;

    @Shadow @Final private List<GenericStack> sendList;

    @Shadow @Final private List<IPatternDetails> patterns;

    @Shadow private Direction sendDirection;

    @Shadow public abstract LockCraftingMode getCraftingLockedReason();

    @Shadow protected abstract Set<Direction> getActiveSides();

    @Shadow protected abstract void onPushPatternSuccess(IPatternDetails pattern);

    @Shadow protected abstract <T> void rearrangeRoundRobin(List<T> list);

    @Shadow public abstract boolean isBlocking();

    @Shadow protected abstract boolean sendStacksOut();

    @Shadow protected abstract void addToSendList(AEKey what, long amount);

    @Shadow public abstract @Nullable IGrid getGrid();

    @Unique
    private void eae_$onUpgradesChanged() {
        /*
        if (!eae_$upgrades.isInstalled(ExpItems.SMART_BLOCKING_CARD)) { //TODO: smart card unlocks extra blocking modes
            assert Minecraft.getInstance().screen != null;
            ((IPatternProvider) me)
            ((IBlockingMode) Minecraft.getInstance().screen).setVisible(false);
        } else {
            assert Minecraft.getInstance().screen != null;
            ((IBlockingMode) Minecraft.getInstance().screen).setVisible(true);
        }*/
        this.host.saveChanges();
    }

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
    private void PatternProviderLogic(IManagedGridNode mainNode, PatternProviderLogicHost host,
                                      int patternInventorySize, CallbackInfo ci) {
        ((ConfigManager) configManager).registerSetting(ExpSettings.BLOCKING_MODE, BlockingMode.DEFAULT);
    }

    @Override
    public BlockingMode expandedae$getBlockingMode() {
        return configManager.getSetting(ExpSettings.BLOCKING_MODE);
    }

    /**
     * @author Kolja
     * @reason .
     */
    @Overwrite
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (this.sendList.isEmpty() && this.mainNode.isActive() && this.patterns.contains(patternDetails)) {
            BlockEntity be = this.host.getBlockEntity();
            Level level = be.getLevel();
            if (this.getCraftingLockedReason() == LockCraftingMode.NONE) {
                record PushTarget(Direction direction, PatternProviderTarget target) {}

                var possibleTargets = new ArrayList<PushTarget>();

                for (Direction direction : this.getActiveSides()) {
                    BlockPos adjPos = be.getBlockPos().relative(direction);
                    BlockEntity adjBe = level.getBlockEntity(adjPos);
                    Direction adjBeSide = direction.getOpposite();
                    ICraftingMachine craftingMachine = ICraftingMachine.of(level, adjPos, adjBeSide);
                    if (craftingMachine != null && craftingMachine.acceptsPlans()) {
                        if (craftingMachine.pushPattern(patternDetails, inputHolder, adjBeSide)) {
                            this.onPushPatternSuccess(patternDetails);
                            return true;
                        }
                    } else {
                        PatternProviderTarget adapter = this.expandedae$findAdapter(direction);
                        if (adapter != null) {
                            possibleTargets.add(new PushTarget(direction, adapter));
                        }
                    }
                }

                if (patternDetails.supportsPushInputsToExternalInventory()) {
                    this.rearrangeRoundRobin(possibleTargets);

                    for (PushTarget target : possibleTargets) {
                        Direction direction = target.direction();
                        PatternProviderTarget adapter = target.target();
                        switch (expandedae$getBlockingMode()) {
                            case ALL -> {
                                if ((!this.isBlocking() || adapter.getStorage().getAvailableStacks().isEmpty()) && this.expandedae$adapterAcceptsAll(adapter, inputHolder)) {
                                    patternDetails.pushInputsToExternalInventory(inputHolder, (what, amount) -> {
                                        long inserted = adapter.insert(what, amount, Actionable.MODULATE);
                                        if (inserted < amount) {
                                            this.addToSendList(what, amount - inserted);
                                        }
                                    });
                                    this.onPushPatternSuccess(patternDetails);
                                    this.sendDirection = direction;
                                    this.sendStacksOut();
                                    ++this.roundRobinIndex;
                                    return true;
                                }
                            }
                            case SMART -> {
                                if ((!this.isBlocking() || adapter.getStorage().getAvailableStacks().isEmpty() || adapter.onlyHasPatternInput(this.patternInputs)) && this.expandedae$adapterAcceptsAll(adapter, inputHolder)) {
                                    patternDetails.pushInputsToExternalInventory(inputHolder, (what, amount) -> {
                                        long inserted = adapter.insert(what, amount, Actionable.MODULATE);
                                        if (inserted < amount) {
                                            this.addToSendList(what, amount - inserted);
                                        }
                                    });
                                    this.onPushPatternSuccess(patternDetails);
                                    this.sendDirection = direction;
                                    this.sendStacksOut();
                                    ++this.roundRobinIndex;
                                    return true;
                                }
                            }
                            case DEFAULT -> {
                                if ((!this.isBlocking() || !adapter.containsPatternInput(this.patternInputs)) && this.expandedae$adapterAcceptsAll(adapter, inputHolder)) {
                                    patternDetails.pushInputsToExternalInventory(inputHolder, (what, amount) -> {
                                        long inserted = adapter.insert(what, amount, Actionable.MODULATE);
                                        if (inserted < amount) {
                                            this.addToSendList(what, amount - inserted);
                                        }
                                    });
                                    this.onPushPatternSuccess(patternDetails);
                                    this.sendDirection = direction;
                                    this.sendStacksOut();
                                    ++this.roundRobinIndex;
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Unique
    private @Nullable PatternProviderTarget expandedae$findAdapter(Direction side) {
        if (this.expandedae$targetCaches[side.get3DDataValue()] == null) {
            BlockEntity thisBe = this.host.getBlockEntity();
            this.expandedae$targetCaches[side.get3DDataValue()] = new PatternProviderTargetCache((ServerLevel) thisBe.getLevel(), thisBe.getBlockPos().relative(side), side.getOpposite(), this.actionSource);
        }
        return this.expandedae$targetCaches[side.get3DDataValue()].find();
    }

    @Unique
    private boolean expandedae$adapterAcceptsAll(PatternProviderTarget target, KeyCounter[] inputHolder) {
        int var4 = inputHolder.length;
        for (KeyCounter counter : inputHolder) {
            for (Object2LongMap.Entry<AEKey> input : counter) {
                long inserted = target.insert(input.getKey(), input.getLongValue(), Actionable.SIMULATE);
                if (inserted == 0L) {
                    return false;
                }
            }
        }
        return true;
    }

    @Inject(
            method = "pushPattern",
            at = @At("HEAD")
    )
    private void expandedae$onPushPatternSuccess(IPatternDetails patternDetails, KeyCounter[] inputHolder, CallbackInfoReturnable<Boolean> cir) {
        expandedae$tryAutoCompleteCraft(patternDetails);
    }

    @Unique
    private void expandedae$tryAutoCompleteCraft(IPatternDetails details) {
        if (!getUpgrades().isInstalled(ExpItems.AUTO_COMPLETE_CARD)) return;
        getGrid().getCraftingService().getCpus().stream()
                .filter(ICraftingCPU::isBusy)
                .map(cpu -> (CraftingCPUCluster) cpu)
                .filter(cluster -> ((AccessorExecutingCraftingJob) ((AccessorCraftingCpuLogic) cluster.craftingLogic).getJob()).getTasks().get(details).getValue() <= 1)
                .findFirst()
                .ifPresent(ICraftingCPU::cancelJob);
    }
}
