package lu.kolja.expandedae.item;

import lu.kolja.expandedae.block.entity.OmniLaserBeamBlockEntity;
import lu.kolja.expandedae.block.entity.RelayLaserBeamBlockEntity;
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
 * 功能：用于连接激光线缆
 * 
 * 全向连接器使用方法：
 * 1. Shift+右键点击第一个全向连接器（选中）
 * 2. 右键点击第二个全向连接器（建立双向连接）
 * 3. Shift+右键已连接的任意全向连接器（断开连接）
 * 
 * 中继连接器使用方法：
 * 1. Shift+右键点击中继连接器（选中）
 * 2. 右键点击目标连接器（建立单向连接）
 * 3. 再次右键已连接的目标（断开连接）
 * 
 * Shift+左键空气：清除工具的选中状态
 * 
 * 连接限制：
 * - 水平范围：16格
 * - 垂直范围：32格
 * - 全向连接器：只能连接一个目标
 * - 中继连接器：可以连接多个目标
 */
public class LaserBindingTool extends Item {
    
    /** NBT标签：选中位置 */
    private static final String TAG_SELECTED = "SelectedPos";
    
    /** NBT标签：选中类型 */
    private static final String TAG_SELECTED_TYPE = "SelectedType";
    
    /** 类型：全向连接器 */
    private static final String TYPE_OMNI = "omni";
    
    /** 类型：中继连接器 */
    private static final String TYPE_RELAY = "relay";
    
    /** 最大连接距离（欧几里得距离） */
    private static final double MAX_RANGE = 32.0;

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
        
        boolean hasSelected = tag.contains(TAG_SELECTED);
        
        // 判断点击的方块类型
        boolean isOmni = be instanceof OmniLaserBeamBlockEntity;
        boolean isRelay = be instanceof RelayLaserBeamBlockEntity;
        boolean isLinkable = isOmni || isRelay;
        
