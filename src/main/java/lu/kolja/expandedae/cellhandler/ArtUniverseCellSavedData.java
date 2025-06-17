package lu.kolja.expandedae.cellhandler;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArtUniverseCellSavedData extends SavedData {
    public static ArtUniverseCellSavedData INSTANCE = new ArtUniverseCellSavedData();

    private final Map<UUID, ArtUniverseCellDataStore> cells = new HashMap<>();

    public ArtUniverseCellSavedData() {
        setDirty();
    }

    public ArtUniverseCellSavedData(CompoundTag nbt) {
        ListTag cellList = nbt.getList("list", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < cellList.size(); i++) {
            CompoundTag cell = cellList.getCompound(i);
            cells.put(cell.getUUID("uuid"), ArtUniverseCellDataStore.fromNbt(cell.getCompound("data")));
        }
        setDirty();
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
        ListTag cellList = new ListTag();
        for (Map.Entry<UUID, ArtUniverseCellDataStore> entry : cells.entrySet()) {
            CompoundTag cell = new CompoundTag();
            cell.putUUID("uuid", entry.getKey());
            cell.put("data", entry.getValue().nbt());
            cellList.add(cell);
        }
        nbt.put("list", cellList);
        return nbt;
    }

    public void updateCell(UUID uuid, ArtUniverseCellDataStore infinityCellDataStorage) {
        cells.put(uuid, infinityCellDataStorage);
        setDirty();
    }

    public ArtUniverseCellDataStore getOrCreateCell(UUID uuid) {
        if (!cells.containsKey(uuid)) {
            updateCell(uuid, new ArtUniverseCellDataStore());
        }
        return cells.get(uuid);
    }

    public void modifyCell(UUID cellID, ListTag keys, long[] amounts) {
        ArtUniverseCellDataStore cellToModify = getOrCreateCell(cellID);
        if (keys != null && amounts != null) {
            cellToModify.keys = keys;
            cellToModify.amounts = amounts;
        }
        updateCell(cellID, cellToModify);
    }

    public void removeCell(UUID uuid) {
        cells.remove(uuid);
        setDirty();
    }
}
