package lu.kolja.expandedae.terminal;

import appeng.helpers.IPatternTerminalMenuHost;
import appeng.menu.me.items.PatternEncodingTermMenu;
import lu.kolja.expandedae.definition.ExpMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class ExpEncodingTerminalMenu extends PatternEncodingTermMenu {
    public ExpEncodingTerminalMenu(int id, Inventory ip, IPatternTerminalMenuHost host) {
        this(ExpMenus.EXP_ENCODING_TERMINAL.get(), id, ip, host);
    }
    public ExpEncodingTerminalMenu(MenuType<?> menuType, int id, Inventory ip, IPatternTerminalMenuHost host) {
        super(menuType, id, ip, host, true);
    }
}