        if (player != null && player.isShiftKeyDown()) {
            // Shift+右键逻辑
            return handleShiftRightClick(level, pos, stack, player, be, isOmni, isRelay, hasSelected, tag);
        } else {
            // 普通右键逻辑
            return handleRightClick(level, pos, stack, player, be, isOmni, isRelay, hasSelected, tag);
        }
    }
    
    /**
     * 处理Shift+右键
     * 
     * 逻辑：
     * - 如果点击的是已连接的全向连接器：断开连接
     * - 否则：选中该连接器
     */
    private InteractionResult handleShiftRightClick(Level level, BlockPos pos, ItemStack stack, Player player, 
            BlockEntity be, boolean isOmni, boolean isRelay, boolean hasSelected, CompoundTag tag) {
        
        // 如果点击的是已连接的全向连接器，断开连接
        if (isOmni && be instanceof OmniLaserBeamBlockEntity omniBe) {
            if (omniBe.isLinked()) {
                BlockPos otherPos = omniBe.getLinkedTarget();
                if (otherPos != null) {
                    // 断开双方的连接
                    omniBe.removeLink(otherPos);
                    BlockEntity otherBe = level.getBlockEntity(otherPos);
                    if (otherBe instanceof OmniLaserBeamBlockEntity otherOmni) {
                        otherOmni.removeLink(pos);
                    }
                    
                    // 清除工具的选中状态
                    clearSelection(stack, player);
                    
                    if (player != null) {
                        player.displayClientMessage(
                            Component.translatable("tooltip.expandedae.binding.omni_disconnected", 
                                pos.getX(), pos.getY(), pos.getZ(),
                                otherPos.getX(), otherPos.getY(), otherPos.getZ()), 
                            true
                        );
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        
        // 否则，选中该连接器
        boolean isLinkable = isOmni || isRelay;
        if (!isLinkable) {
            return InteractionResult.PASS;
        }
        
        // 选中连接器
        CompoundTag selectedTag = new CompoundTag();
        selectedTag.putInt("x", pos.getX());
        selectedTag.putInt("y", pos.getY());
        selectedTag.putInt("z", pos.getZ());
        tag.put(TAG_SELECTED, selectedTag);
        
        if (isOmni) {
            tag.putString(TAG_SELECTED_TYPE, TYPE_OMNI);
            saveTag(stack, tag);
            if (player != null) {
                player.displayClientMessage(
                    Component.translatable("tooltip.expandedae.binding.select_omni", pos.getX(), pos.getY(), pos.getZ()), 
                    true
                );
            }
        } else if (isRelay) {
            tag.putString(TAG_SELECTED_TYPE, TYPE_RELAY);
            saveTag(stack, tag);
            if (player != null) {
                player.displayClientMessage(
                    Component.translatable("tooltip.expandedae.binding.select_relay", pos.getX(), pos.getY(), pos.getZ()), 
                    true
                );
            }
        }
        return InteractionResult.CONSUME;
    }
    
    /**
     * 处理普通右键
     * 
     * 逻辑：
     * - 如果没有选中：提示先选中
     * - 如果已选中：尝试建立连接
     */
    private InteractionResult handleRightClick(Level level, BlockPos pos, ItemStack stack, Player player,
            BlockEntity be, boolean isOmni, boolean isRelay, boolean hasSelected, CompoundTag tag) {
        
        boolean isLinkable = isOmni || isRelay;
        
        if (!hasSelected) {
            // 没有选中，提示
            if (isLinkable) {
                if (player != null) {
                    player.displayClientMessage(
                        Component.translatable("tooltip.expandedae.binding.no_selection"), 
                        true
                    );
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        
        // 已选中，获取选中信息
        CompoundTag selectedTag = tag.getCompound(TAG_SELECTED);
        BlockPos selectedPos = new BlockPos(
            selectedTag.getInt("x"), 
            selectedTag.getInt("y"), 
            selectedTag.getInt("z")
        );
        String selectedType = tag.getString(TAG_SELECTED_TYPE);
        
        // 检查是否是同一个方块
        if (selectedPos.equals(pos)) {
            if (player != null) {
                player.displayClientMessage(
                    Component.translatable("tooltip.expandedae.binding.self_link"), 
                    true
                );
            }
            return InteractionResult.SUCCESS;
        }
        
        // 检查选中的是否还存在
        BlockEntity selectedBe = level.getBlockEntity(selectedPos);
        if (selectedBe == null) {
            clearSelection(stack, player);
            if (player != null) {
                player.displayClientMessage(
                    Component.translatable("tooltip.expandedae.binding.selection_invalid"), 
                    true
                );
            }
            return InteractionResult.SUCCESS;
        }
        
        // 根据选中类型处理
        if (TYPE_OMNI.equals(selectedType)) {
            return handleOmniConnection(level, selectedPos, pos, stack, player, be, isOmni);
        } else if (TYPE_RELAY.equals(selectedType)) {
            return handleRelayConnection(level, selectedPos, pos, stack, player, be, isOmni, isRelay);
        }
        
        return InteractionResult.SUCCESS;
    }
    
    /**
     * 处理全向连接器的连接
     * 
     * 规则：
     * - 只能连接另一个全向连接器
     * - 双方都必须未连接
     * - 建立双向连接
     */
    private InteractionResult handleOmniConnection(Level level, BlockPos sourcePos, BlockPos targetPos, 
            ItemStack stack, Player player, BlockEntity targetBe, boolean targetIsOmni) {
        
        // 目标必须是全向连接器
        if (!targetIsOmni || !(targetBe instanceof OmniLaserBeamBlockEntity targetOmni)) {
            if (player != null) {
                player.displayClientMessage(
                    Component.translatable("tooltip.expandedae.binding.omni_only_omni"), 
                    true
                );
            }
            return InteractionResult.SUCCESS;
        }
        
        BlockEntity sourceBe = level.getBlockEntity(sourcePos);
        if (!(sourceBe instanceof OmniLaserBeamBlockEntity sourceOmni)) {
            clearSelection(stack, player);
            if (player != null) {
                player.displayClientMessage(
                    Component.translatable("tooltip.expandedae.binding.selection_invalid"), 
                    true
                );
            }
            return InteractionResult.SUCCESS;
        }
        
        // 检查距离
        if (!checkRange(sourcePos, targetPos)) {
            if (player != null) {
                player.displayClientMessage(
                    Component.translatable("tooltip.expandedae.binding.out_of_range"), 
                    true
                );
            }
            return InteractionResult.SUCCESS;
        }
        
        // 检查双方是否已连接
        if (sourceOmni.isLinked()) {
            if (player != null) {
                player.displayClientMessage(
                    Component.translatable("tooltip.expandedae.binding.source_already_linked"), 
                    true
                );
            }
            return InteractionResult.SUCCESS;
        }
        
        if (targetOmni.isLinked()) {
            if (player != null) {
                player.displayClientMessage(
                    Component.translatable("tooltip.expandedae.binding.target_already_linked"), 
                    true
                );
            }
            return InteractionResult.SUCCESS;
        }
        
        // 建立双向连接
        sourceOmni.addLink(targetPos);
        targetOmni.addLink(sourcePos);
        
        // 清除工具的选中状态
        clearSelection(stack, player);
        
        if (player != null) {
            player.displayClientMessage(
                Component.translatable("tooltip.expandedae.binding.omni_connected", 
                    sourcePos.getX(), sourcePos.getY(), sourcePos.getZ(),
                    targetPos.getX(), targetPos.getY(), targetPos.getZ()), 
                true
            );
        }
        return InteractionResult.SUCCESS;
    }
    
    /**
     * 处理中继连接器的连接
     * 
     * 规则：
     * - 可以连接全向连接器或另一个中继
     * - 建立单向连接（从Relay到目标）
     */
    private InteractionResult handleRelayConnection(Level level, BlockPos relayPos, BlockPos targetPos,
            ItemStack stack, Player player, BlockEntity targetBe, boolean targetIsOmni, boolean targetIsRelay) {
        
        // 目标必须是可连接的
        if (!targetIsOmni && !targetIsRelay) {
            if (player != null) {
                player.displayClientMessage(
                    Component.translatable("tooltip.expandedae.binding.invalid_target"), 
                    true
                );
            }
            return InteractionResult.SUCCESS;
        }
        
        BlockEntity relayBe = level.getBlockEntity(relayPos);
        if (!(relayBe instanceof RelayLaserBeamBlockEntity relay)) {
            clearSelection(stack, player);
            if (player != null) {
                player.displayClientMessage(
                    Component.translatable("tooltip.expandedae.binding.selection_invalid"), 
                    true
                );
            }
            return InteractionResult.SUCCESS;
        }
        
        // 检查距离
        if (!checkRange(relayPos, targetPos)) {
            if (player != null) {
                player.displayClientMessage(
                    Component.translatable("tooltip.expandedae.binding.out_of_range"), 
                    true
                );
            }
            return InteractionResult.SUCCESS;
        }
        
        ILinkable targetLinkable = (ILinkable) targetBe;
        
        // 检查是否已连接
        if (relay.getLinks().contains(targetPos)) {
            // 已连接，断开
            relay.removeLink(targetPos);
            if (player != null) {
                player.displayClientMessage(
                    Component.translatable("tooltip.expandedae.binding.relay_disconnected", 
                        relayPos.getX(), relayPos.getY(), relayPos.getZ(),
                        targetPos.getX(), targetPos.getY(), targetPos.getZ()), 
                    true
                );
            }
        } else {
            // 未连接，建立连接
            relay.addLink(targetPos);
            if (player != null) {
                player.displayClientMessage(
                    Component.translatable("tooltip.expandedae.binding.relay_connected", 
                        relayPos.getX(), relayPos.getY(), relayPos.getZ(),
                        targetPos.getX(), targetPos.getY(), targetPos.getZ()), 
                    true
                );
            }
        }
        
        // 中继连接不清除选中状态，可以继续连接其他目标
        return InteractionResult.SUCCESS;
    }
    
    /**
     * 检查距离是否在范围内（使用欧几里得距离）
     */
    private boolean checkRange(BlockPos from, BlockPos to) {
        // 使用欧几里得距离计算
        double distance = Math.sqrt(from.distSqr(to));
        return distance <= MAX_RANGE;
    }
    
    /**
     * 清除工具的选中状态
     */
    private void clearSelection(ItemStack stack, Player player) {
        CompoundTag tag = stack.getOrDefault(
            net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
            net.minecraft.world.item.component.CustomData.EMPTY
        ).copyTag();
        
        tag.remove(TAG_SELECTED);
        tag.remove(TAG_SELECTED_TYPE);
        saveTag(stack, tag);
    }
    
    /**
     * 保存NBT标签到物品
     */
    private void saveTag(ItemStack stack, CompoundTag tag) {
        stack.set(
            net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
            net.minecraft.world.item.component.CustomData.of(tag)
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player.isShiftKeyDown()) {
            // Shift+左键空气：清除选中状态
            CompoundTag tag = stack.getOrDefault(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
                net.minecraft.world.item.component.CustomData.EMPTY
            ).copyTag();
            
            if (tag.contains(TAG_SELECTED)) {
                clearSelection(stack, player);
                if (player != null) {
                    player.displayClientMessage(
                        Component.translatable("tooltip.expandedae.binding.selection_cleared"), 
                        true
                    );
                }
                return InteractionResultHolder.consume(stack);
            }
        }
        return super.use(level, player, hand);
    }
}
