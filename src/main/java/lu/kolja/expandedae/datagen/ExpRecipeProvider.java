package lu.kolja.expandedae.datagen;

import lu.kolja.expandedae.Expandedae;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static appeng.core.definitions.AEItems.*;
import static appeng.core.definitions.AEParts.PATTERN_ENCODING_TERMINAL;
import static appeng.core.definitions.AEParts.PATTERN_PROVIDER;
import static lu.kolja.expandedae.definition.ExpBlocks.EXP_PATTERN_PROVIDER;
import static lu.kolja.expandedae.definition.ExpItems.*;
import static net.minecraft.data.recipes.RecipeCategory.MISC;

public class ExpRecipeProvider extends RecipeProvider {
    public ExpRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput out) {
        ShapedRecipeBuilder.shaped(MISC, EXP_PATTERN_PROVIDER)
                .pattern("ECE")
                .pattern("CPC")
                .pattern("ECE")
                .define('C', CAPACITY_CARD)
                .define('E', ENGINEERING_PROCESSOR)
                .define('P', PATTERN_PROVIDER)
                .unlockedBy("has_engineering_processor", has(ENGINEERING_PROCESSOR))
                .unlockedBy("has_pattern_provider", has(PATTERN_PROVIDER))
                .save(out, craftingId("exp_pattern_provider"));
        ShapelessRecipeBuilder.shapeless(MISC, EXP_PATTERN_PROVIDER)
                .requires(EXP_PATTERN_PROVIDER_PART)
                .unlockedBy("has_exp_pattern_provider_part", has(EXP_PATTERN_PROVIDER_PART))
                .save(out, craftingId("exp_pattern_provider_alt"));
        ShapelessRecipeBuilder.shapeless(MISC, EXP_PATTERN_PROVIDER_PART)
                .requires(EXP_PATTERN_PROVIDER)
                .unlockedBy("has_exp_pattern_provider", has(EXP_PATTERN_PROVIDER))
                .save(out, craftingId("exp_pattern_provider_part"));

        ShapedRecipeBuilder.shaped(MISC, EXP_PATTERN_PROVIDER_UPGRADE)
                .pattern("ECE")
                .pattern("C C")
                .pattern("ECE")
                .define('C', CAPACITY_CARD)
                .define('E', ENGINEERING_PROCESSOR)
                .unlockedBy("has_engineering_processor", has(ENGINEERING_PROCESSOR))
                .unlockedBy("has_capacity_card", has(CAPACITY_CARD))
                .save(out, craftingId("exp_pattern_provider_upgrade"));

        ShapelessRecipeBuilder.shapeless(MISC, AUTO_COMPLETE_CARD)
                .requires(ADVANCED_CARD)
                .requires(CRAFTING_CARD)
                .unlockedBy("has_advanced_card", has(ADVANCED_CARD))
                .unlockedBy("has_crafting_card", has(CRAFTING_CARD))
                .save(out, craftingId("auto_complete_card"));

        ShapedRecipeBuilder.shaped(MISC, PATTERN_REFILLER_CARD)
                .pattern("AB")
                .pattern("BE")
                .define('A', ADVANCED_CARD)
                .define('B', BLANK_PATTERN)
                .define('E', ENGINEERING_PROCESSOR)
                .unlockedBy("has_advanced_card", has(ADVANCED_CARD))
                .unlockedBy("has_engineering_processor", has(ENGINEERING_PROCESSOR))
                .save(out, craftingId("pattern_refiller_card"));

        ShapedRecipeBuilder.shaped(MISC, EXP_ENCODING_TERMINAL)
                .pattern("TC")
                .pattern("L ")
                .define('T', PATTERN_ENCODING_TERMINAL)
                .define('C', CALCULATION_PROCESSOR)
                .define('L', LOGIC_PROCESSOR)
                .unlockedBy("has_calculation_processor", has(CALCULATION_PROCESSOR))
                .unlockedBy("has_logic_processor", has(LOGIC_PROCESSOR))
                .save(out, craftingId("exp_encoding_terminal"));

    }

    private ResourceLocation craftingId(String name) {
        return Expandedae.makeId("crafting/" + name);
    }
}
