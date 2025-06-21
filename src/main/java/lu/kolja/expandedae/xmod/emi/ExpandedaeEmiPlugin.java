package lu.kolja.expandedae.xmod.emi;

import appeng.integration.modules.emi.EmiEncodePatternHandler;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import lu.kolja.expandedae.definition.ExpMenus;
import lu.kolja.expandedae.terminal.ExpEncodingTerminalMenu;
import lu.kolja.expandedae.terminal.wtlib.ExpWETMenu;

@EmiEntrypoint
public class ExpandedaeEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addRecipeHandler(
                ExpMenus.EXP_ENCODING_TERMINAL.get(),
                new EmiEncodePatternHandler<>(ExpEncodingTerminalMenu.class)
        );
        registry.addRecipeHandler(
                ExpWETMenu.TYPE,
                new EmiEncodePatternHandler<>(ExpWETMenu.class)
        );
    }
}
