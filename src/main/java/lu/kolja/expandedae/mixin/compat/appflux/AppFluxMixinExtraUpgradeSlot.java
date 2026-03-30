package lu.kolja.expandedae.mixin.compat.appflux;

import appeng.api.networking.IManagedGridNode;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import lu.kolja.expandedae.util.CombinedUpgradeInventory;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 当 AppFlux 加载时，为原版 PatternProviderLogic 添加额外升级槽
 *
 * AppFlux 已经添加了一个升级槽，这个 Mixin 再添加一个专用于自动合成卡的槽位
 */
@Mixin(value = PatternProviderLogic.class, remap = false, priority = 1002)
public abstract class AppFluxMixinExtraUpgradeSlot implements IUpgradeableObject {

    @Shadow
    private IUpgradeInventory af_upgrades;

    @Unique
    private IUpgradeInventory expandedae$extraUpgradeSlot = UpgradeInventories.empty();

    /**
     * 初始化额外升级槽
     */
    @Inject(
            method = "<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/patternprovider/PatternProviderLogicHost;I)V",
            at = @At("TAIL"),
            remap = false)
    private void expandedae$initExtraUpgrade(IManagedGridNode mainNode, PatternProviderLogicHost host,
                                              int patternInventorySize, CallbackInfo ci) {
        // 创建额外的升级槽（1个槽位，专用于自动合成卡）
        this.expandedae$extraUpgradeSlot = UpgradeInventories.forMachine(
                host.getTerminalIcon().getItem(),
                1,
                this::expandedae$onExtraUpgradeChanged
        );
    }

    @Unique
    private void expandedae$onExtraUpgradeChanged() {
        // 保存变更
    }

    /**
     * 保存额外升级槽到 NBT
     */
    @Inject(method = "writeToNBT", at = @At("TAIL"), remap = false)
    private void expandedae$saveExtraUpgrade(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        this.expandedae$extraUpgradeSlot.writeToNBT(tag, "expandedae_extra_upgrades", registries);
    }

    /**
     * 从 NBT 读取额外升级槽
     */
    @Inject(method = "readFromNBT", at = @At("TAIL"), remap = false)
    private void expandedae$loadExtraUpgrade(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        this.expandedae$extraUpgradeSlot.readFromNBT(tag, "expandedae_extra_upgrades", registries);
    }

    /**
     * 添加掉落物
     */
    @Inject(method = "addDrops", at = @At("TAIL"), remap = false)
    private void expandedae$dropExtraUpgrade(List<ItemStack> drops, CallbackInfo ci) {
        for (var is : this.expandedae$extraUpgradeSlot) {
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }
    }

    /**
     * 清除内容
     */
    @Inject(method = "clearContent", at = @At("TAIL"), remap = false)
    private void expandedae$clearExtraUpgrade(CallbackInfo ci) {
        this.expandedae$extraUpgradeSlot.clear();
    }

    /**
     * 获取组合升级槽
     *
     * 注意：这个方法会覆盖 AppFlux 的 getUpgrades() 方法返回的结果
     * 返回一个 CombinedUpgradeInventory，包含 AppFlux 的槽位和我们的额外槽位
     */
    @Override
    public IUpgradeInventory getUpgrades() {
        // 使用 Shadow 直接访问 AppFlux 的升级槽字段，避免递归
        return new CombinedUpgradeInventory(
                this.af_upgrades,
                this.expandedae$extraUpgradeSlot
        );
    }
}
