package lu.kolja.expandedae.datagen;

import appeng.api.util.AEColor;
import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.client.render.model.DriveModel;
import appeng.core.AppEng;
import appeng.datagen.providers.models.AE2BlockStateProvider;
import appeng.hooks.BuiltInModelHooks;
import java.util.function.Supplier;
import lu.kolja.expandedae.Expandedae;
import lu.kolja.expandedae.block.block.ColorableDriveBlock;
import lu.kolja.expandedae.definition.ExpBlocks;
import lu.kolja.expandedae.enums.ExpTiers;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import static lu.kolja.expandedae.definition.ExpItems.*;

public class ExpModelProvider extends AE2BlockStateProvider {
    public ExpModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Expandedae.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        basicItem(PATTERN_REFILLER_CARD);
        basicItem(AUTO_COMPLETE_CARD);
        basicItem(GREATER_ACCEL_CARD);
        basicItem(EXP_PATTERN_PROVIDER_UPGRADE);
        basicItem(WIRELESS_EXP_ENCODING_TERMINAL);
        addBuiltInModel("block/colorable_drive", DriveModel::new);

        for (var color : AEColor.values()) {
            var colorName = color.registryPrefix;
            var model = models().getBuilder("block/colorable_drive_" + colorName);
            getVariantBuilder(ExpBlocks.COLORABLE_DRIVE.block())
                    .partialState()
                    .with(ColorableDriveBlock.COLOR, color.ordinal())
                    .setModels(new ConfiguredModel(model));
        }

        for (var tier : ExpTiers.values()) {
            var block = tier.getDefinition().block();
            var name = tier.isCPU() ? tier.getCpuAffix() : tier.getAffix();
            var model = models().cubeAll("block/crafting/" + name, Expandedae.makeId("block/crafting/" + name));
            getVariantBuilder(block)
                    .partialState()
                    .with(AbstractCraftingUnitBlock.FORMED, false)
                    .setModels(new ConfiguredModel(model))
                    .partialState()
                    .with(AbstractCraftingUnitBlock.FORMED, true)
                    .setModels(new ConfiguredModel(models().getBuilder("block/crafting/" + name + "_formed")));
            simpleBlockItem(block, model);
        }
    }

    private void basicItem(ItemLike item) {
        itemModels().basicItem(item.asItem());
    }

    @NotNull
    @Override
    public String getName() {
        return "Block States / Models";
    }

    private BlockModelBuilder builtInBlockModel(String name) {
        return models().getBuilder("block/" + name);
    }

    private static <T extends UnbakedModel> void addBuiltInModel(String id,
                                                                 Supplier<T> modelFactory) {
        BuiltInModelHooks.addBuiltInModel(Expandedae.makeId(id), modelFactory.get());
    }
}
