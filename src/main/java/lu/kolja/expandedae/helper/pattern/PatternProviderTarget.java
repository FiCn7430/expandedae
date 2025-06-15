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
import com.google.common.util.concurrent.Runnables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public interface PatternProviderTarget {
    @javax.annotation.Nullable
    static PatternProviderTarget get(Level l, BlockPos pos, @Nullable BlockEntity be, Direction side, IActionSource src) {
        MEStorage storage;
        if (be != null) {
            storage = l.getCapability(AECapabilities.ME_STORAGE, be.getBlockPos(), be.getBlockState(), be, side);
        } else {
            storage = l.getCapability(AECapabilities.ME_STORAGE, pos, side);
        }

        if (storage != null) {
            return wrapMeStorage(storage, src);
        } else {
            Map<AEKeyType, ExternalStorageStrategy> strategies = StackWorldBehaviors.createExternalStorageStrategies((ServerLevel)l, pos, side);
            IdentityHashMap<AEKeyType, MEStorage> externalStorages = new IdentityHashMap<>(2);

            for(Map.Entry<AEKeyType, ExternalStorageStrategy> entry : strategies.entrySet()) {
                MEStorage wrapper = entry.getValue().createWrapper(false, Runnables.doNothing());
                if (wrapper != null) {
                    externalStorages.put(entry.getKey(), wrapper);
                }
            }

            if (!externalStorages.isEmpty()) {
                return wrapMeStorage(new CompositeStorage(externalStorages), src);
            } else {
                return null;
            }
        }
    }

    private static PatternProviderTarget wrapMeStorage(final MEStorage storage, final IActionSource src) {
        return new PatternProviderTarget() {
            public long insert(AEKey what, long amount, Actionable type) {
                return storage.insert(what, amount, type, src);
            }

            public boolean containsPatternInput(Set<AEKey> patternInputs) {
                for (var stack : storage.getAvailableStacks()) {
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

    long insert(AEKey var1, long var2, Actionable var4);

    boolean containsPatternInput(Set<AEKey> var1);

    boolean onlyHasPatternInput(Set<AEKey> var1);

    MEStorage getStorage();
}