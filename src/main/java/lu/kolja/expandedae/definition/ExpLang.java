package lu.kolja.expandedae.definition;

import appeng.core.localization.LocalizationEnum;

public enum ExpLang implements LocalizationEnum {
    CREATIVE_TAB("creativetab.expandedae", "Expanded AE"),
    ITEM_GROUP("itemGroup.eae", "Expanded AE"),
    INFO_USELESS("info.expandedae.useless", "This is currently disabled"),

    ITEM_ADVANCED_BLOCKING_CARD_TOOLTIP_1("item.expandedae.advanced_blocking_card.tooltip.1", "Exposes the entire networks contents when placed in an interface"),
    ITEM_AUTO_COMPLETE_CARD_TOOLTIP_1("item.expandedae.auto_complete_card.tooltip.1", "Automatically completes the crafting job"),
    ITEM_AUTO_COMPLETE_CARD_TOOLTIP_2("item.expandedae.auto_complete_card.tooltip.2", "Note: This only works for single requests"),
    ITEM_PATTERN_REFILLER_CARD_TOOLTIP_1("item.expandedae.pattern_refiller_card.tooltip.1", "Automatically refills the blank patterns"),
    TOOLTIP_GREATER_ACCEL_CARD_1("item.expandedae.greater_accel_card.tooltip.1", "Even greater acceleration"),
    TOOLTIP_GREATER_ACCEL_CARD_2("item.expandedae.greater_accel_card.tooltip.2", "Note: Very power hungry!"),
    ITEM_UPGRADE_TOOLTIP("item.expandedae.upgrade.tooltip", "Upgrade %s"),

    GUI_EXP_PATTERN_PROVIDER("gui.expandedae.exp_pattern_provider", "Expanded Pattern Provider"),
    GUI_EXP_IO_PORT("gui.expandedae.exp_io_port", "Expanded IO Port"),
    GUI_FILTER_TERMINAL("gui.expandedae.filter_terminal", "Filter Terminal"),
    GUI_BLOCKING_MODE("gui.expandedae.blocking_mode", "Blocking Mode - %s"),
    GUI_BLOCKING_MODE_ALL("gui.expandedae.blocking_mode.all", "Blocks if target contains anything"),
    GUI_BLOCKING_MODE_DEFAULT("gui.expandedae.blocking_mode.default", "Default blocking mode"),
    GUI_BLOCKING_MODE_SMART("gui.expandedae.blocking_mode.smart", "Allows same pattern to be pushed"),
    GUI_TOOLTIPS_MODIFY_PATTERNS("gui.tooltips.expandedae.modifyPatterns", "Modify Patterns"),
    GUI_TOOLTIPS_MODIFY_PATTERNS_HINT("gui.tooltips.expandedae.modifyPatternsHint", "Left click to multiply, right click to divide \nMultipliers: Shift 2x, Ctrl 8x"),
    GUI_TOOLTIPS_MODIFY_PATTERNS_GT("gui.tooltips.expandedae.modifyPatternsGT", "Left click to multiply, right click to divide"),
    GUI_TOOLTIPS_MODIFY_PATTERNS_HINT_GT("gui.tooltips.expandedae.modifyPatternsHintGT", "Multipliers: Shift 2x, Ctrl 8x"),
    GUI_TOOLTIPS_ADD_MISSING("gui.tooltips.expandedae.addMissing", "Bookmark"),
    GUI_TOOLTIPS_ADD_MISSING_HINT("gui.tooltips.expandedae.addMissingHint", "Bookmark all missing stacks to your recipe manager"),

    GUI_BUTTONS_PATTERN_DIV("gui.buttons.pattern.div", "§c÷%d§f"),
    GUI_BUTTONS_PATTERN_MULT("gui.buttons.pattern.mult", "§bx%d§f"),
    GUI_BUTTONS_TOOLTIPS_PATTERN_DIV("gui.buttons.tooltips.pattern.div", "Divides contents by §b%d§f"),
    GUI_BUTTONS_TOOLTIPS_PATTERN_MULT("gui.buttons.tooltips.pattern.mult", "Multiplies contents by §c%d§f"),

