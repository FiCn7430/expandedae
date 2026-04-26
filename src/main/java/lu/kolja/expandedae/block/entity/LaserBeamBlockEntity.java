package lu.kolja.expandedae.block.entity;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.orientation.BlockOrientation;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import lu.kolja.expandedae.block.LaserBeamBlock;
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
 * 定向激光线缆方块实体
 * 
 * 功能实现：
 * 1. 在指定方向上扫描另一个定向激光线缆
 * 2. 当找到目标时建立AE网络连接
 * 3. 管理光束的渲染状态
 */
public class LaserBeamBlockEntity extends AENetworkedBlockEntity {
    
    /** 最大光束传输距离 */
    private static final int MAX_BEAM_RANGE = 32;
    
    /** 当前光束长度（用于渲染） */
    private int beamLength;
    
    /** 当前AE网格连接 */
    @Nullable
    private IGridConnection connection;
    
    /** 连接的另一个激光线缆 */
    @Nullable
    private LaserBeamBlockEntity other;
    
    /** 上一次暴露的后方方向 */
    @Nullable
    private Direction lastExposedBack;

    public LaserBeamBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        getMainNode().setFlags(GridFlags.DENSE_CAPACITY);
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return dir == getFacing().getOpposite() ? AECableType.SMART : AECableType.NONE;
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return EnumSet.of(getFacing().getOpposite());
    }

    public int getBeamLength() {
        return beamLength;
    }

    /**
     * 服务器端tick逻辑
     * 
     * 每tick执行：
     * 1. 同步暴露的后方方向
     * 2. 检查自身网络节点状态
     * 3. 扫描前方目标
     * 4. 建立或维护连接
     * 5. 更新方块状态
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, LaserBeamBlockEntity be) {
        if (be.isRemoved()) {
            return;
        }

        Direction facing = state.getValue(LaserBeamBlock.FACING);
        be.syncExposedBack(facing.getOpposite());

        IGridNode myNode = be.getMainNode().getNode();
        if (myNode == null) {
            LaserBeamBlockEntity partner = be.other;
            be.disconnect();
            be.applyVisualState(partner, 0);
            be.updateStatus(partner, LaserBeamBlock.Status.ON);
            return;
        }

        ScanResult scan = be.scanForTarget(level, pos, facing);
        if (!be.hasConnectableTarget(scan.target)) {
            LaserBeamBlockEntity partner = scan.target != null ? scan.target : be.other;
            be.disconnect();
            be.applyVisualState(partner, 0);
            be.updateStatus(partner, LaserBeamBlock.Status.ON);
            return;
        }

        if (!be.ensureConnection(scan.target, scan.length)) {
            LaserBeamBlockEntity partner = scan.target;
            be.disconnect();
            be.applyVisualState(partner, 0);
            be.updateStatus(partner, LaserBeamBlock.Status.ON);
            return;
        }

        if (be.hasActiveBeam(scan.target)) {
            be.applyVisualState(scan.target, scan.length);
            be.updateStatus(scan.target, LaserBeamBlock.Status.BEAMING);
        } else {
            be.applyVisualState(scan.target, 0);
            be.updateStatus(scan.target, LaserBeamBlock.Status.ON);
        }
    }

    /**
     * 客户端tick逻辑
     * 
     * 客户端无需逻辑，渲染器会直接读取同步后的可视状态
     */
    public static void clientTick(Level level, BlockPos pos, BlockState state, LaserBeamBlockEntity be) {
        // 客户端无需逻辑
    }

    /**
     * 断开当前连接
     * 
     * 清理连接状态并通知对方
     */
    public void disconnect() {
        LaserBeamBlockEntity partner = other != null ? other : findConnectedPeer();
        IGridConnection activeConnection = connection;

        boolean selfChanged = clearRuntimeState();
        boolean partnerChanged = false;
        if (partner != null && (partner.other == this || partner.connection == activeConnection)) {
            partnerChanged = partner.clearRuntimeState();
        }

        if (activeConnection != null) {
            try {
                activeConnection.destroy();
            } catch (IllegalStateException ignored) {
            }
        }

        if (selfChanged) {
            markVisualChanged();
        }
        if (partnerChanged) {
            partner.markVisualChanged();
        }
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeVarInt(beamLength);
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean changed = super.readFromStream(data);
        int oldLength = beamLength;
        beamLength = data.readVarInt();
        return changed || oldLength != beamLength;
    }

    @Override
    public void onChunkUnloaded() {
        disconnect();
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        disconnect();
        super.setRemoved();
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
    }

    @Override
    public void loadTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadTag(tag, registries);
        beamLength = 0;
        connection = null;
        other = null;
    }

    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        BlockPos pos = getBlockPos();
        int len = Math.max(0, beamLength);
        if (len <= 0) {
            return new AABB(pos.getX() - 5, pos.getY() - 5, pos.getZ() - 5,
                    pos.getX() + 6, pos.getY() + 6, pos.getZ() + 6);
        }

        Direction dir = getFacing();
        BlockPos endPos = pos.relative(dir, len);
        double minX = Math.min(pos.getX(), endPos.getX());
        double minY = Math.min(pos.getY(), endPos.getY());
        double minZ = Math.min(pos.getZ(), endPos.getZ());
        double maxX = Math.max(pos.getX() + 1, endPos.getX() + 1);
        double maxY = Math.max(pos.getY() + 1, endPos.getY() + 1);
        double maxZ = Math.max(pos.getZ() + 1, endPos.getZ() + 1);

        double expansion = 2.0;
        return new AABB(minX - expansion, minY - expansion, minZ - expansion,
                maxX + expansion, maxY + expansion, maxZ + expansion);
    }

    /**
     * 确保与目标建立连接
     * 
     * @param target 目标激光线缆
     * @param len 光束长度
     * @return 是否成功建立连接
     */
    private boolean ensureConnection(LaserBeamBlockEntity target, int len) {
        IGridNode myNode = getMainNode().getNode();
        IGridNode otherNode = target.getMainNode().getNode();
        if (myNode == null || otherNode == null) {
            return false;
        }

        if (other != null && other != target) {
            disconnect();
        }
        if (target.other != null && target.other != this) {
            target.disconnect();
        }

        if (isConnectionToDifferentNode(connection, myNode, otherNode)) {
            disconnect();
        }
        if (target.isConnectionToDifferentNode(target.connection, otherNode, myNode)) {
            target.disconnect();
        }

        IGridConnection activeConnection = findConnection(myNode, otherNode);
        if (activeConnection == null) {
            try {
                activeConnection = GridHelper.createConnection(myNode, otherNode);
            } catch (IllegalStateException ignored) {
                activeConnection = findConnection(myNode, otherNode);
            }
        }

        if (activeConnection == null) {
            return false;
        }

        connection = activeConnection;
        other = target;
        target.connection = activeConnection;
        target.other = this;
        return true;
    }

    /**
     * 检查目标是否可连接
     * 
     * @param target 目标激光线缆
     * @return 是否可连接
     */
    private boolean hasConnectableTarget(@Nullable LaserBeamBlockEntity target) {
        if (target == null || target == this || target.isRemoved()) {
            return false;
        }

        return getMainNode().getNode() != null && target.getMainNode().getNode() != null;
    }

    /**
     * 检查是否有活跃的光束
     * 
     * @param target 目标激光线缆
     * @return 双方是否都在线且有能量
     */
    private boolean hasActiveBeam(LaserBeamBlockEntity target) {
        IManagedGridNode myManaged = getMainNode();
        IManagedGridNode targetManaged = target.getMainNode();
        return myManaged.isOnline()
                && myManaged.isPowered()
                && targetManaged.isOnline()
                && targetManaged.isPowered();
    }

    /**
     * 应用可视状态
     * 
     * @param target 目标激光线缆
     * @param newLength 新的光束长度
     */
    private void applyVisualState(@Nullable LaserBeamBlockEntity target, int newLength) {
        boolean selfChanged = beamLength != newLength;
        beamLength = newLength;
        if (selfChanged) {
            markVisualChanged();
        }

        if (target != null) {
            boolean targetChanged = target.beamLength != newLength;
            target.beamLength = newLength;
            if (targetChanged) {
                target.markVisualChanged();
            }
        }
    }

    /**
     * 同步暴露的后方方向
     * 
     * @param back 后方方向
     */
    private void syncExposedBack(Direction back) {
        if (lastExposedBack != back) {
            getMainNode().setExposedOnSides(EnumSet.of(back));
            lastExposedBack = back;
        }
    }

    /**
     * 更新状态
     * 
     * @param target 目标激光线缆
     * @param status 新状态
     */
    private void updateStatus(@Nullable LaserBeamBlockEntity target, LaserBeamBlock.Status status) {
        updateOwnStatus(status);
        if (target != null) {
            target.updateOwnStatus(status);
        }
    }

    /**
     * 更新自身状态
     * 
     * @param status 新状态
     */
    private void updateOwnStatus(LaserBeamBlock.Status status) {
        if (level == null || isRemoved()) {
            return;
        }

        BlockState state = getBlockState();
        if (state.getBlock() instanceof LaserBeamBlock && state.getValue(LaserBeamBlock.STATUS) != status) {
            level.setBlock(getBlockPos(), state.setValue(LaserBeamBlock.STATUS, status), 3);
        }
    }

    /**
     * 扫描前方的目标
     * 
     * @param level 世界
     * @param pos 当前位置
     * @param facing 朝向
     * @return 扫描结果
     */
    private ScanResult scanForTarget(Level level, BlockPos pos, Direction facing) {
        BlockPos cur = pos;

        for (int i = 0; i < MAX_BEAM_RANGE; i++) {
            cur = cur.relative(facing);
            BlockState state = level.getBlockState(cur);
            var blockEntity = level.getBlockEntity(cur);

            if (state.canOcclude() && !state.isAir()) {
                if (blockEntity instanceof LaserBeamBlockEntity otherBe) {
                    Direction otherFacing = state.getValue(LaserBeamBlock.FACING);
                    if (otherFacing == facing.getOpposite()) {
                        return new ScanResult(otherBe, i);
                    }
                }
                return ScanResult.NONE;
            }

            if (blockEntity instanceof LaserBeamBlockEntity otherBe) {
                Direction otherFacing = otherBe.getFacing();
                if (otherFacing == facing) {
                    return ScanResult.NONE;
                }
                if (otherFacing == facing.getOpposite()) {
                    return new ScanResult(otherBe, i + 1);
                }
            }
        }

        return ScanResult.NONE;
    }

    /**
     * 获取当前朝向
     * 
     * @return 朝向
     */
    private Direction getFacing() {
        return getBlockState().getValue(LaserBeamBlock.FACING);
    }

    /**
     * 清理运行时状态
     * 
     * @return 状态是否改变
     */
    private boolean clearRuntimeState() {
        boolean changed = connection != null || other != null || beamLength != 0;
        connection = null;
        other = null;
        beamLength = 0;
        return changed;
    }

    /**
     * 查找已连接的对等方
     * 
     * @return 对等方实体，如果没有则返回null
     */
    @Nullable
    private LaserBeamBlockEntity findConnectedPeer() {
        IGridConnection activeConnection = connection;
        IGridNode myNode = getMainNode().getNode();
        if (activeConnection == null || myNode == null) {
            return null;
        }

        try {
            IGridNode otherNode = activeConnection.getOtherSide(myNode);
            if (otherNode != null && otherNode.getOwner() instanceof LaserBeamBlockEntity otherBe) {
                return otherBe;
            }
        } catch (IllegalStateException ignored) {
        }

        return null;
    }

    /**
     * 查找两个节点之间的连接
     * 
     * @param aNode 节点A
     * @param bNode 节点B
     * @return 连接，如果没有则返回null
     */
    @Nullable
    private IGridConnection findConnection(IGridNode aNode, IGridNode bNode) {
        return aNode.getConnections().stream()
                .filter(connection -> getOtherSide(connection, aNode) == bNode)
                .findFirst()
                .orElse(null);
    }

    /**
     * 检查连接是否指向不同的节点
     * 
     * @param activeConnection 当前连接
     * @param selfNode 自身节点
     * @param expectedOtherNode 期望的对端节点
     * @return 是否指向不同的节点
     */
    private boolean isConnectionToDifferentNode(@Nullable IGridConnection activeConnection, IGridNode selfNode,
            IGridNode expectedOtherNode) {
        if (activeConnection == null) {
            return false;
        }

        IGridNode actualOtherNode = getOtherSide(activeConnection, selfNode);
        return actualOtherNode != null && actualOtherNode != expectedOtherNode;
    }

    /**
     * 获取连接的对端节点
     * 
     * @param activeConnection 当前连接
     * @param selfNode 自身节点
     * @return 对端节点，如果没有则返回null
     */
    @Nullable
    private IGridNode getOtherSide(IGridConnection activeConnection, IGridNode selfNode) {
        try {
            return activeConnection.getOtherSide(selfNode);
        } catch (IllegalStateException ignored) {
            return null;
        }
    }

    /**
     * 标记可视状态改变
     */
    private void markVisualChanged() {
        if (level != null) {
            markForUpdate();
        }
    }

    /**
     * 扫描结果内部类
     */
    private static final class ScanResult {
        private static final ScanResult NONE = new ScanResult(null, 0);

        @Nullable
        private final LaserBeamBlockEntity target;
        private final int length;

        private ScanResult(@Nullable LaserBeamBlockEntity target, int length) {
            this.target = target;
            this.length = length;
        }
    }
}
