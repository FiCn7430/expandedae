package lu.kolja.expandedae.mixin.compat.advancedae;

import appeng.api.networking.IManagedGridNode;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogicHost;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 为 AdvancedAE 的样板供应器添加升级槽支持（仅在无 AppFlux 时加载）
 * 
 * 当 AppFlux 加载时，AdvancedAE 自身会通过 Mixin 添加升级槽支持，
 * 所以我们不需要重复添加。
 * 
 * 这个 Mixin 的优先级设置为 1001，确保在 AdvancedAE 的 Mixin 之前加载，
 * 但只有当 AppFlux 未加载时才会应用。
 */
@Mixin(value = AdvPatternProviderLogic.class, remap = false, priority = 1001)
public abstract class MixinAdvPatternProviderLogicUpgrades implements IUpgradeableObject {

    @Final
    @Shadow
    private AdvPatternProviderLogicHost host;

    @Unique
    private IUpgradeInventory expandedae$upgrades = UpgradeInventories.empty();

    /**
     * 初始化升级槽
     */
    @Inject(
            method = "<init>(Lappeng/api/networking/IManagedGridNode;Lnet/pedroksl/advanced_ae/common/logic/AdvPatternProviderLogicHost;I)V",
            at = @At("TAIL"))
    private void expandedae$initUpgrade(
            IManagedGridNode mainNode, AdvPatternProviderLogicHost host, int patternInventorySize, CallbackInfo ci) {
        this.expandedae$upgrades =
                UpgradeInventories.forMachine(host.getTerminalIcon().getItem(), 1, this::expandedae$onUpgradesChanged);
    }

    /**
     * 升级发生变化时的回调
     */
    @Unique
    private void expandedae$onUpgradesChanged() {
        this.host.saveChanges();
    }

    /**
     * 获取升级槽
     */
    @Override
    public IUpgradeInventory getUpgrades() {
        return this.expandedae$upgrades;
    }

    /**
     * 保存升级数据到 NBT
     */
    @Inject(method = "writeToNBT", at = @At("TAIL"))
    private void expandedae$saveUpgrade(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        this.expandedae$upgrades.writeToNBT(tag, "expandedae_upgrades", registries);
    }

    /**
     * 从 NBT 读取升级数据
     */
    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void expandedae$loadUpgrade(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        this.expandedae$upgrades.readFromNBT(tag, "expandedae_upgrades", registries);
    }

    /**
     * 添加掉落物（升级卡）
     */
    @Inject(method = "addDrops", at = @At("TAIL"))
    private void expandedae$dropUpgrade(List<ItemStack> drops, CallbackInfo ci) {
        for (var is : this.expandedae$upgrades) {
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }
    }

    /**
     * 清除内容时同时清除升级
     */
    @Inject(method = "clearContent", at = @At("TAIL"))
    private void expandedae$clearUpgrade(CallbackInfo ci) {
        this.expandedae$upgrades.clear();
    }
}
