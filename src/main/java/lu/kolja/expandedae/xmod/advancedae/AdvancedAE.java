package lu.kolja.expandedae.xmod.advancedae;

import appeng.api.upgrades.Upgrades;
import net.pedroksl.advanced_ae.common.definitions.AAEBlocks;
import net.pedroksl.advanced_ae.common.definitions.AAEItems;

import static lu.kolja.expandedae.definition.ExpItems.AUTO_COMPLETE_CARD;

/**
 * Advanced AE 兼容性处理类
 * 负责注册 Expanded AE 的升级卡片到 Advanced AE 的方块和物品
 */
public class AdvancedAE {

    public AdvancedAE() {
        Upgrades.add(AUTO_COMPLETE_CARD, AAEItems.SMALL_ADV_PATTERN_PROVIDER, 1, "group.adv_pattern_provider.name");
        Upgrades.add(AUTO_COMPLETE_CARD, AAEItems.ADV_PATTERN_PROVIDER, 1, "group.advanced_pattern_provider.name");

        Upgrades.add(AUTO_COMPLETE_CARD, AAEBlocks.SMALL_ADV_PATTERN_PROVIDER, 1, "group.adv_pattern_provider.name");
        Upgrades.add(AUTO_COMPLETE_CARD, AAEBlocks.ADV_PATTERN_PROVIDER, 1, "group.advanced_pattern_provider.name");
    }
}
