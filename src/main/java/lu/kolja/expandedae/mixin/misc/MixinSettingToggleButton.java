package lu.kolja.expandedae.mixin.misc;

import appeng.api.config.CondenserOutput;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.client.gui.Icon;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.localization.ButtonToolTips;
import lu.kolja.expandedae.definition.ExpLang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static lu.kolja.expandedae.definition.ExpSettings.BLOCKING_MODE;
import static lu.kolja.expandedae.enums.BlockingMode.*;

@Mixin(value = SettingToggleButton.class, remap = false)
public abstract class MixinSettingToggleButton {
    @Shadow
    private static Map<SettingToggleButton.EnumPair<?>, SettingToggleButton.ButtonAppearance> appearances;

    @Shadow
    private static <T extends Enum<T>> void registerApp(Icon icon, Setting<T> setting, T val, ButtonToolTips title, ButtonToolTips hint) {}

    @Redirect(method = "<init>(Lappeng/api/config/Setting;Ljava/lang/Enum;Ljava/util/function/Predicate;Lappeng/client/gui/widgets/SettingToggleButton$IHandler;)V",
            at = @At(value = "INVOKE",
                    target = "Lappeng/client/gui/widgets/SettingToggleButton;registerApp(Lappeng/client/gui/Icon;Lappeng/api/config/Setting;Ljava/lang/Enum;Lappeng/core/localization/ButtonToolTips;Lappeng/core/localization/ButtonToolTips;)V",
                    ordinal = 0),
            remap = false)
    private <T extends Enum<T>> void register(Icon icon, Setting<T> setting, T val, ButtonToolTips title,
                                              ButtonToolTips hint) {

        registerApp(Icon.CONDENSER_OUTPUT_TRASH, Settings.CONDENSER_OUTPUT, CondenserOutput.TRASH,
                ButtonToolTips.CondenserOutput,
                ButtonToolTips.Trash);
        eae$registerIcon(Icon.CLEAR, BLOCKING_MODE, ALL,
                ExpLang.GUI_BLOCKING_MODE.text(ALL.getName()),
                ExpLang.GUI_BLOCKING_MODE_ALL.text()
        );
        eae$registerIcon(Icon.BLOCKING_MODE_YES, BLOCKING_MODE, DEFAULT,
                ExpLang.GUI_BLOCKING_MODE.text(DEFAULT.getName()),
                ExpLang.GUI_BLOCKING_MODE_DEFAULT.text()
        );
        eae$registerIcon(Icon.BLOCKING_MODE_NO, BLOCKING_MODE, SMART,
                ExpLang.GUI_BLOCKING_MODE.text(SMART.getName()),
                ExpLang.GUI_BLOCKING_MODE_SMART.text()
        );
    }

    @Unique
    private static <T extends Enum<T>> void eae$registerIcon(Icon icon, Setting<T> setting, T val, Component title, Component... tooltipLines) {
        var lines = new ArrayList<Component>();
        lines.add(title);
        Collections.addAll(lines, tooltipLines);

        appearances.put(new SettingToggleButton.EnumPair<>(setting, val), ButtonAppearanceMixin.eae$create(icon, null, lines));
    }

    @Mixin(value = SettingToggleButton.ButtonAppearance.class, remap = false)
    private static class ButtonAppearanceMixin {
        @Invoker("<init>")
        static SettingToggleButton.ButtonAppearance eae$create(@Nullable Icon icon, @Nullable Item item, List<Component> tooltipLines) {
            throw new AssertionError();
        }
    }
}