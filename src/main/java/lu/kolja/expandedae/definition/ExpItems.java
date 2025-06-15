package lu.kolja.expandedae.definition;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartModels;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import lu.kolja.expandedae.Expandedae;
import lu.kolja.expandedae.item.cards.*;
import lu.kolja.expandedae.item.misc.ExpPatternProviderUpgradeItem;
import lu.kolja.expandedae.item.misc.ExtPatternProviderUpgradeItem;
import lu.kolja.expandedae.item.part.ExpEncodingTerminalPartItem;
import lu.kolja.expandedae.item.part.ExpPatternProviderPartItem;
import lu.kolja.expandedae.part.ExpPatternProviderPart;
import lu.kolja.expandedae.terminal.ExpEncodingTerminalPart;
import lu.kolja.expandedae.xmod.ae2wtlib.WTLibIntegration;
import net.minecraft.Util;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings("ALL")
public class ExpItems {

    public static final DeferredRegister.Items DR =
            DeferredRegister.createItems(Expandedae.MODID);

    private static final List<ItemDefinition<?>> ITEMS = new ArrayList<>();

    public static final ItemDefinition<ExpPatternProviderPartItem> EXP_PATTERN_PROVIDER_PART = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(ExpPatternProviderPart.class));
        return item("Expanded Pattern Provider", "exp_pattern_provider_part", ExpPatternProviderPartItem::new);
    });

    public static final ItemDefinition<ExpEncodingTerminalPartItem> EXP_ENCODING_TERMINAL = Util.make(() -> {
        PartModels.registerModels(PartModelsHelper.createModels(ExpEncodingTerminalPart.class));
        return item("Expanded Pattern Encoding Terminal", "exp_encoding_terminal", ExpEncodingTerminalPartItem::new);
    });

    public static final ItemDefinition<ExpPatternProviderUpgradeItem> EXP_PATTERN_PROVIDER_UPGRADE = item(
            "Expanded Pattern Provider Upgrade",
            "exp_pattern_provider_upgrade",
            ExpPatternProviderUpgradeItem::new
    );

    public static final ItemDefinition<ExtPatternProviderUpgradeItem> EXT_PATTERN_PROVIDER_UPGRADE = item(
            "Extended Pattern Provider Upgrader",
            "ext_pattern_provider_upgrader",
            ExtPatternProviderUpgradeItem::new
    );

    public static final ItemDefinition<ItemAutoCompleteCard> AUTO_COMPLETE_CARD = item(
            "Auto Complete Card",
            "auto_complete_card",
            ItemAutoCompleteCard::new
    );

    public static final ItemDefinition<ItemAdvancedBlockingCard> ADVANCED_BLOCKING_CARD = item(
            "Advanced Blocking Card",
            "advanced_blocking_card",
            ItemAdvancedBlockingCard::new
    );

    public static final ItemDefinition<ItemSmartBlockingCard> SMART_BLOCKING_CARD = item(
            "Smart Blocking Card",
            "smart_blocking_card",
            ItemSmartBlockingCard::new
    );

    public static final ItemDefinition<Item> WIRELESS_EXP_ENCODING_TERMINAL = item("Wireless Expanded Pattern Encoding Terminal", "wireless_exp_encoding_terminal", p -> WTLibIntegration.TERMINAL);

    public static final ItemDefinition<ItemStickyCard> STICKY_CARD = item(
            "Sticky Card",
            "sticky_card",
            ItemStickyCard::new
    );

    public static final ItemDefinition<ItemPatternRefillerCard> PATTERN_REFILLER_CARD = item(
            "Pattern Refiller Card",
            "pattern_refiller_card",
            ItemPatternRefillerCard::new
    );

    public static List<ItemDefinition<?>> getItems() {
        return Collections.unmodifiableList(ITEMS);
    }

    public static <T extends IPart> ItemDefinition<PartItem<T>> part(
            String englishName, String id, Class<T> partClass, Function<IPartItem<T>, T> factory) {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        return item(englishName, id, p -> new PartItem<>(p, partClass, factory));
    }

    private static <T extends Item> ItemDefinition<T> item(
            String englishName, String id, Function<Item.Properties, T> factory) {
        var definition = new ItemDefinition<>(englishName, DR.registerItem(id, factory));
        ITEMS.add(definition);
        return definition;
    }

    private static <T extends Item> ItemDefinition<T> altItem(
            String englishName, String id, Function<Item.Properties, T> factory) {
        var definition = new ItemDefinition<>(englishName, DR.registerItem(id, factory));
        ITEMS.add(definition);
        return definition;
    }

    public static void orderInit() {}
}
