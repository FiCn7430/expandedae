package lu.kolja.expandedae.item;

import lu.kolja.expandedae.block.entity.OmniLaserBeamBlockEntity;
import lu.kolja.expandedae.laserbeam.ILinkable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 激光绑定工具
 * 
 * 功能：用于连接两个全向激光线缆
 * 
 * 使用方法：
 * 1. Shift+右键点击第一个全向激光线缆（设置为源）
 * 2. 右键点击第二个全向激光线缆（建立连接）
 * 3. 再次右键已连接的线缆可断开连接
 * 4. Shift+左键空气清空已选定的源
 * 
 * 连接限制：
 * - 水平范围：16格
 * - 垂直范围：32格
 */
public class LaserBindingTool extends Item {
    
    /** NBT标签：源位置 */
    private static final String TAG_SOURCE = "SourcePos";
    
    /** NBT标签：源类型 */
    private static final String TAG_SOURCE_TYPE = "SourceType";
    
    /** 源类型：全向激光线缆 */
    private static final String TYPE_OMNI = "omni";
    
    /** 全向激光线缆连接范围 - 水平 */
    private static final int OMNI_RANGE_XZ = 16;
    
    /** 全向激光线缆连接范围 - 垂直 */
    private static final int OMNI_RANGE_Y = 32;

    public LaserBindingTool(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockPos pos = ctx.getClickedPos();
        ItemStack stack = ctx.getItemInHand();
        Player player = ctx.getPlayer();

        BlockEntity be = level.getBlockEntity(pos);
        
        CompoundTag tag = stack.getOrDefault(
            net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
            net.minecraft.world.item.component.CustomData.EMPTY
        ).copyTag();
        
        boolean hasSource = tag.contains(TAG_SOURCE);
        boolean isLinkable = be instanceof ILinkable;
        
        if (player != null && player.isShiftKeyDown()) {
            // Shift+右键：选择源
            
            if (!isLinkable) {
                return InteractionResult.PASS;
            }
            
            // Shift+右键：选定被连接的激光线缆（源）
            CompoundTag t = new CompoundTag();
            t.putInt("x", pos.getX());
            t.putInt("y", pos.getY());
            t.putInt("z", pos.getZ());
            tag.put(TAG_SOURCE, t);
            
            // 记录源类型
            if (be instanceof OmniLaserBeamBlockEntity) {
                tag.putString(TAG_SOURCE_TYPE, TYPE_OMNI);
                stack.set(
                    net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
                    net.minecraft.world.item.component.CustomData.of(tag)
                );
                player.displayClientMessage(
                    Component.translatable("tooltip.expandedae.binding.set_omni", pos.getX(), pos.getY(), pos.getZ()), 
                    true
                );
            } else {
                tag.putString(TAG_SOURCE_TYPE, TYPE_OMNI);
                stack.set(
                    net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
                    net.minecraft.world.item.component.CustomData.of(tag)
                );
                player.displayClientMessage(
                    Component.translatable("tooltip.expandedae.binding.set", pos.getX(), pos.getY(), pos.getZ()), 
                    true
                );
            }
            return InteractionResult.CONSUME;
            
        } else {
            // 普通右键：建立连接
            
            if (!hasSource) {
                // 没有选定源
                if (isLinkable) {
                    // 如果是ILinkable，提示先选定源
                    if (player != null) {
                        player.displayClientMessage(
                            Component.translatable("tooltip.expandedae.binding.no_source"), 
                            true
                        );
                    }
                    return InteractionResult.SUCCESS;
                } else {
                    // 不是ILinkable，让其他交互继续
                    return InteractionResult.PASS;
                }
            }
            
            // 已选定源，执行绑定逻辑
            // 全向激光线缆只能连接其他全向激光线缆
            if (!(be instanceof OmniLaserBeamBlockEntity)) {
                // 目标不是全向激光线缆
                if (player != null) {
                    player.displayClientMessage(
                        Component.translatable("tooltip.expandedae.binding.omni_only"), 
                        true
                    );
                }
                return InteractionResult.SUCCESS;
            }
            
            CompoundTag t = tag.getCompound(TAG_SOURCE);
            BlockPos source = new BlockPos(t.getInt("x"), t.getInt("y"), t.getInt("z"));
            
            if (source.equals(pos)) {
                // 点击相同方块：提示不能连接自己
                if (player != null) {
                    player.displayClientMessage(
                        Component.translatable("tooltip.expandedae.binding.self_link"), 
                        true
                    );
                }
                return InteractionResult.SUCCESS;
            }
            
            BlockEntity beSource = level.getBlockEntity(source);
            if (beSource instanceof OmniLaserBeamBlockEntity sourceEntity) {
                // 对于OmniLaserBeamBlockEntity，检查距离限制：水平范围16x16，垂直范围32
                int dx = Math.abs(pos.getX() - source.getX());
                int dy = Math.abs(pos.getY() - source.getY());
                int dz = Math.abs(pos.getZ() - source.getZ());
                
                if (dx > OMNI_RANGE_XZ || dz > OMNI_RANGE_XZ || dy > OMNI_RANGE_Y) {
                    // 超出连接范围
                    if (player != null) {
                        player.displayClientMessage(
                            Component.translatable("tooltip.expandedae.binding.out_of_range"), 
                            true
                        );
                    }
                    return InteractionResult.SUCCESS;
                }
                
                // 检查是否已经连接
                if (sourceEntity.getLinks().contains(pos)) {
                    // 已连接，断开连接
                    sourceEntity.removeLink(pos);
                    if (player != null) {
                        player.displayClientMessage(
                            Component.translatable(
                                "tooltip.expandedae.binding.omni_unlinked", 
                                source.getX(), source.getY(), source.getZ(), 
                                pos.getX(), pos.getY(), pos.getZ()
                            ), 
                            true
                        );
                    }
                } else {
                    // 未连接，建立链接：从源到目标的单向连接
                    sourceEntity.addLink(pos);
                    if (player != null) {
                        player.displayClientMessage(
                            Component.translatable(
                                "tooltip.expandedae.binding.omni_linked", 
                                source.getX(), source.getY(), source.getZ(), 
                                pos.getX(), pos.getY(), pos.getZ()
                            ), 
                            true
                        );
                    }
                }
                // 注意：不清空TAG_SOURCE，保持源信息，可以继续连接/断开其他目标
                return InteractionResult.SUCCESS;
            } else {
                // 源位置不再存在有效的激光线缆
                tag.remove(TAG_SOURCE);
                tag.remove(TAG_SOURCE_TYPE);
                stack.set(
                    net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
                    net.minecraft.world.item.component.CustomData.of(tag)
                );
                if (player != null) {
                    player.displayClientMessage(
                        Component.translatable("tooltip.expandedae.binding.invalid"), 
                        true
                    );
                }
                return InteractionResult.SUCCESS;
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player.isShiftKeyDown()) {
            // Shift+左键：清空已选定的源
            CompoundTag tag = stack.getOrDefault(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
                net.minecraft.world.item.component.CustomData.EMPTY
            ).copyTag();
            
            if (tag.contains(TAG_SOURCE)) {
                tag.remove(TAG_SOURCE);
                tag.remove(TAG_SOURCE_TYPE);
                stack.set(
                    net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
                    net.minecraft.world.item.component.CustomData.of(tag)
                );
                player.displayClientMessage(
                    Component.translatable("tooltip.expandedae.binding.cleared"), 
                    true
                );
                return InteractionResultHolder.consume(stack);
            }
        }
        return super.use(level, player, hand);
    }
}
