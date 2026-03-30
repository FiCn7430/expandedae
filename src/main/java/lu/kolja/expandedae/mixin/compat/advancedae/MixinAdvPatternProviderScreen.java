package lu.kolja.expandedae.mixin.compat.advancedae;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import lu.kolja.expandedae.client.gui.widgets.ExpActionButton;
import lu.kolja.expandedae.client.gui.widgets.ExpActionItems;
import lu.kolja.expandedae.helper.pattern.IPatternProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.pedroksl.advanced_ae.client.gui.AdvPatternProviderScreen;
import net.pedroksl.advanced_ae.gui.advpatternprovider.AdvPatternProviderMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 为 AdvancedAE 的样板供应器屏幕添加修改样板按钮
 * 
 * 注意：阻塞模式按钮已移除，保留 AE2 原版的阻塞行为
 */
@Mixin(value = AdvPatternProviderScreen.class, remap = false)
public abstract class MixinAdvPatternProviderScreen<C extends AdvPatternProviderMenu> extends AEBaseScreen<C> {

    private MixinAdvPatternProviderScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    /**
     * 初始化时添加修改样板按钮
     */
    @Inject(
            method = "<init>(Lnet/pedroksl/advanced_ae/gui/advpatternprovider/AdvPatternProviderMenu;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;Lappeng/client/gui/style/ScreenStyle;)V",
            at = @At("TAIL"),
            remap = false)
    private void expandedae$init(AdvPatternProviderMenu menu, Inventory playerInventory, Component title, ScreenStyle style, CallbackInfo ci) {
        // 添加修改样板按钮
        ExpActionButton modifyPatterns = new ExpActionButton(
                ExpActionItems.MODIFY_PATTERNS, 
                16, 
                16, 
                act -> ((IPatternProvider) menu).expandedae$modifyPatterns(
                        ((AEBaseScreen<?>) Minecraft.getInstance().screen).isHandlingRightClick()
                )
        );
        this.addToLeftToolbar(modifyPatterns);
    }
}
