package lu.kolja.expandedae.mixin.compat.advancedae;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.GenericStack;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.ToolboxMenu;
import appeng.menu.guisync.GuiSync;
import lu.kolja.expandedae.definition.ExpSettings;
import lu.kolja.expandedae.enums.BlockingMode;
import lu.kolja.expandedae.helper.misc.KeybindUtil;
import lu.kolja.expandedae.helper.pattern.IPatternProvider;
import lu.kolja.expandedae.helper.pattern.IUpgradableMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.ItemLike;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic;
import net.pedroksl.advanced_ae.gui.advpatternprovider.AdvPatternProviderMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

/**
 * 为 AdvancedAE 的样板供应器菜单添加 IPatternProvider 接口支持
 * 提供修改样板和阻塞模式功能
 */
@Mixin(value = AdvPatternProviderMenu.class, remap = false)
public abstract class MixinAdvPatternProviderMenuIPatternProvider extends AEBaseMenu implements IUpgradableMenu, IPatternProvider {

    @Unique
    private static final int BASE_FACTOR = 2;

    @Final
    @Shadow
    protected AdvPatternProviderLogic logic;

    @Unique
    private ToolboxMenu expandedae$toolbox;

    @Unique
    @GuiSync(100)
    public BlockingMode expandedae$blockingMode = BlockingMode.DEFAULT;

    public MixinAdvPatternProviderMenuIPatternProvider(MenuType<?> menuType, int id, Inventory playerInventory, Object host) {
        super(menuType, id, playerInventory, host);
    }

    /**
     * 初始化时注册客户端动作和工具箱
     */
    @Inject(
            method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lnet/pedroksl/advanced_ae/common/logic/AdvPatternProviderLogicHost;)V",
            at = @At("TAIL"),
            remap = false)
    private void expandedae$initIPatternProvider(MenuType<?> menuType, int id, Inventory playerInventory, 
                                                  net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogicHost host, 
                                                  CallbackInfo ci) {
        this.expandedae$toolbox = new ToolboxMenu(this);
        this.registerClientAction("modifyPatterns", Boolean.class, this::expandedae$modifyPatterns);
    }

    /**
     * 修改样板数量（乘以或除以缩放因子）
     * 
     * @param rightClick 如果为 true，则除以缩放因子；如果为 false，则乘以缩放因子
     */
    @Unique
    @Override
    public void expandedae$modifyPatterns(boolean rightClick) {
        if (this.isClientSide()) {
            this.sendClientAction("modifyPatterns", rightClick);
        } else {
            for (var slot : this.getSlots(SlotSemantics.ENCODED_PATTERN)) {
                var stack = slot.getItem();
                var detail = PatternDetailsHelper.decodePattern(stack, this.getPlayer().level());
                if (detail instanceof AEProcessingPattern processingPattern) {
                    var input = processingPattern.getSparseInputs().toArray(GenericStack[]::new);
                    var output = processingPattern.getOutputs().toArray(GenericStack[]::new);
                    int scale = expandedae$getScale();
                    
                    if (expandedae$checkModify(input, scale, rightClick) && 
                        expandedae$checkModify(output, scale, rightClick)) {
                        var mulInput = new GenericStack[input.length];
                        var mulOutput = new GenericStack[output.length];
                        expandedae$modifyStacks(input, mulInput, scale, rightClick);
                        expandedae$modifyStacks(output, mulOutput, scale, rightClick);
                        var newPattern = PatternDetailsHelper.encodeProcessingPattern(
                                Arrays.stream(mulInput).toList(),
                                Arrays.stream(mulOutput).toList()
                        );
                        slot.set(newPattern);
                    }
                }
            }
        }
    }

    @Unique
    private int expandedae$getScale() {
        return BASE_FACTOR * KeybindUtil.shiftMultiplier() * KeybindUtil.ctrlMultiplier();
    }

    @Unique
    private boolean expandedae$checkModify(GenericStack[] stacks, int scale, boolean division) {
        if (division) {
            for (var stack : stacks) {
                if (stack != null) {
                    if (stack.amount() % scale != 0) {
                        return false;
                    }
                }
            }
        } else {
            for (var stack : stacks) {
                if (stack != null) {
                    long upper = 999999L * stack.what().getAmountPerUnit();
                    if (stack.amount() * scale > upper) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Unique
    private void expandedae$modifyStacks(GenericStack[] stacks, GenericStack[] des, int scale, boolean division) {
        for (int i = 0; i < stacks.length; i++) {
            if (stacks[i] != null) {
                long amt = division ? stacks[i].amount() / scale : stacks[i].amount() * scale;
                des[i] = new GenericStack(stacks[i].what(), amt);
            }
        }
    }

    @Override
    public ToolboxMenu expandedae$getToolbox() {
        return this.expandedae$toolbox;
    }

    @Override
    public IUpgradeInventory expandedae$getUpgrades() {
        return ((IUpgradeableObject) this.logic).getUpgrades();
    }

    @Override
    public boolean expandedae$hasUpgrade(ItemLike upgradeCard) {
        IUpgradeInventory upgrades = expandedae$getUpgrades();
        return upgrades != null && upgrades.isInstalled(upgradeCard);
    }

    /**
     * 每 tick 更新工具箱
     */
    @Inject(
            method = "broadcastChanges",
            at = @At("TAIL"),
            remap = true)
    @Unique
    public void expandedae$tickToolbox(CallbackInfo ci) {
        this.expandedae$toolbox.tick();
    }

    /**
     * 同步阻塞模式设置
     */
    @Inject(
            method = "broadcastChanges",
            at = @At("TAIL"),
            remap = true)
    private void expandedae$broadcastChanges(CallbackInfo ci) {
        expandedae$blockingMode = logic.getConfigManager().getSetting(ExpSettings.BLOCKING_MODE);
    }

    @Override
    public BlockingMode expandedae$getBlockingMode() {
        return expandedae$blockingMode;
    }

    @Override
    public void expandedae$resetBlocking() {
        expandedae$blockingMode = BlockingMode.DEFAULT;
    }

    @Override
    public void expandedae$setBlocking(BlockingMode blockingMode) {
        expandedae$blockingMode = blockingMode;
    }

    @Override
    public void expandedae$showBlocking() {
        // 按钮可见性由屏幕 Mixin 控制
    }
}
