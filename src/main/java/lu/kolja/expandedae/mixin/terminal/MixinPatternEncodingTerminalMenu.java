package lu.kolja.expandedae.mixin.terminal;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.ITerminalHost;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.core.definitions.AEItems;
import appeng.helpers.IPatternTerminalMenuHost;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.util.ConfigInventory;
import lu.kolja.expandedae.definition.ExpItems;
import lu.kolja.expandedae.helper.misc.KeybindUtil;
import lu.kolja.expandedae.helper.pattern.IPatternEncodingTerminalMenu;
import lu.kolja.expandedae.terminal.ExpEncodingTerminalMenu;
import lu.kolja.expandedae.terminal.ExpEncodingTerminalScreen;
import lu.kolja.expandedae.terminal.wtlib.ExpWETScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(value = PatternEncodingTermMenu.class, remap = false)
public abstract class MixinPatternEncodingTerminalMenu extends MEStorageMenu implements IPatternEncodingTerminalMenu {
    @Final
    @Shadow
    @Mutable
    private RestrictedInputSlot encodedPatternSlot;

    @Shadow
    @Final
    private RestrictedInputSlot blankPatternSlot;

    @Shadow(remap = false)
    @Final
    private ConfigInventory encodedInputsInv;

    @Shadow(remap = false)
    @Final
    private ConfigInventory encodedOutputsInv;

    protected MixinPatternEncodingTerminalMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host) {
        super(menuType, id, ip, host);
    }

    @Inject(method = "encode", at = @At("RETURN"))
    private void encode(CallbackInfo ci) {
        final IGridNode node = this.getGridNode();
        var source = this.getActionSource().player();
        assert source.isPresent();
        var player = source.get();
        if (!(Minecraft.getInstance().screen instanceof ExpEncodingTerminalScreen<? extends ExpEncodingTerminalMenu>
                || Minecraft.getInstance().screen instanceof ExpWETScreen)) return;
        if (encodedPatternSlot.getItem() != ItemStack.EMPTY) {
            if (KeybindUtil.isShiftDown()) {
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

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lappeng/helpers/IPatternTerminalMenuHost;Z)V",
            at = @At("TAIL"),
            remap = false)
    private void initHooks(MenuType<?> menuType, int id, Inventory ip, IPatternTerminalMenuHost host,
                           boolean bindInventory, CallbackInfo ci) {
        registerClientAction("modifyPattern", Integer.class,
                this::eae$ModifyPattern);
    }

    @Unique
    @Override
    public void eae$ModifyPattern(Integer data) {
        if (isClientSide()) {
            sendClientAction("modifyPattern", data);
        } else {
            // modify
            var output = eae$isValid(encodedOutputsInv, data);
            if (output == null) {
                return;
            }
            var input = eae$isValid(encodedInputsInv, data);
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

    @Unique
    private static GenericStack[] eae$isValid(ConfigInventory inv, int data) {
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