package lu.kolja.expandedae.menu;

import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.RestrictedInputSlot;
import lu.kolja.expandedae.block.entity.ExpDriveBlockEntity;
import lu.kolja.expandedae.definition.ExpMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class ExpDriveMenu extends AEBaseMenu {
    public static final SlotSemantic CELLS_1 = SlotSemantics.register("EXP_CELLS_1", false);
    public static final SlotSemantic CELLS_2 = SlotSemantics.register("EXP_CELLS_2", false);
    public static final SlotSemantic CELLS_3 = SlotSemantics.register("EXP_CELLS_3", false);
    public static final SlotSemantic CELLS_4 = SlotSemantics.register("EXP_CELLS_4", false);
    
    public ExpDriveMenu(int id, Inventory ip, ExpDriveBlockEntity drive) {
        super(ExpMenus.EXP_DRIVE.get(), id, ip, drive);

        for (int i = 0; i < 10; i++) {
            this.addSlot(new RestrictedInputSlot(
                    RestrictedInputSlot.PlacableItemType.STORAGE_CELLS,
                    drive.getInternalInventory(), i), CELLS_1
            );
        }
        for (int i = 0; i < 10; i++) {
            this.addSlot(new RestrictedInputSlot(
                    RestrictedInputSlot.PlacableItemType.STORAGE_CELLS,
                    drive.getInternalInventory(), i + 10), CELLS_2
            );
        }
        for (int i = 0; i < 10; i++) {
            this.addSlot(new RestrictedInputSlot(
                    RestrictedInputSlot.PlacableItemType.STORAGE_CELLS,
                    drive.getInternalInventory(), i + 20), CELLS_3
            );
        }
        for (int i = 0; i < 10; i++) {
            this.addSlot(new RestrictedInputSlot(
                    RestrictedInputSlot.PlacableItemType.STORAGE_CELLS,
                    drive.getInternalInventory(), i + 30), CELLS_4
            );
        }

        this.createPlayerInventorySlots(ip);
    }
}
