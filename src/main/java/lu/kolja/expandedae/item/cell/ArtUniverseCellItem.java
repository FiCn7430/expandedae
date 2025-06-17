package lu.kolja.expandedae.item.cell;

import appeng.api.stacks.AEKeyType;
import com.glodblock.github.extendedae.common.inventory.InfinityCellInventory;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lu.kolja.expandedae.cellhandler.ArtUniverseCellHandler;
import lu.kolja.expandedae.cellhandler.ArtUniverseCellInventory;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@Getter
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ArtUniverseCellItem extends Item {
    private final AEKeyType keyType;

    public ArtUniverseCellItem(AEKeyType keyType) {
        super(new Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
        this.keyType = keyType;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        Preconditions.checkArgument(stack.getItem() == this);
        var handler = ArtUniverseCellHandler.INSTANCE.getCellInventory(stack, null);
        var data = stack.get(ArtUniverseCellInventory.UUID);
        if (data != null && handler != null && handler.hasUUID()) {
            tooltip.add(Component.literal("UUID: ").withStyle(ChatFormatting.GRAY).append(Component.literal(handler.getUUID().toString()).withStyle(ChatFormatting.AQUA)));
            tooltip.add(Component.literal("Byte: ").withStyle(ChatFormatting.GRAY).append(Component.literal(handler.getTotalStorage()).withStyle(ChatFormatting.GREEN)));
        }
    }
}
