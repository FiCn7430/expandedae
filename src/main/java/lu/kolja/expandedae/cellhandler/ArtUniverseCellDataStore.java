package lu.kolja.expandedae.cellhandler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class ArtUniverseCellDataStore {
    public static final ArtUniverseCellDataStore INSTANCE = new ArtUniverseCellDataStore();

    public ListTag keys;
    public long[] amounts;

    public ArtUniverseCellDataStore() {
        this(new ListTag(), new long[0]);
    }
    private ArtUniverseCellDataStore(ListTag keys, long[] amounts) {
        this.keys = keys;
        this.amounts = amounts;
    }

    public CompoundTag nbt() {
        var nbt = new CompoundTag();
        nbt.put("keys", this.keys);
        nbt.putLongArray("amounts", this.amounts);
        return nbt;
    }

    public static ArtUniverseCellDataStore fromNbt(CompoundTag nbt) {
        ListTag keys = nbt.getList("keys", Tag.TAG_COMPOUND);
        long[] amounts = nbt.getLongArray("amounts");
        return new ArtUniverseCellDataStore(keys, amounts);
    }
}
