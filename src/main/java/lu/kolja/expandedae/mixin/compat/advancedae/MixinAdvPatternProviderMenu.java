package lu.kolja.expandedae.mixin.compat.advancedae;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.menu.AEBaseMenu;
import appeng.menu.ToolboxMenu;
import lu.kolja.expandedae.helper.pattern.IUpgradableMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.ItemLike;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic;
import net.pedroksl.advanced_ae.gui.advpatternprovider.AdvPatternProviderMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * 为 AdvancedAE 的样板供应器菜单添加 IUpgradableMenu 接口支持
 *
 * 注意：当 AppFlux 加载时，AdvancedAE 自身会通过 Mixin 添加完整的升级槽支持。
 * 当 AppFlux 未加载时，升级槽的创建由 MixinAdvPatternProviderLogicUpgrades 处理。
 *
 * 这个 Mixin 只负责实现 IUpgradableMenu 接口，提供获取升级槽的方法。
 */
@Mixin(value = AdvPatternProviderMenu.class, remap = false)
public abstract class MixinAdvPatternProviderMenu extends AEBaseMenu implements IUpgradableMenu {

    @Final
    @Shadow
    protected AdvPatternProviderLogic logic;

    @Unique
    private ToolboxMenu expandedae$toolbox;

    /**
     * 获取工具箱菜单
     */
    @Override
    public ToolboxMenu expandedae$getToolbox() {
        return this.expandedae$toolbox;
    }

    /**
     * 获取升级槽
     *
     * 通过 IUpgradeableObject 接口访问升级槽
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
        IUpgradeInventory upgrades = expandedae$getUpgrades();
        return upgrades != null && upgrades.isInstalled(upgradeCard);
    }

    public MixinAdvPatternProviderMenu(MenuType<?> menuType, int id, Inventory playerInventory, Object host) {
        super(menuType, id, playerInventory, host);
    }
}
