package lu.kolja.expandedae.cellhandler;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import lu.kolja.expandedae.ExpConfig;
import lu.kolja.expandedae.helper.misc.NumberUtil;
import lu.kolja.expandedae.item.cell.ArtUniverseCellItem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ArtUniverseCellInventory implements StorageCell {
    private final ItemStack stack;
    private final ISaveProvider container;
    private final AEKeyType keyType;
    private Object2LongMap<AEKey> storedMap;
    private boolean isPersisted = true;

    public static final DataComponentType<CompoundTag> UUID = DataComponentType.<CompoundTag>builder()
            .persistent(CompoundTag.CODEC)
            .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
            .build();

    private ArtUniverseCellInventory(AEKeyType keyType, ItemStack stack, ISaveProvider saveProvider) {
        this.stack = stack;
        container = saveProvider;
        this.keyType = keyType;
        storedMap = null;
        initData();
    }

    private ArtUniverseCellDataStore getCellStorage() {
        if (getUUID() == null) {
            return ArtUniverseCellDataStore.INSTANCE;
        } else {
            return getStorageInstance().getOrCreateCell(getUUID());
        }
    }

    private void initData() {
        if (!hasUUID()) {
            getCellStoredMap();
        }
    }

    @Override
    public CellState getStatus() {
        if (getCellStoredMap().isEmpty()) {
            return CellState.EMPTY;
        }
        return CellState.NOT_EMPTY;
    }

    @Override
    public double getIdleDrain() {
        return ExpConfig.idleDrain;
    }

    @Override
    public Component getDescription() {
        return null;
    }

    public boolean hasUUID() {
        var data = stack.get(UUID);
        return data != null && data.contains("uuid");
    }

    public UUID getUUID() {
        var data = stack.get(UUID);
        if (data != null && data.contains("uuid"))
            return data.getUUID("uuid");
        return null;
    }

    static ArtUniverseCellInventory createInventory(ItemStack stack, ISaveProvider saveProvider) {
        if (stack.getItem() instanceof ArtUniverseCellItem cellType) {
            return new ArtUniverseCellInventory(cellType.getKeyType(), stack, saveProvider);
        }
        return null;
    }

    private static ArtUniverseCellSavedData getStorageInstance() {
        return ArtUniverseCellSavedData.INSTANCE;
    }

    private Object2LongMap<AEKey> getCellStoredMap() {
        if (storedMap == null) {
            storedMap = new Object2LongOpenHashMap<>();
            loadCellStoredMap();
        }
        return storedMap;
    }

    private void loadCellStoredMap() {
        boolean corruptedTag = false;
        if (stack.get(UUID) == null) return;
        long[] amounts = getCellStorage().amounts;
        for (int i = 0; i < amounts.length; i++) {
            long amount = amounts[i];
            var level = Minecraft.getInstance().level;
            if (level == null) return;
            AEKey key = AEKey.fromTagGeneric(level.registryAccess(), getCellStorage().keys.getCompound(i));
            if (amount <= 0 || key == null) {
                corruptedTag = true;
            } else {
                getCellStoredMap().put(key, amount);
            }
        }
        if (corruptedTag) {
            saveChanges();
        }
    }

    private void saveChanges() {
        isPersisted = false;
        if (container != null) {
            container.saveChanges();
        } else {
            persist();
        }
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (Object2LongMap.Entry<AEKey> entry : getCellStoredMap().object2LongEntrySet()) {
            out.add(entry.getKey(), entry.getLongValue());
        }
    }

    @Override
    public void persist() {
        if (isPersisted) return;
        if (getCellStoredMap().isEmpty()) {
            if (hasUUID()) {
                getStorageInstance().removeCell(getUUID());
                var tag = stack.get(UUID);
                if (tag != null) {
                    tag.remove("uuid");
                }
                initData();
            }
            return;
        }
        LongArrayList amounts = new LongArrayList(getCellStoredMap().size());
        ListTag keys = new ListTag();
        for (Object2LongMap.Entry<AEKey> entry : getCellStoredMap().object2LongEntrySet()) {
            long amount = entry.getLongValue();
            if (amount > 0) {
                var level = Minecraft.getInstance().level;
                if (level == null) return;
                keys.add(entry.getKey().toTagGeneric(level.registryAccess()));
                amounts.add(amount);
            }
        }
        if (keys.isEmpty()) {
            getStorageInstance().updateCell(getUUID(), new ArtUniverseCellDataStore());
        } else {
            getStorageInstance().modifyCell(getUUID(), keys, amounts.toArray(new long[0]));
        }
        isPersisted = true;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount == 0 || !keyType.contains(what)) return 0;
        if (what instanceof AEItemKey itemKey && itemKey.getItem() instanceof ArtUniverseCellItem) return 0;
        if (!hasUUID()) {
            var data = stack.get(UUID);
            if (data != null && !data.contains("uuid"))
                data.putUUID("uuid", java.util.UUID.randomUUID());

            getStorageInstance().getOrCreateCell(getUUID());
            loadCellStoredMap();
        }
        long currentAmount = getCellStoredMap().getLong(what);
        if (mode == Actionable.MODULATE) {
            getCellStoredMap().put(what, currentAmount + amount);
            saveChanges();
        }
        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        long currentAmount = getCellStoredMap().getLong(what);
        if (currentAmount > 0) {
            if (amount >= currentAmount) {
                if (mode == Actionable.MODULATE) {
                    getCellStoredMap().remove(what, currentAmount);
                    saveChanges();
                }
                return currentAmount;
            } else {
                if (mode == Actionable.MODULATE) {
                    getCellStoredMap().put(what, currentAmount - amount);
                    saveChanges();
                }
                return amount;
            }
        }
        return 0;
    }

    public String getTotalStorage() {
        double itemCount = 0;
        for (long storedAmount : getCellStoredMap().values()) {
            itemCount += storedAmount;
        }
        return NumberUtil.formatDouble(itemCount);
    }
}
