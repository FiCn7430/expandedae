package lu.kolja.expandedae.terminal;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.core.definitions.AEItems;
import appeng.helpers.IPatternTerminalMenuHost;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.util.ConfigInventory;
import lu.kolja.expandedae.definition.ExpItems;
import lu.kolja.expandedae.definition.ExpMenus;
import lu.kolja.expandedae.mixin.accessor.AccessorPatternEncodingTermMenu;
import lu.kolja.expandedae.terminal.wtlib.ExpWETScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class ExpEncodingTerminalMenu extends PatternEncodingTermMenu {
    public ExpEncodingTerminalMenu(int id, Inventory ip, IPatternTerminalMenuHost host) {
        this(ExpMenus.EXP_ENCODING_TERMINAL.get(), id, ip, host);
    }
    public ExpEncodingTerminalMenu(MenuType<?> menuType, int id, Inventory ip, IPatternTerminalMenuHost host) {
        super(menuType, id, ip, host, true);
        registerClientAction("modifyPattern", Integer.class, this::modifyPattern);
    }

    @Override
    public void encode() {
        super.encode();
        var node = this.getGridNode();
        var source = this.getActionSource().player();
        assert source.isPresent();
        var player = source.get();
        var encodedPatternSlot = ((AccessorPatternEncodingTermMenu) this).getEncodedPatternSlot();
        var blankPatternSlot = ((AccessorPatternEncodingTermMenu) this).getBlankPatternSlot();

        if (encodedPatternSlot.getItem() != ItemStack.EMPTY) {
            if (AbstractContainerScreen.hasShiftDown()) {
                if (player.getInventory().getFreeSlot() > 0) {
                    player.addItem(encodedPatternSlot.getItem());
                    encodedPatternSlot.set(ItemStack.EMPTY);
                    encodedPatternSlot.setChanged();
                }
            }
        }

        if (!(Minecraft.getInstance().screen instanceof ExpWETScreen wetScreen)) return;
        var terminalItem = wetScreen.getHost().getItemStack();
        if (terminalItem == null) return;

        IUpgradeInventory inventory = ((IUpgradeableItem) terminalItem.getItem()).getUpgrades(terminalItem);
        if (!inventory.isInstalled(ExpItems.PATTERN_REFILLER_CARD)) return;

        var blankPatternSlotCount = blankPatternSlot.getItem().getCount();
        if (node == null) return;
        int changed = (int) Objects.requireNonNull(node).getGrid().getStorageService().getInventory().extract(
                AEItemKey.of(AEItems.BLANK_PATTERN),
                64 - blankPatternSlotCount,
                Actionable.MODULATE,
                this.getActionSource()
        );
        blankPatternSlot.set(new ItemStack(AEItems.BLANK_PATTERN, blankPatternSlotCount + changed));
        blankPatternSlot.setChanged();
    }

    public void modifyPattern(Integer data) {
        if (isClientSide()) {
            sendClientAction("modifyPattern", data);
        } else {
            var encodedInputsInv = ((AccessorPatternEncodingTermMenu) this).getEncodedInputsInv();
            var encodedOutputsInv = ((AccessorPatternEncodingTermMenu) this).getEncodedOutputsInv();
            var output = isValid(encodedOutputsInv, data);
            if (output == null) {
                return;
            }
            var input = isValid(encodedInputsInv, data);
            if (input == null) {
                return;
            }
            for (int slot = 0; slot < output.length; ++slot) {
                if (output[slot] != null) {
                    encodedOutputsInv.setStack(slot, output[slot]);
                }
            }
            for (int slot = 0; slot < input.length; ++slot) {
                if (input[slot] != null) {
                    encodedInputsInv.setStack(slot, input[slot]);
                }
            }
        }
    }

    private static GenericStack[] isValid(ConfigInventory inv, int data) {
        boolean flag = data > 0;
        if (!flag) {
            data = -data;
        }
        GenericStack[] result = new GenericStack[inv.size()];
        for (int slot = 0; slot < inv.size(); ++slot) {
            GenericStack stack = inv.getStack(slot);
            if (stack != null) {
                if (flag) {
                    if (data * stack.amount() > Integer.MAX_VALUE) {
                        return null;
                    } else {
                        result[slot] = new GenericStack(stack.what(), data * stack.amount());
                    }
                } else {
                    if (stack.amount() % data != 0) {
                        return null;
                    } else {
                        result[slot] = new GenericStack(stack.what(), stack.amount() / data);
                    }
                }
            }
        }
        return result;
    }
}
