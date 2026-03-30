package lu.kolja.expandedae.mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {

    /**
     * If mod b is loaded, don't load class A
     */
    public static final Object2ObjectMap<String, String> mixinMap = new Object2ObjectOpenHashMap<>(
        new String[]{
                "lu.kolja.expandedae.mixin.patternprovider.MixinPatternProviderLogic",
                "lu.kolja.expandedae.mixin.patternprovider.MixinPatternProviderMenu",
                "lu.kolja.expandedae.mixin.patternprovider.MixinPatternProviderScreen",
                // 当 AppFlux 加载时，AdvancedAE 自身会添加升级槽支持，所以我们不需要重复添加
                "lu.kolja.expandedae.mixin.compat.advancedae.MixinAdvPatternProviderLogicUpgrades"
        },
        new String[]{
                "appflux",
                "appflux",
                "appflux",
                "appflux"  // 当 AppFlux 加载时，禁用我们的升级槽 Mixin
        }
    );

    /**
     * If mod b is loaded, do load class A
     */
    public static final Object2ObjectMap<String, String> mixinMap2 = new Object2ObjectOpenHashMap<>(
            new String[]{
                    "lu.kolja.expandedae.mixin.compat.appflux.AppFluxMixinPatternProviderLogic",
                    "lu.kolja.expandedae.mixin.compat.appflux.AppFluxMixinPatternProviderMenu",
                    "lu.kolja.expandedae.mixin.compat.appflux.AppFluxMixinPatternProviderScreen",
                    "lu.kolja.expandedae.mixin.compat.advancedae.AAEAccessorAdvCraftingCPULogic",
                    "lu.kolja.expandedae.mixin.compat.advancedae.AAEAccessorExecutingCraftingJob",
                    "lu.kolja.expandedae.mixin.compat.advancedae.AAEAccessorExecutingCraftingJob.AAEAccessorTaskProgress",
                    "lu.kolja.expandedae.mixin.compat.advancedae.MixinAdvPatternProviderLogic",
                    "lu.kolja.expandedae.mixin.compat.advancedae.MixinAdvPatternProviderMenu",
                    "lu.kolja.expandedae.mixin.compat.advancedae.MixinAdvPatternProviderMenuIPatternProvider",
                    "lu.kolja.expandedae.mixin.compat.advancedae.MixinAdvPatternProviderScreen",
                    "lu.kolja.expandedae.mixin.compat.jei.MixinJEIPlugin"
            },
            new String[]{
                    "appflux",
                    "appflux",
                    "appflux",
                    "advanced_ae",
                    "advanced_ae",
                    "advanced_ae",
                    "advanced_ae",
                    "advanced_ae",
                    "advanced_ae",
                    "advanced_ae",
                    "ae2jeiintegration"
            }
    );

    /**
     * Safely checks if a mod is loaded, working even during early loading phases
     * when ModList may not be initialized yet.
     *
     * @param modId The mod ID to check
     * @return true if the mod is loaded or loading, false otherwise
     */
    private static boolean isModLoaded(String modId) {
        if (ModList.get() == null) {
            return LoadingModList.get().getMods().stream()
                    .map(ModInfo::getModId)
                    .anyMatch(modId::equals);
        } else {
            return ModList.get().isLoaded(modId);
        }
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    /*
     * Kinda hacky, but it works
     */
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinMap.containsKey(mixinClassName)) return !isModLoaded(mixinMap.get(mixinClassName));
        if (mixinMap2.containsKey(mixinClassName)) return isModLoaded(mixinMap2.get(mixinClassName));
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() { return null; }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
