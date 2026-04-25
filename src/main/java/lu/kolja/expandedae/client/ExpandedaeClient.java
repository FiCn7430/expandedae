package lu.kolja.expandedae.client;

import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.render.crafting.CraftingCubeModel;
import appeng.hooks.BuiltInModelHooks;
import appeng.init.client.InitScreens;
import lu.kolja.expandedae.Expandedae;
import lu.kolja.expandedae.client.render.ExpCraftingUnitModelProvider;
import lu.kolja.expandedae.client.render.LaserBeamBER;
import lu.kolja.expandedae.client.render.OmniLaserBeamBER;
import lu.kolja.expandedae.definition.ExpBlockEntities;
import lu.kolja.expandedae.definition.ExpMenus;
import lu.kolja.expandedae.enums.ExpTiers;
import lu.kolja.expandedae.menu.ExpPatternProviderMenu;
import lu.kolja.expandedae.screen.ExpIOPortScreen;
import lu.kolja.expandedae.terminal.ExpEncodingTerminalMenu;
import lu.kolja.expandedae.terminal.ExpEncodingTerminalScreen;
import lu.kolja.expandedae.terminal.wtlib.ExpWETScreen;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = Expandedae.MODID, dist = Dist.CLIENT)
public class ExpandedaeClient {
    public ExpandedaeClient(IEventBus modEventBus) {
        initCraftingUnitModels();
        modEventBus.addListener(ExpandedaeClient::initScreens);
        modEventBus.addListener(ExpandedaeClient::initRenderers);
    }

    private static void initRenderers(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BlockEntityRenderers.register(ExpBlockEntities.LASER_BEAM_BE.get(), ctx -> new LaserBeamBER(ctx));
            BlockEntityRenderers.register(ExpBlockEntities.OMNI_LASER_BEAM_BE.get(), ctx -> new OmniLaserBeamBER(ctx));
        });
    }

    private static void initCraftingUnitModels() {
        for (var tier : ExpTiers.values()) {
            var affix = tier.isCPU() ? tier.getCpuAffix() : tier.getAffix();
            BuiltInModelHooks.addBuiltInModel(
                    Expandedae.makeId("block/crafting/" + affix + "_formed"),
                    new CraftingCubeModel(new ExpCraftingUnitModelProvider(tier))
            );
        }
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
                ExpEncodingTerminalScreen<ExpEncodingTerminalMenu>::new,
                "/screens/terminals/exp_encoding_terminal.json"
        );
        InitScreens.register(
                event,
                ExpMenus.EXP_IO_PORT.get(),
                ExpIOPortScreen::new,
                "/screens/exp_io_port.json"
        );
        ExpWETScreen.register(event);
    }
}
