package lu.kolja.expandedae.screen;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import lu.kolja.expandedae.menu.ExpDriveMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExpDriveScreen extends AEBaseScreen<ExpDriveMenu> {
    public ExpDriveScreen(ExpDriveMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        widgets.addOpenPriorityButton();
    }
}
