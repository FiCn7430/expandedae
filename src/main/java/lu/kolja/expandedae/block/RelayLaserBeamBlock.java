package lu.kolja.expandedae.block;

import appeng.block.AEBaseEntityBlock;
import lu.kolja.expandedae.block.entity.RelayLaserBeamBlockEntity;
import lu.kolja.expandedae.definition.ExpBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;

import java.util.EnumMap;
import java.util.Map;

/**
 * 中继激光线缆方块
 *
 * 功能：作为全向激光线缆的中继节点
 * - 可以被从连接器连接（作为目标）
 * - 可以作为主连接器连接多个从连接器
 * - 不能作为从连接器再次连接其他中继
 * - 支持Shift+右键切换光束显示/隐藏
 */
public class RelayLaserBeamBlock extends AEBaseEntityBlock<RelayLaserBeamBlockEntity> {

    /** 朝向属性 */
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    /**
     * 激光线缆状态枚举
     */
    public enum Status implements StringRepresentable {
        OFF("off"),      // 未通电/离线
        ON("on"),        // 在线但未连接
        BEAMING("beaming"); // 正在传输光束

        private final String name;

        Status(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /** 状态属性 */
    public static final EnumProperty<Status> STATUS = EnumProperty.create("status", Status.class);

    /** 为每个朝向缓存的碰撞箱 */
    private static final Map<Direction, VoxelShape> SHAPES;

    static {
        SHAPES = new EnumMap<>(Direction.class);

        // 为每个朝向创建对应的形状
        for (Direction facing : Direction.values()) {
            VoxelShape shape;
            switch (facing) {
                case NORTH:
                    shape = Block.box(2, 2, 0, 14, 14, 8);
                    break;
                case SOUTH:
                    shape = Block.box(2, 2, 8, 14, 14, 16);
                    break;
                case WEST:
                    shape = Block.box(0, 2, 2, 8, 14, 14);
                    break;
                case EAST:
                    shape = Block.box(8, 2, 2, 16, 14, 14);
                    break;
                case UP:
                    shape = Block.box(2, 8, 2, 14, 16, 14);
                    break;
                case DOWN:
                    shape = Block.box(2, 0, 2, 14, 8, 14);
                    break;
                default:
                    shape = Block.box(2, 2, 8, 14, 14, 16);
                    break;
            }
            SHAPES.put(facing, shape);
        }
    }

    public RelayLaserBeamBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.SOUTH)
                .setValue(STATUS, Status.OFF));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STATUS);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RelayLaserBeamBlockEntity(ExpBlockEntities.RELAY_LASER_BEAM_BE.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> {
            if (be instanceof RelayLaserBeamBlockEntity relayBe) {
                if (lvl.isClientSide) {
                    RelayLaserBeamBlockEntity.clientTick(lvl, pos, st, relayBe);
                } else {
                    RelayLaserBeamBlockEntity.serverTick(lvl, pos, st, relayBe);
                }
            }
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction face = ctx.getNearestLookingDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, face).setValue(STATUS, Status.OFF);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return false;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return SHAPES.get(facing);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return SHAPES.get(facing);
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        // 方块发光亮度设置为15（最大亮度）
        return 15;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        // 通知相邻方块，让线缆可以连接
        if (!level.isClientSide()) {
            level.blockUpdated(pos, this);
            for (Direction dir : Direction.values()) {
                level.updateNeighborsAt(pos.relative(dir), this);
            }
        }
    }

    /**
     * 处理玩家使用方块（右键点击，无物品）
     *
     * Shift+右键切换光束显示/隐藏
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        // 只处理Shift+右键
        if (!player.isShiftKeyDown()) {
            return super.useWithoutItem(state, level, pos, player, hitResult);
        }

        // 获取方块实体
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RelayLaserBeamBlockEntity relayBe)) {
            return super.useWithoutItem(state, level, pos, player, hitResult);
        }

        // 切换光束显示状态
        if (!level.isClientSide) {
            relayBe.toggleBeamVisibility();
        }

        return InteractionResult.SUCCESS;
    }
}
