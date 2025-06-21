package lu.kolja.expandedae.xmod;

import lu.kolja.expandedae.xmod.advancedae.AdvancedAE;
import lu.kolja.expandedae.xmod.appflux.AppFlux;
import lu.kolja.expandedae.xmod.extendedae.ExtendedAE;
import lu.kolja.expandedae.xmod.megacells.MegaCells;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;

public class XMod {
    public enum ADDONS {
        EXT("extendedae"),
        MEGA("megacells"),
        APPFLUX("appflux"),
        ADV("advancedae"),
        APPMEK("appmek"),
        ARSENG("arseng"),
        APPEX("appex");

        private final String mod;
        ADDONS(String mod) { this.mod = mod; }
        boolean isLoaded() { return ModList.get().isLoaded(mod); }
        public Component getUnavailableTooltip() {
            return Component.literal("Mod not installed!").withStyle(ChatFormatting.DARK_RED);
        }
    }


    public XMod() {
        for (var mod : ADDONS.values()) {
            if (!mod.isLoaded()) continue;
            switch (mod) {
                case EXT -> new ExtendedAE();
                case MEGA -> new MegaCells();
                case APPFLUX -> new AppFlux();
                case ADV -> new AdvancedAE();
            }
        }
    }
}
