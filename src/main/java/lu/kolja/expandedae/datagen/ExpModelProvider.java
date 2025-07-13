package lu.kolja.expandedae.datagen;

import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.datagen.providers.models.AE2BlockStateProvider;
import lu.kolja.expandedae.Expandedae;
import lu.kolja.expandedae.enums.ExpTiers;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import static lu.kolja.expandedae.definition.ExpBlocks.EXP_IO_PORT;
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
}
