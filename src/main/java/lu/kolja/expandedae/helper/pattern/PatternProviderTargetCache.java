package lu.kolja.expandedae.helper.pattern;

import appeng.api.AECapabilities;
import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.MEStorage;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.me.storage.CompositeStorage;
import appeng.parts.automation.StackWorldBehaviors;
import appeng.util.ConfigManager;
import lu.kolja.expandedae.definition.ExpSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Most code is borrowed from ae2
 */
public class PatternProviderTargetCache {
    private final ResourceLocation programmedCircuit = ResourceLocation.fromNamespaceAndPath("gtceu", "programmed_circuit");

    private final BlockCapabilityCache<MEStorage, Direction> cache;
    private final IActionSource src;
    private final Map<AEKeyType, ExternalStorageStrategy> strategies;
    private final ConfigManager configManager;

    public PatternProviderTargetCache(ServerLevel l, BlockPos pos, Direction direction, IActionSource src, ConfigManager configManager) {
        this.cache = BlockCapabilityCache.create(AECapabilities.ME_STORAGE, l, pos, direction);
        this.src = src;
        this.strategies = StackWorldBehaviors.createExternalStorageStrategies(l, pos, direction);
        this.configManager = configManager;
    }

    @Nullable
    public PatternProviderTarget find() {
        // our capability first: allows any storage channel
        var meStorage = cache.getCapability();
        if (meStorage != null) {
            return wrapMeStorage(meStorage);
        }

        // otherwise fall back to the platform capability
        var externalStorages = new IdentityHashMap<AEKeyType, MEStorage>(2);
        for (var entry : strategies.entrySet()) {
            var wrapper = entry.getValue().createWrapper(false, () -> {
            });
            if (wrapper != null) {
                externalStorages.put(entry.getKey(), wrapper);
            }
        }

        if (!externalStorages.isEmpty()) {
            return wrapMeStorage(new CompositeStorage(externalStorages));
        }

        return null;
    }

    private PatternProviderTarget wrapMeStorage(MEStorage storage) {
        return new PatternProviderTarget() {
            @Override
            public long insert(AEKey what, long amount, Actionable type) {
                return storage.insert(what, amount, type, src);
            }

            @Override
            public boolean containsPatternInput(Set<AEKey> patternInputs) {
                switch (configManager.getSetting(ExpSettings.BLOCKING_MODE)) {
                    case ALL -> {
                        for (var stack : storage.getAvailableStacks()) {
                            if (stack.getKey().getId().equals(programmedCircuit)) continue;
                            return true;
                        }
                    }
                    case DEFAULT -> {
                        for (var stack : storage.getAvailableStacks()) {
                            if (stack.getKey().getId().equals(programmedCircuit)) continue;
                            if (patternInputs.contains(stack.getKey().dropSecondary())) return true;
                        }
                    }
                    case SMART -> {
                        for (var stack : storage.getAvailableStacks()) {
                            if (stack.getKey().getId().equals(programmedCircuit)) continue;
                            if (!patternInputs.contains(stack.getKey().dropSecondary())) return true;
                        }
                    }
                }
                return false;
            }
        };
    }
}