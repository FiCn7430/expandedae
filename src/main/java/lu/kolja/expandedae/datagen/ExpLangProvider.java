package lu.kolja.expandedae.datagen;

import lu.kolja.expandedae.Expandedae;
import lu.kolja.expandedae.definition.ExpBlocks;
import lu.kolja.expandedae.definition.ExpItems;
import lu.kolja.expandedae.definition.ExpLang;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.jetbrains.annotations.NotNull;

public class ExpLangProvider extends LanguageProvider {
    public ExpLangProvider(PackOutput packOutput) {
        super(packOutput, Expandedae.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        for (var item : ExpItems.getItems()) {
            add(item.asItem(), item.getEnglishName());
        }

        for (var block : ExpBlocks.getBlocks()) {
            add(block.block(), block.getEnglishName());
        }

        for (var entry : ExpLang.values()) {
            add(entry.getTranslationKey(), entry.getEnglishText());
        }
    }

    @NotNull
    @Override
    public String getName() {
        return "Language";
    }
}
