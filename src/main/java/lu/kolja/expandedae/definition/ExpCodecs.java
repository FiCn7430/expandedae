package lu.kolja.expandedae.definition;

import com.mojang.serialization.MapCodec;
import lu.kolja.expandedae.Expandedae;
import lu.kolja.expandedae.datagen.conditionals.ModNotLoadedCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ExpCodecs {
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONAL_CODECS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, Expandedae.MODID);

    public static final Supplier<MapCodec<ModNotLoadedCondition>> NOT_LOADED =
            CONDITIONAL_CODECS.register("not_loaded", () -> ModNotLoadedCondition.CODEC);
}
