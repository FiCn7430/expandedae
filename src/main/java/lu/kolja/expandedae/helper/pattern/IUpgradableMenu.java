package lu.kolja.expandedae.helper.pattern;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.menu.ToolboxMenu;
import net.minecraft.world.level.ItemLike;

public interface IUpgradableMenu {

    ToolboxMenu expandedae$getToolbox();

    IUpgradeInventory expandedae$getUpgrades();

    boolean expandedae$hasUpgrade(ItemLike upgradeCard);

}