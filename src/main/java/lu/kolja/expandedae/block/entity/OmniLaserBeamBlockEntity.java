package lu.kolja.expandedae.block.entity;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import lu.kolja.expandedae.block.OmniLaserBeamBlock;
import lu.kolja.expandedae.laserbeam.ILinkable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

/**
 * 全向激光线缆方块实体
 * 
 * 功能实现：
 * 1. 通过激光绑定工具建立双向对等连接（只能连接一个目标）
 * 2. 维护单个AE网络连接
 * 3. 管理光束的渲染状态
 * 4. 实现ILinkable接口，支持激光绑定工具操作
 */
public class OmniLaserBeamBlockEntity extends AENetworkedBlockEntity implements ILinkable {
    
    /** 连接目标（全向连接器只能连接一个目标） */
    @Nullable
    private BlockPos linkedTarget = null;
    
    /** 当前活跃连接 */
    @Nullable
    private IGridConnection activeConnection = null;
    
    /** 客户端活跃目标（用于渲染） */
    @Nullable
    private BlockPos clientLinkedTarget = null;
    
    /** 上一次暴露的后方方向 */
    @Nullable
    private Direction lastExposedBack;

    /** 是否隐藏光束 */
    private boolean hideBeam;

    public OmniLaserBeamBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.getMainNode().setFlags(GridFlags.DENSE_CAPACITY);
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        Direction facing = this.getBlockState().getValue(OmniLaserBeamBlock.FACING);
        return dir == facing.getOpposite() ? AECableType.SMART : AECableType.NONE;
    }

    @Override
    public Set<Direction> getGridConnectableSides(appeng.api.orientation.BlockOrientation orientation) {
        Direction facing = this.getBlockState().getValue(OmniLaserBeamBlock.FACING);
        return EnumSet.of(facing.getOpposite());
    }

    /**
     * 服务器端tick逻辑
     * 
     * 每tick执行：
     * 1. 同步暴露的后方方向
     * 2. 检查自身网络节点状态
     * 3. 维护与目标的双向连接
     * 4. 更新方块状态
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, OmniLaserBeamBlockEntity be) {
        if (be.isRemoved()) {
            return;
        }

        Direction facing = state.getValue(OmniLaserBeamBlock.FACING);
        be.syncExposedBack(facing.getOpposite());

        IManagedGridNode myManaged = be.getMainNode();
        IGridNode myNode = myManaged.getNode();
        if (myNode == null) {
            be.clearConnection();
            be.updateOwnStatus(OmniLaserBeamBlock.Status.ON);
            return;
        }

        BlockPos targetPos = be.linkedTarget;
        boolean isActive = false;

        if (targetPos != null) {
            if (!level.hasChunkAt(targetPos)) {
                be.clearConnection();
            } else {
                BlockState targetState = level.getBlockState(targetPos);
                if (!(targetState.getBlock() instanceof OmniLaserBeamBlock)) {
                    be.clearLink();
                } else {
                    var targetEntity = level.getBlockEntity(targetPos);
                    if (!(targetEntity instanceof OmniLaserBeamBlockEntity other) || other.isRemoved()) {
                        be.clearConnection();
                    } else {
                        IManagedGridNode otherManaged = other.getMainNode();
                        IGridNode otherNode = otherManaged.getNode();
                        if (otherNode == null) {
                            be.clearConnection();
                        } else if (be.ensureConnection(myNode, otherNode) != null
                                && myManaged.isOnline() && myManaged.isPowered()
                                && otherManaged.isOnline() && otherManaged.isPowered()) {
                            isActive = true;
                        }
                    }
                }
            }
        }

        be.syncActiveTarget(isActive ? targetPos : null);
        be.updateOwnStatus(isActive ? OmniLaserBeamBlock.Status.BEAMING : OmniLaserBeamBlock.Status.ON);
    }

    /**
     * 客户端tick逻辑
     * 
     * 客户端无需逻辑，渲染器会直接读取同步后的可视状态
     */
    public static void clientTick(Level level, BlockPos pos, BlockState state, OmniLaserBeamBlockEntity be) {
        // 客户端无需逻辑
    }

    /**
     * 建立连接（双向对等）
     * 
     * @param other 目标位置
     */
    @Override
    public void addLink(BlockPos other) {
        if (other.equals(this.getBlockPos())) {
            return;
        }

        // 全向连接器只能连接一个目标
        if (this.linkedTarget != null && !this.linkedTarget.equals(other)) {
            // 如果已有连接，先断开
            this.clearLink();
        }

        this.linkedTarget = other;
        this.setChanged();
        this.markForUpdate();
    }

    /**
     * 断开连接
     * 
     * @param other 目标位置
     */
    @Override
    public void removeLink(BlockPos other) {
        if (this.linkedTarget == null || !this.linkedTarget.equals(other)) {
            return;
        }

        this.clearLink();
    }

    /**
     * 获取连接目标
     * 
     * @return 连接目标位置，如果没有连接则返回null
     */
    @Override
    public Set<BlockPos> getLinks() {
        return this.linkedTarget != null ? Set.of(this.linkedTarget) : Set.of();
    }

    /**
     * 获取单个连接目标（用于简化操作）
     * 
     * @return 连接目标位置，如果没有连接则返回null
     */
    @Nullable
    public BlockPos getLinkedTarget() {
        return this.linkedTarget;
    }

    /**
     * 获取客户端连接目标（用于渲染）
     * 
     * @return 连接目标位置，如果没有连接则返回null
     */
    @Nullable
    public BlockPos getClientLinkedTarget() {
        return this.clientLinkedTarget;
    }

    /**
     * 是否已连接
     * 
     * @return 如果已连接返回true
     */
    public boolean isLinked() {
        return this.linkedTarget != null;
    }

    /**
     * 清除连接（内部使用）
     */
    private void clearLink() {
        this.linkedTarget = null;
        this.clearConnection();
        this.setChanged();
        this.markForUpdate();
    }

    /**
     * 清除AE网络连接但不清除目标记录
     */
    private void clearConnection() {
        if (this.activeConnection != null) {
            try {
                this.activeConnection.destroy();
            } catch (Exception ignored) {
            }
            this.activeConnection = null;
        }
    }

    public boolean isHideBeam() {
        return hideBeam;
    }

    public boolean shouldRenderBeam() {
        return !hideBeam && clientLinkedTarget != null;
    }

    public void toggleBeamVisibility() {
        setBeamHidden(!hideBeam);
    }

    public void setBeamHidden(boolean hidden) {
        if (this.hideBeam != hidden) {
            this.hideBeam = hidden;
            this.setChanged();
            this.markForUpdate();
        }
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(this.linkedTarget != null);
        if (this.linkedTarget != null) {
            data.writeBlockPos(this.linkedTarget);
        }
        data.writeBoolean(this.hideBeam);
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean changed = super.readFromStream(data);
        boolean hasTarget = data.readBoolean();
        BlockPos newTarget = hasTarget ? data.readBlockPos() : null;
        boolean targetsChanged = (newTarget == null) != (this.clientLinkedTarget == null) 
                || (newTarget != null && !newTarget.equals(this.clientLinkedTarget));
        this.clientLinkedTarget = newTarget;
        
        boolean receivedHideBeam = data.readBoolean();
        boolean hideBeamChanged = this.hideBeam != receivedHideBeam;
        this.hideBeam = receivedHideBeam;
        
        return changed || targetsChanged || hideBeamChanged;
    }

    @Override
    public void onChunkUnloaded() {
        this.clearConnection();
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        this.clearConnection();
        super.setRemoved();
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.linkedTarget != null) {
            CompoundTag targetTag = new CompoundTag();
            targetTag.putInt("x", this.linkedTarget.getX());
            targetTag.putInt("y", this.linkedTarget.getY());
            targetTag.putInt("z", this.linkedTarget.getZ());
            tag.put("linkedTarget", targetTag);
        }
        tag.putBoolean("hideBeam", hideBeam);
    }

    @Override
    public void loadTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadTag(tag, registries);
        this.linkedTarget = null;
        this.activeConnection = null;
        this.clientLinkedTarget = null;
        this.lastExposedBack = null;
        this.hideBeam = tag.getBoolean("hideBeam");

        if (tag.contains("linkedTarget")) {
            CompoundTag targetTag = tag.getCompound("linkedTarget");
            this.linkedTarget = new BlockPos(
                    targetTag.getInt("x"),
                    targetTag.getInt("y"),
                    targetTag.getInt("z"));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        BlockPos pos = this.getBlockPos();
        if (this.clientLinkedTarget == null) {
            return new AABB(
                    pos.getX() - 5, pos.getY() - 5, pos.getZ() - 5,
                    pos.getX() + 6, pos.getY() + 6, pos.getZ() + 6);
        }

        double minX = Math.min(pos.getX(), this.clientLinkedTarget.getX());
        double minY = Math.min(pos.getY(), this.clientLinkedTarget.getY());
        double minZ = Math.min(pos.getZ(), this.clientLinkedTarget.getZ());
        double maxX = Math.max(pos.getX() + 1, this.clientLinkedTarget.getX() + 1);
        double maxY = Math.max(pos.getY() + 1, this.clientLinkedTarget.getY() + 1);
        double maxZ = Math.max(pos.getZ() + 1, this.clientLinkedTarget.getZ() + 1);

        double expansion = 2.0;
        return new AABB(
                minX - expansion, minY - expansion, minZ - expansion,
                maxX + expansion, maxY + expansion, maxZ + expansion);
    }

    private void syncActiveTarget(@Nullable BlockPos target) {
        if ((target == null) != (this.linkedTarget == null) 
                || (target != null && !target.equals(this.linkedTarget))) {
            this.markForUpdate();
        }
    }

    private void syncExposedBack(Direction back) {
        if (this.lastExposedBack != back) {
            this.getMainNode().setExposedOnSides(EnumSet.of(back));
            this.lastExposedBack = back;
        }
    }

    private void updateOwnStatus(OmniLaserBeamBlock.Status status) {
        if (this.level == null || this.isRemoved()) {
            return;
        }

        BlockState currentState = this.getBlockState();
        if (currentState.getBlock() instanceof OmniLaserBeamBlock
                && currentState.getValue(OmniLaserBeamBlock.STATUS) != status) {
            this.level.setBlock(this.getBlockPos(), currentState.setValue(OmniLaserBeamBlock.STATUS, status), 3);
        }
    }

    @Nullable
    private IGridConnection ensureConnection(IGridNode myNode, IGridNode otherNode) {
        // 检查现有连接是否有效
        if (this.activeConnection != null) {
            if (myNode.getConnections().contains(this.activeConnection)) {
                IGridNode otherSide = this.activeConnection.getOtherSide(myNode);
                if (otherSide == otherNode) {
                    return this.activeConnection;
                }
            }
            // 连接无效，销毁
            try {
                this.activeConnection.destroy();
            } catch (Exception ignored) {
            }
            this.activeConnection = null;
        }

        // 查找是否已有连接
        for (IGridConnection conn : myNode.getConnections()) {
            if (conn.getOtherSide(myNode) == otherNode) {
                this.activeConnection = conn;
                return conn;
            }
        }

        // 创建新连接
        try {
            this.activeConnection = GridHelper.createConnection(myNode, otherNode);
            return this.activeConnection;
        } catch (Exception ignored) {
            return null;
        }
    }
}
