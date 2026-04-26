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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 全向激光线缆方块实体
 * 
 * 功能实现：
 * 1. 可以通过激光绑定工具手动配置多个连接目标
 * 2. 同时维护多个AE网络连接
 * 3. 管理多个光束的渲染状态
 * 4. 实现ILinkable接口，支持激光绑定工具操作
 */
public class OmniLaserBeamBlockEntity extends AENetworkedBlockEntity implements ILinkable {
    
    /** 目标排序比较器 */
    private static final Comparator<BlockPos> TARGET_ORDER = Comparator.comparingLong(BlockPos::asLong);
    
    /** 连接目标集合 */
    private final Set<BlockPos> links = new HashSet<>();
    
    /** 位置到连接的映射 */
    private final Map<BlockPos, IGridConnection> connections = new HashMap<>();
    
    /** 当前活跃的目标列表 */
    private List<BlockPos> activeTargets = List.of();
    
    /** 客户端活跃目标列表（用于渲染） */
    private List<BlockPos> clientActiveTargets = List.of();
    
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
     * 3. 遍历所有连接目标，维护连接
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
            be.clearRuntimeState();
            be.updateOwnStatus(OmniLaserBeamBlock.Status.ON);
            return;
        }

        List<BlockPos> activeNow = new ArrayList<>();
        for (BlockPos targetPos : be.getSortedLinks()) {
            if (!level.hasChunkAt(targetPos)) {
                be.releaseConnection(targetPos, myNode, true);
                continue;
            }

            BlockState targetState = level.getBlockState(targetPos);
            if (!(targetState.getBlock() instanceof OmniLaserBeamBlock)) {
                be.removeLink(targetPos);
                continue;
            }

            var targetEntity = level.getBlockEntity(targetPos);
            if (!(targetEntity instanceof OmniLaserBeamBlockEntity other) || other == be || other.isRemoved()) {
                be.releaseConnection(targetPos, myNode, true);
                continue;
            }

            IManagedGridNode otherManaged = other.getMainNode();
            IGridNode otherNode = otherManaged.getNode();
            if (otherNode == null) {
                be.releaseConnection(targetPos, myNode, true);
                continue;
            }

            if (be.ensureConnection(targetPos, myNode, otherNode) != null
                    && myManaged.isOnline() && myManaged.isPowered()
                    && otherManaged.isOnline() && otherManaged.isPowered()) {
                activeNow.add(targetPos);
            }
        }

        be.syncActiveTargets(activeNow);
        be.updateOwnStatus(activeNow.isEmpty() ? OmniLaserBeamBlock.Status.ON : OmniLaserBeamBlock.Status.BEAMING);
    }

    /**
     * 客户端tick逻辑
     * 
     * 客户端无需逻辑，渲染器会直接读取同步后的可视状态
     */
    public static void clientTick(Level level, BlockPos pos, BlockState state, OmniLaserBeamBlockEntity be) {
        // 客户端无需逻辑
    }

    @Override
    public void addLink(BlockPos other) {
        if (other.equals(this.getBlockPos())) {
            return;
        }

        if (this.links.add(other)) {
            this.setChanged();
        }
    }

    @Override
    public void removeLink(BlockPos other) {
        if (!this.links.remove(other)) {
            return;
        }

        this.releaseConnection(other, this.getMainNode().getNode(), true);
        this.removeActiveTarget(other);
        this.setChanged();
    }

    @Override
    public Set<BlockPos> getLinks() {
        return Collections.unmodifiableSet(this.links);
    }

    public List<BlockPos> getClientActiveTargets() {
        return this.clientActiveTargets;
    }

    public boolean isHideBeam() {
        return hideBeam;
    }

    public boolean shouldRenderBeam() {
        return !hideBeam && !clientActiveTargets.isEmpty();
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
        data.writeVarInt(this.activeTargets.size());
        for (BlockPos p : this.activeTargets) {
            data.writeBlockPos(p);
        }
        data.writeBoolean(this.hideBeam);
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean changed = super.readFromStream(data);
        int count = data.readVarInt();
        List<BlockPos> updatedTargets = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            updatedTargets.add(data.readBlockPos());
        }

        List<BlockPos> immutableTargets = updatedTargets.isEmpty() ? List.of() : List.copyOf(updatedTargets);
        boolean targetsChanged = !immutableTargets.equals(this.clientActiveTargets);
        this.clientActiveTargets = immutableTargets;
        
        boolean receivedHideBeam = data.readBoolean();
        boolean hideBeamChanged = this.hideBeam != receivedHideBeam;
        this.hideBeam = receivedHideBeam;
        
        return changed || targetsChanged || hideBeamChanged;
    }

    @Override
    public void onChunkUnloaded() {
        this.disconnectAll();
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        this.disconnectAll();
        super.setRemoved();
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (BlockPos p : this.links) {
            CompoundTag targetTag = new CompoundTag();
            targetTag.putInt("x", p.getX());
            targetTag.putInt("y", p.getY());
            targetTag.putInt("z", p.getZ());
            list.add(targetTag);
        }
        tag.put("links", list);
        tag.putBoolean("hideBeam", hideBeam);
    }

    @Override
    public void loadTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadTag(tag, registries);
        this.links.clear();
        this.connections.clear();
        this.activeTargets = List.of();
        this.clientActiveTargets = List.of();
        this.lastExposedBack = null;
        this.hideBeam = tag.getBoolean("hideBeam");

        if (tag.contains("links", Tag.TAG_LIST)) {
            ListTag list = tag.getList("links", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag targetTag = list.getCompound(i);
                this.links.add(new BlockPos(
                        targetTag.getInt("x"),
                        targetTag.getInt("y"),
                        targetTag.getInt("z")));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        if (this.clientActiveTargets.isEmpty()) {
            BlockPos pos = this.getBlockPos();
            return new AABB(
                    pos.getX() - 5, pos.getY() - 5, pos.getZ() - 5,
                    pos.getX() + 6, pos.getY() + 6, pos.getZ() + 6);
        }

        BlockPos pos = this.getBlockPos();
        double minX = pos.getX();
        double minY = pos.getY();
        double minZ = pos.getZ();
        double maxX = pos.getX() + 1;
        double maxY = pos.getY() + 1;
        double maxZ = pos.getZ() + 1;

        for (BlockPos target : this.clientActiveTargets) {
            minX = Math.min(minX, target.getX());
            minY = Math.min(minY, target.getY());
            minZ = Math.min(minZ, target.getZ());
            maxX = Math.max(maxX, target.getX() + 1);
            maxY = Math.max(maxY, target.getY() + 1);
            maxZ = Math.max(maxZ, target.getZ() + 1);
        }

        double expansion = 2.0;
        return new AABB(
                minX - expansion, minY - expansion, minZ - expansion,
                maxX + expansion, maxY + expansion, maxZ + expansion);
    }

    private void disconnectAll() {
        IGridNode myNode = this.getMainNode().getNode();
        if (myNode != null) {
            for (IGridConnection connection : new HashSet<>(this.connections.values())) {
                if (this.isLiveConnection(connection, myNode, null)) {
                    this.destroyConnection(connection);
                }
            }
        }

        this.clearRuntimeState();
    }

    private void clearRuntimeState() {
        this.connections.clear();
        this.syncActiveTargets(List.of());
    }

    @Nullable
    private IGridConnection ensureConnection(BlockPos targetPos, IGridNode myNode, IGridNode otherNode) {
        IGridConnection cachedConnection = this.connections.get(targetPos);
        if (this.isLiveConnection(cachedConnection, myNode, otherNode)) {
            return cachedConnection;
        }

        if (this.isLiveConnection(cachedConnection, myNode, null)) {
            this.destroyConnection(cachedConnection);
        }
        this.connections.remove(targetPos);

        IGridConnection liveConnection = this.findLiveConnection(myNode, otherNode);
        if (liveConnection == null) {
            try {
                liveConnection = GridHelper.createConnection(myNode, otherNode);
            } catch (IllegalStateException ignored) {
                liveConnection = this.findLiveConnection(myNode, otherNode);
            }
        }

        if (liveConnection != null) {
            this.connections.put(targetPos, liveConnection);
        }
        return liveConnection;
    }

    private void releaseConnection(BlockPos targetPos, @Nullable IGridNode myNode, boolean destroyLiveConnection) {
        IGridConnection cachedConnection = this.connections.remove(targetPos);
        if (!destroyLiveConnection || myNode == null) {
            return;
        }

        if (this.isLiveConnection(cachedConnection, myNode, null)) {
            this.destroyConnection(cachedConnection);
            return;
        }

        IGridNode targetNode = this.getTargetNode(targetPos);
        if (targetNode == null) {
            return;
        }

        IGridConnection liveConnection = this.findLiveConnection(myNode, targetNode);
        if (liveConnection != null) {
            this.destroyConnection(liveConnection);
        }
    }

    @Nullable
    private IGridNode getTargetNode(BlockPos targetPos) {
        if (this.level == null || !this.level.hasChunkAt(targetPos)) {
            return null;
        }

        var targetEntity = this.level.getBlockEntity(targetPos);
        if (targetEntity instanceof OmniLaserBeamBlockEntity other && !other.isRemoved()) {
            return other.getMainNode().getNode();
        }

        return null;
    }

    private void syncActiveTargets(List<BlockPos> activeNow) {
        List<BlockPos> immutableTargets = activeNow.isEmpty() ? List.of() : List.copyOf(activeNow);
        if (!immutableTargets.equals(this.activeTargets)) {
            this.activeTargets = immutableTargets;
            this.markForUpdate();
        }
    }

    private void removeActiveTarget(BlockPos targetPos) {
        if (!this.activeTargets.contains(targetPos)) {
            return;
        }

        List<BlockPos> updatedTargets = new ArrayList<>(this.activeTargets);
        updatedTargets.remove(targetPos);
        this.syncActiveTargets(updatedTargets);
        this.updateOwnStatus(updatedTargets.isEmpty()
                ? OmniLaserBeamBlock.Status.ON
                : OmniLaserBeamBlock.Status.BEAMING);
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

    private List<BlockPos> getSortedLinks() {
        List<BlockPos> sortedLinks = new ArrayList<>(this.links);
        sortedLinks.sort(TARGET_ORDER);
        return sortedLinks;
    }

    @Nullable
    private IGridConnection findLiveConnection(IGridNode myNode, IGridNode otherNode) {
        for (IGridConnection connection : myNode.getConnections()) {
            if (this.getOtherSide(connection, myNode) == otherNode) {
                return connection;
            }
        }
        return null;
    }

    private boolean isLiveConnection(
            @Nullable IGridConnection connection,
            IGridNode myNode,
            @Nullable IGridNode expectedOtherNode) {
        if (connection == null || !myNode.getConnections().contains(connection)) {
            return false;
        }

        IGridNode actualOtherNode = this.getOtherSide(connection, myNode);
        if (actualOtherNode == null) {
            return false;
        }

        return expectedOtherNode == null || actualOtherNode == expectedOtherNode;
    }

    @Nullable
    private IGridNode getOtherSide(IGridConnection connection, IGridNode myNode) {
        try {
            return connection.getOtherSide(myNode);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
            return null;
        }
    }

    private void destroyConnection(IGridConnection connection) {
        try {
            connection.destroy();
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }
}
