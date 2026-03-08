package lu.kolja.expandedae;

import appeng.api.client.StorageCellModels;
import appeng.api.features.GridLinkables;
import appeng.api.storage.StorageCells;
import appeng.init.client.InitBlockEntityRenderers;
import appeng.init.client.InitItemModelsProperties;
import com.mojang.logging.LogUtils;
import lu.kolja.expandedae.cell.art.ArtUniverseCellHandler;
import lu.kolja.expandedae.cell.dual.DualCellHandler;
import lu.kolja.expandedae.client.ExpCellModels;
import lu.kolja.expandedae.client.ExpandedaeClient;
import lu.kolja.expandedae.client.render.ExpBuiltinModels;
import lu.kolja.expandedae.client.render.ExpItemModelProperties;
import lu.kolja.expandedae.datagen.conditionals.ModNotLoadedCondition;
import lu.kolja.expandedae.definition.*;
import lu.kolja.expandedae.item.misc.InfinityColorApplicatorItem;
import lu.kolja.expandedae.network.ExpNetworkHandler;
import lu.kolja.expandedae.xmod.XMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@Mod(Expandedae.MODID)
public class Expandedae {
    public static final String MODID = "expandedae";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Contract("_ -> new")
    public static @NotNull ResourceLocation makeId(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    //getActionableNode().getGrid().getStorageService().getInventory().insert() TODO IMPLEMENT TO STICKY CARD

    public Expandedae(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ExpBlocks.init();
        ExpItems.init();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener((RegisterEvent event) -> {
            event.register(ForgeRegistries.Keys.RECIPE_SERIALIZERS,
                    helper -> CraftingHelper.register(ModNotLoadedCondition.Serializer.INSTANCE)
            );
            if (event.getRegistryKey().equals(Registries.BLOCK)) {
                ExpBlocks.getBlocks().forEach(def -> {
                    ForgeRegistries.BLOCKS.register(def.id(), def.block());
                    ForgeRegistries.ITEMS.register(def.id(), def.asItem());
                });
            }
            if (event.getRegistryKey().equals(Registries.ITEM)) {
                ExpItems.getItems().forEach(i -> ForgeRegistries.ITEMS.register(i.id(), i.asItem()));
            }
            if (event.getRegistryKey().equals(Registries.BLOCK_ENTITY_TYPE)) {
                ExpBlockEntities.getBlockEntityTypes().forEach(ForgeRegistries.BLOCK_ENTITY_TYPES::register);
            }
            if (event.getRegistryKey().equals(Registries.MENU)) {
                ExpMenus.getMenuTypes().forEach(ForgeRegistries.MENU_TYPES::register);
            }
        });
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.register(ExpandedaeClient.INSTANCE);
            modEventBus.addListener(this::registerItemColors);
            modEventBus.addListener(this::modelRegistryEvent);
            ExpBuiltinModels.init();
        }
        context.registerConfig(ModConfig.Type.COMMON, ExpConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        new XMod();
        new ExpUpgrades(event);
        event.enqueueWork(() -> {
            ExpNetworkHandler.registerPackets();
            StorageCells.addCellHandler(DualCellHandler.INSTANCE);
            StorageCells.addCellHandler(ArtUniverseCellHandler.INSTANCE);
            for (var cellModel : ExpCellModels.cellModels.object2ObjectEntrySet()) {
                StorageCellModels.registerModel(cellModel.getKey(), cellModel.getValue());
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
                (stack, tintIndex) -> {
                    if (tintIndex == 0) return -1;
                    var color = ((InfinityColorApplicatorItem) (stack.getItem())).getActiveColor(stack);
                    return switch (tintIndex) {
                        case 1 -> color.blackVariant;
                        case 2 -> color.mediumVariant;
                        case 3 -> color.whiteVariant;
                        default -> -1;
                    };
                }
        );
    }

    @OnlyIn(Dist.CLIENT)
    public void modelRegistryEvent(ModelEvent.RegisterGeometryLoaders event) {
        ExpItemModelProperties.init();
    }
}