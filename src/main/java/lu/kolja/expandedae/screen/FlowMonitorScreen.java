package lu.kolja.expandedae.screen;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import lu.kolja.expandedae.menu.FlowMonitorMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FlowMonitorScreen extends AEBaseScreen<FlowMonitorMenu> {
    public FlowMonitorScreen(FlowMonitorMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);


    }
}
