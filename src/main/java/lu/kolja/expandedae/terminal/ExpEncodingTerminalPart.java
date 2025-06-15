package lu.kolja.expandedae.terminal;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.PartModel;
import appeng.parts.encoding.PatternEncodingTerminalPart;
import lu.kolja.expandedae.Expandedae;
import lu.kolja.expandedae.definition.ExpItems;
import lu.kolja.expandedae.definition.ExpMenus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ExpEncodingTerminalPart extends PatternEncodingTerminalPart {
    @PartModels
    public static final ResourceLocation MODEL_OFF = Expandedae.makeId("part/exp_encoding_terminal_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = Expandedae.makeId("part/exp_encoding_terminal_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    public ExpEncodingTerminalPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public boolean onUseWithoutItem(Player p, Vec3 pos) {
        if (!p.getCommandSenderWorld().isClientSide()) {
            MenuOpener.open(ExpMenus.EXP_ENCODING_TERMINAL.get(), p, MenuLocators.forPart(this));
        }
        return true;
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(ExpMenus.EXP_ENCODING_TERMINAL.get(), player, subMenu.getLocator());
    }

    @Override
    public MenuType<?> getMenuType(Player p) {
        return ExpMenus.EXP_ENCODING_TERMINAL.get();
    }

    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return ExpItems.EXP_ENCODING_TERMINAL.stack();
    }
}
