package lu.kolja.expandedae.datagen.model;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.init.client.InitItemModelsProperties;
import lu.kolja.expandedae.Expandedae;
import lu.kolja.expandedae.client.ExpCellModels;
import lu.kolja.expandedae.definition.ExpItems;
import lu.kolja.expandedae.xmod.extendedae.ExtendedAE;
import lu.kolja.expandedae.xmod.megacells.MegaCells;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import static lu.kolja.expandedae.definition.ExpItems.*;
import static lu.kolja.expandedae.definition.ExpItems.GREATER_ACCEL_CARD;

public class ExpItemModelProvider extends ItemModelProvider {
    public ExpItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Expandedae.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (var cell : ExpItems.getCells().entrySet()) {
            storageCell(cell.getKey(), "item/dual_storage_cell_" + cell.getValue());
        }
        for (var cell : ExpCellModels.cellModels.object2ObjectEntrySet()) {
            driveCell(cell.getValue());
        }

        // General
        basicItem(EXP_PATTERN_PROVIDER_UPGRADE.asItem());
        basicItem(MegaCells.MEGA_PATTERN_PROVIDER_UPGRADE.asItem());
        basicItem(ExtendedAE.EXT_PATTERN_PROVIDER_UPGRADE.asItem());
        // CARDS
        basicItem(AUTO_COMPLETE_CARD.asItem());
        basicItem(PATTERN_REFILLER_CARD.asItem());
        basicItem(GREATER_ACCEL_CARD.asItem());
        basicItem(ExpItems.DUAL_CELL_HOUSING.asItem());
        basicItem(MegaCells.DUAL_CELL_MEGA_HOUSING.asItem());
        basicItem(ExpItems.PRIORITY_CARD.asItem());

        var colorApplicator = withExistingParent(INFINITY_COLOR_APPLICATOR.id().getPath() + "_colored", "item/generated")
                .texture("layer0", AppEng.makeId("item/color_applicator"))
                .texture("layer1", AppEng.makeId("item/color_applicator_tip_dark"))
                .texture("layer2", AppEng.makeId("item/color_applicator_tip_medium"))
                .texture("layer3", AppEng.makeId("item/color_applicator_tip_bright"));

        withExistingParent(INFINITY_COLOR_APPLICATOR.id().getPath(), "item/generated")
                .texture("layer0", AppEng.makeId("item/color_applicator"))
                // Use different model when colored
                .override()
                .predicate(InitItemModelsProperties.COLORED_PREDICATE_ID, 1)
                .model(colorApplicator)
                .end();
    }

    private void storageCell(ItemDefinition<?> item, String background) {
        singleTexture(
                item.id().getPath(),
                mcLoc("item/generated"),
                "layer0",
                Expandedae.makeId(background)
        ).texture("layer1", "ae2:item/storage_cell_led");
    }

    private void driveCell(ResourceLocation texture) {
        var loc = Expandedae.makeId("block/drive/cells/" + (texture.toString().endsWith("m") ? "mega_" : "") + "dual_cell");
        singleTexture(
                texture.toString(),
                AppEng.makeId("block/drive/drive_cell"),
                "cell",
                loc
        );
    }
}
