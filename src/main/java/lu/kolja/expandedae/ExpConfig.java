package lu.kolja.expandedae;

import lu.kolja.expandedae.helper.misc.Maths;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = Expandedae.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ExpConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    //private static final ModConfigSpec.IntValue IDLE_DRAIN = BUILDER.comment("Artificial Universe Cell idle drain: how many AE/t it uses passively.").defineInRange("idle_drain", 512, 1, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue MAX_THREADS = BUILDER
            .comment("Up to which exponent of 2 should co-processors be created")
            .comment("For example, at the default value of 20 it would enable co-processors with threads from 2^1 up to 2^20")
            .comment("Set to 0 to disable")
            .defineInRange("max_threads", 20, 0, 20);

    //public static int idleDrain;
    public static int maxThreadsPow;
    public static int maxThreads = Maths.pow(2, maxThreadsPow);

    public static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) return;
        //idleDrain = IDLE_DRAIN.get();
        maxThreadsPow = MAX_THREADS.get();
    }
}