    GROUP_ADV_PATTERN_PROVIDER_NAME("group.adv_pattern_provider.name", "ME Advanced Pattern Provider"),
    GROUP_EX_PATTERN_PROVIDER_NAME("group.ex_pattern_provider.name", "ME Extended Pattern Provider"),
    GROUP_EXP_PATTERN_PROVIDER_NAME("group.exp_pattern_provider.name", "ME Expanded Pattern Provider"),
    GROUP_EXP_IO_PORT_NAME("group.exp_io_port.name", "ME Expanded IO Port"),
    GROUP_MEGA_PATTERN_PROVIDER_NAME("group.mega_pattern_provider.name", "ME MEGA Pattern Provider"),
    GROUP_PATTERN_PROVIDER_NAME("group.pattern_provider.name", "ME Pattern Provider"),
    GROUP_INTERFACE_NAME("group.interface.name", "ME Interface"),
    GROUP_STORAGE_BUS_NAME("group.storage_bus.name", "ME Storage Bus"),
    GROUP_EX_INTERFACE_NAME("group.ex_interface.name", "ME Extended Interface"),
    GROUP_OVERSIZE_INTERFACE_NAME("group.oversize_interface.name", "ME Oversize Interface"),
    GROUP_TAG_STORAGE_BUS_NAME("group.tag_storage_bus.name", "ME Tagged Storage Bus"),
    GROUP_MOD_STORAGE_BUS_NAME("group.mod_storage_bus.name", "ME Mod Storage Bus"),
    GROUP_PRECISE_STORAGE_BUS_NAME("group.precise_storage_bus.name", "ME Precise Storage Bus"),
    GROUP_PATTERN_ENCODING_TERMINAL_NAME("group.pattern_encoding_terminal.name", "ME Pattern Encoding Terminal"),

    CANCEL_ALL("gui.expandedae.cancel_all", "Cancel All"),
    CANCEL_CONFIRM("gui.expandedae.cancel_confirm", "§4Confirm"),
    CANCEL_CONFIRM_HINT("gui.expandedae.cancel_confirm.hint", "Press to confirm"),
    CANCEL_ALL_HINT("gui.expandedae.cancel_all.hint", "Cancel all running Crafts"),
    BYTES_USED("gui.expandedae.bytes_used", "%sB Used"),
    PRIO_CHANGED("msg.expandedae.prio_changed", "Set priority of %s to %s"),

    PRIO_RESET("msg.expandedae.prio_reset", "Reset priority to %s"),
    CHANGED_MODE("msg.expandedae.mode_changed", "Changed mode to %s"),

    PRIO_CARD_HINT_1("item.expandedae.priority_card.hint.1", "Shift-click a block/part to modify its priority"),
    PRIO_CARD_HINT_2("item.expandedae.priority_card.hint.2", "Shift-click the air to reset the internal priority"),
    PRIO_CARD_HINT_3("item.expandedae.priority_card.hint.3", "Right-click the air to toggle the mode"),
    CURRENT("text.expandedae.card", "Current: %s"),

    SWITCH_PAGE("gui.expandedae.switch_page", "%s Page"),
    SWITCH_PAGE_HINT("gui.expandedae.switch_page.hint", "Click to go to the %s page"),

    BOUND_TO("msg.expandedae.bound_to", "Bound to %s"),
    SHIFT_INFO("gui.expandedae.shift_info", "§7Press §o[SHIFT] §r§7for more info"),
    LINKED_TERM_HINT("gui.expandedae.linked_terminal.hint", "§c§lNote: This will only pull blocks from your ME network, not your inventory"),

    HIGHLIGHTED_BLOCK("msg.expandedae.highlighted", "Highlighted block at %s §rin %s"),
    HIGHLIGHT("gui.expandedae.highlight", "Shift-Click to highlight");

    private final String key;
    private final String text;

    ExpLang(String key, String text) {
        this.key = key;
        this.text = text;
    }

    @Override
    public String getEnglishText() {
        return this.text;
    }

    @Override
    public String getTranslationKey() {
        return this.key;
    }
}