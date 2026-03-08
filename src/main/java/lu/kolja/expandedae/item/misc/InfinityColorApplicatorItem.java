package lu.kolja.expandedae.item.misc;

import appeng.api.util.AEColor;
import appeng.core.localization.Tooltips;
import appeng.helpers.IMouseWheelItem;
import appeng.items.misc.PaintBallItem;
import appeng.items.tools.powered.ColorApplicatorItem;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class InfinityColorApplicatorItem extends ColorApplicatorItem implements IMouseWheelItem {
    private int currentColorIndex = 0;

    public InfinityColorApplicatorItem(Properties props) {
        super(props.stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return super.getChargeRate(stack) * 2;
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return 0;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return 0;
    }

    @Override
    public ItemStack getColor(ItemStack is) {
        final CompoundTag c = is.getTag();
        if (c != null && c.contains("color")) {
            final CompoundTag color = c.getCompound("color");
            final ItemStack oldColor = ItemStack.of(color);
            if (!oldColor.isEmpty()) {
                return oldColor;
            }
        }

        return this.findNextColor(is, ItemStack.EMPTY, 0);
    }

    private ItemStack findNextColor(ItemStack is, ItemStack anchor, int scrollOffset) {
        var color = getColorFromItem(anchor);
        currentColorIndex =  currentColorIndex >= 17 ? 0 : ++currentColorIndex;
        var next = AEColor.values()[currentColorIndex];
        var stack = BuiltInRegistries.ITEM.get(new ResourceLocation(next.registryPrefix + "_dye")).getDefaultInstance();
        this.setColor(is, stack);
        return stack;
    }

    @Override
    public AEColor getActiveColor(ItemStack tol) {
        return AEColor.values()[currentColorIndex];
    }

    @Override
    public void cycleColors(ItemStack is, ItemStack paintBall, int i) {
        if (paintBall.isEmpty()) {
            this.setColor(is, this.getColor(is));
        } else {
            this.setColor(is, this.findNextColor(is, paintBall, i));
        }
    }

    /**
     * Copied from {@link AEBasePoweredItem#appendHoverText(ItemStack, Level, List, TooltipFlag)}
     * so we don't need to show the cell tooltip
     */
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag advancedTooltips) {
        final CompoundTag tag = stack.getTag();
        double internalCurrentPower = 0;
        final double internalMaxPower = this.getAEMaxPower(stack);

        if (tag != null) {
            internalCurrentPower = tag.getDouble("internalCurrentPower");
        }

        lines.add(Tooltips.energyStorageComponent(internalCurrentPower, internalMaxPower));
    }

    private void setColor(ItemStack is, ItemStack newColor) {
        final CompoundTag data = is.getOrCreateTag();
        if (newColor.isEmpty()) {
            data.remove("color");
        } else {
            final CompoundTag color = new CompoundTag();
            newColor.save(color);
            data.put("color", color);
        }
    }

    private AEColor getColorFromItem(ItemStack paintBall) {
        if (paintBall.isEmpty()) {
            return null;
        }

        return getColorFromItem(paintBall.getItem());
    }

    private AEColor getColorFromItem(Item paintBall) {
        if (paintBall instanceof SnowballItem) {
            return AEColor.TRANSPARENT;
        }

        if (paintBall instanceof PaintBallItem ipb) {
            return ipb.getColor();
        }

        return null;
    }

    @Override
    public void onWheel(ItemStack is, boolean up) {
        this.cycleColors(is, this.getColor(is), up ? 1 : -1);
    }
}