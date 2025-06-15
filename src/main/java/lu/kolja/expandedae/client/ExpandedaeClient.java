package lu.kolja.expandedae.client;

import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.init.client.InitScreens;
import lu.kolja.expandedae.Expandedae;
import lu.kolja.expandedae.definition.ExpMenus;
import lu.kolja.expandedae.menu.ExpPatternProviderMenu;
import lu.kolja.expandedae.terminal.ExpEncodingTerminalMenu;
import lu.kolja.expandedae.terminal.wtlib.ExpWETScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = Expandedae.MODID, dist = Dist.CLIENT)
public class ExpandedaeClient {
    public ExpandedaeClient(IEventBus modEventBus) {
        modEventBus.addListener(ExpandedaeClient::initScreens);
    }

    private static void initScreens(RegisterMenuScreensEvent event) {
        InitScreens.register(
                event,
                ExpMenus.EXP_PATTERN_PROVIDER.get(),
                PatternProviderScreen<ExpPatternProviderMenu>::new,
                "/screens/exp_pattern_provider.json"
        );
        InitScreens.register(
                event,
                ExpMenus.EXP_ENCODING_TERMINAL.get(),
                PatternEncodingTermScreen<ExpEncodingTerminalMenu>::new,
                "/screens/terminals/exp_encoding_terminal.json"
        );
        ExpWETScreen.register(event);
    }
}
