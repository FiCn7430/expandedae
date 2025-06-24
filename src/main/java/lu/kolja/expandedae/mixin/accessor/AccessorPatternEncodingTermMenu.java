package lu.kolja.expandedae.mixin.accessor;

import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.util.ConfigInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = PatternEncodingTermMenu.class, remap = false)
public interface AccessorPatternEncodingTermMenu {
    @Accessor("encodedPatternSlot")
    RestrictedInputSlot getEncodedPatternSlot();

    @Accessor("blankPatternSlot")
    RestrictedInputSlot getBlankPatternSlot();

    @Accessor("encodedInputsInv")
    ConfigInventory getEncodedInputsInv();

    @Accessor("encodedOutputsInv")
    ConfigInventory getEncodedOutputsInv();
}
