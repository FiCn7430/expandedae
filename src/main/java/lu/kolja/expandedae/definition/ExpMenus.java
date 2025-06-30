package lu.kolja.expandedae.definition;

import appeng.helpers.IPatternTerminalMenuHost;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import gripe._90.megacells.MEGACells;
import lu.kolja.expandedae.Expandedae;
import lu.kolja.expandedae.block.entity.ExpDriveBlockEntity;
import lu.kolja.expandedae.menu.ExpDriveMenu;
import lu.kolja.expandedae.menu.ExpPatternProviderMenu;
import lu.kolja.expandedae.terminal.ExpEncodingTerminalMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ExpMenus {
    public static final DeferredRegister<MenuType<?>> DR =
            DeferredRegister.create(Registries.MENU, Expandedae.MODID);

    private static final Map<ResourceLocation, MenuType<?>> MENU_TYPES = new HashMap<>();

    public static Map<ResourceLocation, MenuType<?>> getMenuTypes() {
        return Collections.unmodifiableMap(MENU_TYPES);
    }

    public static final Supplier<MenuType<ExpPatternProviderMenu>> EXP_PATTERN_PROVIDER = create(
            "exp_pattern_provider",
            ExpPatternProviderMenu::new,
            PatternProviderLogicHost.class
    );

    public static final Supplier<MenuType<ExpEncodingTerminalMenu>> EXP_ENCODING_TERMINAL = create(
            "exp_encoding_terminal",
            ExpEncodingTerminalMenu::new,
            IPatternTerminalMenuHost.class
    );

    public static final Supplier<MenuType<ExpDriveMenu>> EXP_DRIVE = create(
            "exp_drive",
            ExpDriveMenu::new,
            ExpDriveBlockEntity.class
    );
    /*
    public static final Supplier<MenuType<ExpWETMenu>> EXP_WIRELESS_ENCODING_TERMINAL = create(
            "exp_wireless_encoding_terminal",
            ExpWETMenu::new,
            ExpWETMenuHost.class
    );
    */
    private static <M extends AEBaseMenu, H> Supplier<MenuType<M>> create(
            String id, MenuTypeBuilder.MenuFactory<M, H> factory, Class<H> host) {
        return DR.register(id, () -> MenuTypeBuilder.create(factory, host).buildUnregistered(MEGACells.makeId(id)));
    }

    private static <M extends AEBaseMenu, H> Supplier<MenuType<M>> createTyped(
            String id, MenuTypeBuilder.TypedMenuFactory<M, H> factory, Class<H> host) {
        return DR.register(id, () -> MenuTypeBuilder.create(factory, host).buildUnregistered(MEGACells.makeId(id)));
    }
}
