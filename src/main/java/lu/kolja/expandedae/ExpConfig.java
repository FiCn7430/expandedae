package lu.kolja.expandedae;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = Expandedae.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ExpConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue IDLE_DRAIN = BUILDER.comment("Artificial Universe Cell idle drain: how many AE/t it uses passively.").defineInRange("idle_drain", 512, 1, Integer.MAX_VALUE);
    public static int idleDrain;

    public static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) return;
        idleDrain = IDLE_DRAIN.get();
    }
}
