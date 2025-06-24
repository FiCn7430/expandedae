package lu.kolja.expandedae.datagen.conditionals;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

public record ModNotLoadedCondition(String modId) implements ICondition {
    public static MapCodec<ModNotLoadedCondition> CODEC = RecordCodecBuilder.mapCodec(
            (builder) -> builder
                    .group(Codec.STRING.fieldOf("modid")
                    .forGetter(ModNotLoadedCondition::modId))
                    .apply(builder, ModNotLoadedCondition::new)
    );

    public boolean test(@NotNull ICondition.IContext context) {
        return !ModList.get().isLoaded(this.modId);
    }

    public @NotNull MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    public @NotNull String toString() {
        return "mod_not_loaded(\"" + this.modId + "\")";
    }
}
