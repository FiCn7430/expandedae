package lu.kolja.expandedae.menu;

import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.FakeSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class FlowMonitorMenu extends AEBaseMenu {
    public FlowMonitorMenu(MenuType<?> menuType, int id, Inventory playerInventory, FlowMonitorMenuHost host) {
        super(menuType, id, playerInventory, host);
        this.createPlayerInventorySlots(playerInventory);

        var slot = new FakeSlot(host.getInternalInventory(), 0);
        slots.add(slot);
        this.addSlot(slot, SlotSemantics.CONFIG);
    }
}
