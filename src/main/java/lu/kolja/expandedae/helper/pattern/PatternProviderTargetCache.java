package lu.kolja.expandedae.helper.pattern;

import appeng.api.AECapabilities;
import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.MEStorage;
import appeng.me.storage.CompositeStorage;
import appeng.parts.automation.StackWorldBehaviors;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class PatternProviderTargetCache {
    private final BlockCapabilityCache<MEStorage, Direction> cache;
    private final IActionSource src;
    private final Map<AEKeyType, ExternalStorageStrategy> strategies;

    public PatternProviderTargetCache(ServerLevel l, BlockPos pos, Direction direction, IActionSource src) {
        this.cache = BlockCapabilityCache.create(AECapabilities.ME_STORAGE, l, pos, direction);
        this.src = src;
        this.strategies = StackWorldBehaviors.createExternalStorageStrategies(l, pos, direction);
    }

    @Nullable
    public PatternProviderTarget find() {
        MEStorage meStorage = this.cache.getCapability();
        if (meStorage != null) {
            return this.wrapMeStorage(meStorage);
        } else {
            IdentityHashMap<AEKeyType, MEStorage> externalStorages = new IdentityHashMap(2);

            for(Map.Entry<AEKeyType, ExternalStorageStrategy> entry : this.strategies.entrySet()) {
                MEStorage wrapper = entry.getValue().createWrapper(false, () -> {
                });
                if (wrapper != null) {
                    externalStorages.put(entry.getKey(), wrapper);
                }
            }

            if (!externalStorages.isEmpty()) {
                return this.wrapMeStorage(new CompositeStorage(externalStorages));
            } else {
                return null;
            }
        }
    }

    private PatternProviderTarget wrapMeStorage(final MEStorage storage) {
        return new PatternProviderTarget() {
            public long insert(AEKey what, long amount, Actionable type) {
                return storage.insert(what, amount, type, PatternProviderTargetCache.this.src);
            }

            public boolean containsPatternInput(Set<AEKey> patternInputs) {
                for(Object2LongMap.Entry<AEKey> stack : storage.getAvailableStacks()) {
                    if (patternInputs.contains(stack.getKey().dropSecondary())) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean onlyHasPatternInput(Set<AEKey> patternInputs) {
                for (var stack : storage.getAvailableStacks()) {
                    if (patternInputs.contains(stack.getKey().dropSecondary())) continue;
                    return false;
                }
                return true;
            }

            public MEStorage getStorage() {
                return storage;
            }
        };
    }
}