package lu.kolja.expandedae.menu;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class FlowMonitorMenuHost extends ItemMenuHost implements InternalInventoryHost {

    private final AppEngInternalInventory inv;
    public FlowMonitorMenuHost(Player player, @Nullable Integer slot, ItemStack itemStack) {
        super(player, slot, itemStack);
        this.inv = new AppEngInternalInventory(this, 1);
        this.inv.setEnableClientEvents(true);
        if (itemStack.hasTag()) {
            this.inv.readFromNBT(itemStack.getOrCreateTag(), "inv");
        }
    }

    @Override
    public void saveChanges() {
        this.inv.writeToNBT(getItemStack().getOrCreateTag(), "inv");
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {}

    public InternalInventory getInternalInventory() {
        return this.inv;
    }
}
