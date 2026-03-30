package lu.kolja.expandedae.mixin.compat.advancedae;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.UpgradeSlot;
import lu.kolja.expandedae.helper.pattern.IUpgradableMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.ItemLike;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogicHost;
import net.pedroksl.advanced_ae.gui.advpatternprovider.AdvPatternProviderMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 为 AdvancedAE 的样板供应器菜单添加升级槽支持
 */
@Mixin(value = AdvPatternProviderMenu.class, remap = false)
public abstract class MixinAdvPatternProviderMenu extends AEBaseMenu implements IUpgradableMenu {

    @Final
    @Shadow
    protected AdvPatternProviderLogic logic;

    @Unique
    private appeng.menu.ToolboxMenu expandedae$toolbox;

    /**
     * 在构造函数中初始化升级槽
     */
    @Inject(
            method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;"
                    + "Lnet/pedroksl/advanced_ae/common/logic/AdvPatternProviderLogicHost;)V",
            at = @At("TAIL"),
            remap = false)
    private void expandedae$initUpgrades(
            MenuType menuType, int id, Inventory playerInventory, AdvPatternProviderLogicHost host, CallbackInfo ci) {
        // 添加升级槽到菜单
        var upgrades = ((IUpgradeableObject) this.logic).getUpgrades();
        for (int i = 0; i < upgrades.size(); i++) {
            this.addSlot(new UpgradeSlot(upgrades, i), SlotSemantics.UPGRADE);
        }
    }

    /**
     * 获取工具箱菜单
     */
    @Override
    public appeng.menu.ToolboxMenu expandedae$getToolbox() {
        return this.expandedae$toolbox;
    }

    /**
     * 获取升级槽
     */
    @Override
    public IUpgradeInventory expandedae$getUpgrades() {
        return ((IUpgradeableObject) this.logic).getUpgrades();
    }

    /**
     * 检查是否安装了指定升级卡
     */
    @Override
    public boolean expandedae$hasUpgrade(ItemLike upgradeCard) {
        return expandedae$getUpgrades().isInstalled(upgradeCard);
    }

    public MixinAdvPatternProviderMenu(MenuType<?> menuType, int id, Inventory playerInventory, Object host) {
        super(menuType, id, playerInventory, host);
    }
}
