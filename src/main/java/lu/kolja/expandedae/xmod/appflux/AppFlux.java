package lu.kolja.expandedae.xmod.appflux;

import appeng.api.upgrades.Upgrades;
import com.glodblock.github.appflux.common.AFSingletons;
import lu.kolja.expandedae.definition.ExpBlocks;
import lu.kolja.expandedae.definition.ExpItems;

public class AppFlux {
    public AppFlux() {
        Upgrades.add(AFSingletons.INDUCTION_CARD, ExpItems.EXP_PATTERN_PROVIDER_PART, 1, "group.exp_pattern_provider.name");
        Upgrades.add(AFSingletons.INDUCTION_CARD, ExpBlocks.EXP_PATTERN_PROVIDER, 1, "group.exp_pattern_provider.name");
    }
}
