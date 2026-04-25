package lu.kolja.expandedae.definition;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartModels;
import appeng.core.definitions.ItemDefinition;
import appeng.items.materials.UpgradeCardItem;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import lu.kolja.expandedae.Expandedae;
import lu.kolja.expandedae.item.LaserBindingTool;
import lu.kolja.expandedae.item.misc.ExpPatternProviderUpgradeItem;
import lu.kolja.expandedae.part.LaserBeamPart;
import lu.kolja.expandedae.part.ExpPatternProviderPart;
import lu.kolja.expandedae.terminal.ExpEncodingTerminalPart;
import lu.kolja.expandedae.xmod.ae2wtlib.WTLibIntegration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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

    public static final ItemDefinition<PartItem<ExpPatternProviderPart>> EXP_PATTERN_PROVIDER_PART = part(
            "Expanded Pattern Provider",
            "exp_pattern_provider_part",
            ExpPatternProviderPart.class,
            ExpPatternProviderPart::new
    );

    public static final ItemDefinition<PartItem<ExpEncodingTerminalPart>> EXP_ENCODING_TERMINAL = part(
            "Expanded Pattern Encoding Terminal",
            "exp_encoding_terminal",
            ExpEncodingTerminalPart.class,
            ExpEncodingTerminalPart::new
    );

    public static final ItemDefinition<ExpPatternProviderUpgradeItem> EXP_PATTERN_PROVIDER_UPGRADE = item(
            "Expanded Pattern Provider Upgrade",
            "exp_pattern_provider_upgrade",
            ExpPatternProviderUpgradeItem::new
    );

    public static final ItemDefinition<UpgradeCardItem> AUTO_COMPLETE_CARD = item(
            "Auto Complete Card",
            "auto_complete_card",
            p -> new UpgradeCardItem(p) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag advancedTooltips) {
                    tooltip.add(Component.translatable("item.expandedae.auto_complete_card.tooltip.1").withStyle(ChatFormatting.GRAY));
                    tooltip.add(Component.translatable("item.expandedae.auto_complete_card.tooltip.2").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.RED));
                    super.appendHoverText(stack, context, tooltip, advancedTooltips);
                }
            }
    );

    public static final ItemDefinition<UpgradeCardItem> PATTERN_REFILLER_CARD = item(
            "Pattern Refiller Card",
            "pattern_refiller_card",
            p -> new UpgradeCardItem(p) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag advancedTooltips) {
                    tooltip.add(Component.translatable("item.expandedae.pattern_refiller_card.tooltip.1").withStyle(ChatFormatting.GRAY));
                    super.appendHoverText(stack, context, tooltip, advancedTooltips);
                }
            }
    );

    public static final ItemDefinition<UpgradeCardItem> GREATER_ACCEL_CARD = item(
            "Greater Acceleration Card",
            "greater_accel_card",
            p -> new UpgradeCardItem(p) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag advancedTooltips) {
                    tooltip.add(Component.translatable("item.expandedae.greater_accel_card.tooltip.1").withStyle(ChatFormatting.GRAY));
                    tooltip.add(Component.translatable("item.expandedae.greater_accel_card.tooltip.2").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.RED));
                    super.appendHoverText(stack, context, tooltip, advancedTooltips);
                }
            }
    );

    public static final ItemDefinition<Item> WIRELESS_EXP_ENCODING_TERMINAL = item("Wireless Expanded Pattern Encoding Terminal",
            "wireless_exp_encoding_terminal",
            p -> WTLibIntegration.TERMINAL
    );

    public static final ItemDefinition<LaserBindingTool> LASER_BINDING_TOOL = item(
            "Laser Binding Tool",
            "laser_binding_tool",
            p -> new LaserBindingTool(p.stacksTo(1))
    );

    public static final ItemDefinition<PartItem<LaserBeamPart>> LASER_BEAM_PART = part(
            "ME Laser Beam Former Part",
            "laser_beam_part",
            LaserBeamPart.class,
            LaserBeamPart::new
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
