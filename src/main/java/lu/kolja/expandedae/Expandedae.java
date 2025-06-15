package lu.kolja.expandedae;

import appeng.api.AECapabilities;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.parts.RegisterPartCapabilitiesEvent;
import appeng.items.tools.powered.powersink.PoweredItemCapabilities;
import com.mojang.logging.LogUtils;
import gripe._90.megacells.definition.MEGABlockEntities;
import gripe._90.megacells.definition.MEGAItems;
import gripe._90.megacells.item.part.CellDockPart;
import gripe._90.megacells.item.part.MEGAInterfacePart;
import gripe._90.megacells.item.part.MEGAPatternProviderPart;
import lu.kolja.expandedae.definition.*;
import lu.kolja.expandedae.part.ExpPatternProviderPart;
import lu.kolja.expandedae.xmod.XMod;
import lu.kolja.expandedae.xmod.ae2wtlib.WTLibIntegration;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@Mod(Expandedae.MODID)
public class Expandedae {

    public static final String MODID = "expandedae";
    public static final Logger LOGGER = LogUtils.getLogger();

    //getActionableNode().getGrid().getStorageService().getInventory().insert() TODO IMPLEMENT TO STICKY CARD

    public Expandedae(IEventBus modEventBus, ModContainer container) {
        ExpItems.DR.register(modEventBus);
        ExpBlocks.DR.register(modEventBus);
        ExpBlockEntities.DR.register(modEventBus);
        ExpMenus.DR.register(modEventBus);
        ExpCreativeTab.DR.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::initCapabilities);
        modEventBus.addListener(this::initPartCapabilities);
        if (ModList.get().isLoaded("ae2wtlib")) {
            modEventBus.addListener(WTLibIntegration::registerMenu);
        }
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation makeId(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        new XMod();
        new ExpUpgrades(event);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void initCapabilities(RegisterCapabilitiesEvent event) {
        for (var type : ExpBlockEntities.DR.getEntries()) {
            event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, type.get(), (be, context) -> (IInWorldGridNodeHost) be);
        }
        event.registerBlockEntity(
                AECapabilities.GENERIC_INTERNAL_INV,
                ExpBlockEntities.EXP_PATTERN_PROVIDER.get(),
                (be, context) -> be.getLogic().getReturnInv()
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    private void initPartCapabilities(RegisterPartCapabilitiesEvent event) {
        event.register(
                AECapabilities.GENERIC_INTERNAL_INV,
                (part, context) -> part.getLogic().getReturnInv(),
                ExpPatternProviderPart.class
        );
    }
}