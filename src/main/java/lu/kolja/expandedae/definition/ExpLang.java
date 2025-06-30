package lu.kolja.expandedae.definition;

import appeng.core.localization.LocalizationEnum;

public enum ExpLang implements LocalizationEnum {
    CREATIVETAB_EXPANDEDAE("creativetab.expandedae", "Expanded AE"),
    ITEM_GROUP_EAE("itemGroup.eae", "Expanded AE"),
    INFO_EXPANDEDAE_USELESS("info.expandedae.useless", "This is currently disabled"),

    TOOLTIP_ADVANCED_BLOCKING_CARD_1("item.expandedae.advanced_blocking_card.tooltip.1", "Exposes the entire networks contents when placed in an interface"),
    TOOLTIP_AUTO_COMPLETE_CARD_1("item.expandedae.auto_complete_card.tooltip.1", "Automatically completes the crafting job"),
    TOOLTIP_AUTO_COMPLETE_CARD_2("item.expandedae.auto_complete_card.tooltip.2", "Note: This only works for single requests"),
    TOOLTIP_PATTERN_REFILLER_CARD_1("item.expandedae.pattern_refiller_card.tooltip.1", "Automatically refills the blank patterns"),
    TOOLTIP_SMART_BLOCKING_CARD("item.expandedae.smart_blocking_card.tooltip", "Adds blocking mode options"),
    TOOLTIP_STICKY_CARD_1("item.expandedae.sticky_card.tooltip.1", "Filtered stacks will only enter the network on this storage bus"),
    TOOLTIP_UPGRADE("item.expandedae.upgrade.tooltip", "Upgrade %s"),

    GUI_EXP_PATTERN_PROVIDER("gui.expandedae.exp_pattern_provider", "Expanded Pattern Provider"),
    GUI_EXP_DRIVE("gui.expandedae.exp_drive", "ME Expanded Drive"),
    GUI_FILTER_TERMINAL("gui.expandedae.filter_terminal", "Filter Terminal"),
    GUI_BLOCKING_MODE_ALL("gui.expandedae.blocking_mode.all", "Blocks if target contains anything"),
    GUI_BLOCKING_MODE_DEFAULT("gui.expandedae.blocking_mode.default", "Default blocking mode"),
    GUI_BLOCKING_MODE_SMART("gui.expandedae.blocking_mode.smart", "Allows same pattern to be pushed"),
    GUI_MODIFY_PATTERNS("gui.tooltips.expandedae.modifyPatterns", "Modify Patterns"),
    GUI_MODIFY_PATTERNS_HINT("gui.tooltips.expandedae.modifyPatternsHint", "Left click to multiply, right click to divide \nMultipliers: Shift 2x, Ctrl 8x"),

    GUI_BUTTON_PATTERN_DIV("gui.expandedae.buttons.pattern.div", "§c÷%d§f"),
    GUI_BUTTON_PATTERN_MULT("gui.expandedae.buttons.pattern.mult", "§bx%d§f"),
    GUI_TOOLTIP_PATTERN_DIV("gui.expandedae.buttons.tooltips.pattern.div", "Divides contents by §b%d§f"),
    GUI_TOOLTIP_PATTERN_MULT("gui.expandedae.buttons.tooltips.pattern.mult", "Multiplies contents by §c%d§f"),
    GUI_ARROW("gui.tooltips.expandedae.arrow", "Advanced Encoding"),
    GUI_ARROW_HINT("gui.tooltips.expandedae.arrowHint", "Click to toggle advanced encoding"),

    GROUP_ADV_PATTERN_PROVIDER("group.adv_pattern_provider.name", "ME Advanced Pattern Provider"),
    GROUP_EX_PATTERN_PROVIDER("group.ex_pattern_provider.name", "ME Extended Pattern Provider"),
    GROUP_EXP_PATTERN_PROVIDER("group.exp_pattern_provider.name", "ME Expanded Pattern Provider"),
    GROUP_EXP_WET("group.exp_wet.name", "Expanded Pattern Encoding Terminal"),
    GROUP_MEGA_PATTERN_PROVIDER("group.mega_pattern_provider.name", "ME MEGA Pattern Provider"),
    GROUP_PATTERN_PROVIDER("group.pattern_provider.name", "ME Pattern Provider"),
    GROUP_INTERFACE("group.interface.name", "ME Interface"),
    GROUP_STORAGE_BUS("group.storage_bus.name", "ME Storage Bus"),
    GROUP_EX_INTERFACE("group.ex_interface.name", "ME Extended Interface"),
    GROUP_OVERSIZE_INTERFACE("group.oversize_interface.name", "ME Oversize Interface"),
    GROUP_TAG_STORAGE_BUS("group.tag_storage_bus.name", "ME Tagged Storage Bus"),
    GROUP_MOD_STORAGE_BUS("group.mod_storage_bus.name", "ME Mod Storage Bus"),
    GROUP_PRECISE_STORAGE_BUS("group.precise_storage_bus.name", "ME Precise Storage Bus"),
    GROUP_WIRELESS_EXP_ENCODING_TERMINAL("group.wireless_exp_pattern_encoding_terminal.name", "Wireless Expanded Pattern Encoding Terminal");

    private final String key;
    private final String value;

    ExpLang(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }

    @Override
    public String getEnglishText() {
        return value;
    }
}
