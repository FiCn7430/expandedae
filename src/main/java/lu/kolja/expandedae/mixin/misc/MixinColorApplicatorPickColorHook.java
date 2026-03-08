package lu.kolja.expandedae.mixin.misc;

import appeng.core.definitions.ItemDefinition;
import appeng.hooks.ColorApplicatorPickColorHook;
import appeng.items.tools.powered.ColorApplicatorItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lu.kolja.expandedae.definition.ExpItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ColorApplicatorPickColorHook.class, remap = false)
public class MixinColorApplicatorPickColorHook {
    @WrapOperation(
            method = "onPickColor",
            at = @At(value = "INVOKE", target = "Lappeng/core/definitions/ItemDefinition;isSameAs(Lnet/minecraft/world/item/ItemStack;)Z"),
            remap = false
    )
    private static boolean onPickColor(ItemDefinition<? extends ColorApplicatorItem> instance, ItemStack comparableStack, Operation<Boolean> original) {
        return original.call(instance, comparableStack) || ExpItems.INFINITY_COLOR_APPLICATOR.isSameAs(comparableStack);
    }
}
