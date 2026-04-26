package lu.kolja.expandedae.part;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.items.parts.PartModels;
import appeng.parts.AEBasePart;
import appeng.parts.PartModel;
import lu.kolja.expandedae.client.render.LaserBeamRenderHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static lu.kolja.expandedae.Expandedae.MODID;

/**
 * 激光线缆部件
 * 
 * 可以安装在AE2线缆上的激光传输部件，功能与定向激光线缆方块相同，
 * 但占用空间更小，直接集成在线缆系统中。
 */
public class LaserBeamPart extends AEBasePart implements IGridTickable {
    
    /** 部件模型位置 */
    private static final ResourceLocation MODEL_BASE_LOC =
            ResourceLocation.fromNamespaceAndPath(MODID, "part/laser_beam_part");
    private static final IPartModel MODEL = new PartModel(MODEL_BASE_LOC);
    
    /** 最大光束传输距离 */
    private static final int MAX_BEAM_RANGE = 32;
    
    /** Tick请求配置 */
    private static final TickingRequest TICKING_REQUEST = new TickingRequest(5, 10, false);
    
    /** 当前光束长度 */
    private int beamLength;
    
    /** 连接的另一个激光线缆部件 */
    @Nullable
    private LaserBeamPart other;
    
    /** 当前AE网格连接 */
    @Nullable
    private IGridConnection connection;

    public LaserBeamPart(IPartItem<?> partItem) {
        super(partItem);
        getMainNode()
                .setFlags(GridFlags.DENSE_CAPACITY)
                .addService(IGridTickable.class, this);
    }

    @PartModels
    public static List<IPartModel> getModels() {
        return List.of(MODEL);
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        // 新模型的主体更靠内，发射头会明显向外探出，用三段体积贴近实际轮廓
        bch.addBox(4, 4, 17, 12, 12, 11);
        bch.addBox(5, 5, 19, 11, 11, 17);
        bch.addBox(6, 6, 21, 10, 10, 19);
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 5f;
    }

    @Override
    public AECableType getExternalCableConnectionType() {
        return AECableType.SMART;
    }

    @Override
    public IPartModel getStaticModels() {
        return MODEL;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return TICKING_REQUEST;
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        Level level = getLevelOrNull();
        BlockEntity blockEntity = getBlockEntity();
        Direction side = getSide();
        if (level == null || blockEntity == null || side == null) {
            disconnect();
            return TickRateModulation.SLEEP;
        }

        var scan = scanForTarget(level, blockEntity.getBlockPos(), side);
        if (scan.target == null) {
            disconnect();
            return TickRateModulation.SLOWER;
        }

        ensureConnection(scan.target, scan.length);
        return TickRateModulation.SLOWER;
    }

    public int getBeamLength() {
        return beamLength;
    }

    public boolean shouldRenderBeam() {
        return beamLength > 0 && isPowered();
    }

    @Override
    public int getLightLevel() {
        return shouldRenderBeam() ? 15 : 0;
    }

    @Override
    public boolean requireDynamicRender() {
        return true;
    }

    /**
     * 动态渲染光束
     */
    @Override
    public void renderDynamic(float partialTicks, com.mojang.blaze3d.vertex.PoseStack poseStack,
            net.minecraft.client.renderer.MultiBufferSource buffers, int combinedLightIn, int combinedOverlayIn) {
        if (!shouldRenderBeam()) {
            return;
        }

        BlockEntity blockEntity = getBlockEntity();
        Level level = getLevelOrNull();
        Direction side = getSide();
        if (blockEntity == null || level == null || side == null) {
            return;
        }

        if (!isPathClearForRender(level, blockEntity.getBlockPos(), side, beamLength)) {
            return;
        }

        float[] sourceColor = LaserBeamRenderHelper.getPartHostColor(getHost());
        ScanResult scan = scanForTarget(level, blockEntity.getBlockPos(), side);
        float[] targetColor = scan.target != null && scan.length == beamLength
                ? LaserBeamRenderHelper.getPartHostColor(scan.target.getHost())
                : null;

        // 使用渐变渲染，从源端颜色渐变到目标端颜色
        LaserBeamRenderHelper.renderGradientBeamForPart(
                poseStack,
                buffers,
                side,
                beamLength,
                sourceColor != null ? sourceColor[0] : 1.0f,
                sourceColor != null ? sourceColor[1] : 1.0f,
                sourceColor != null ? sourceColor[2] : 1.0f,
                targetColor != null ? targetColor[0] : 1.0f,
                targetColor != null ? targetColor[1] : 1.0f,
                targetColor != null ? targetColor[2] : 1.0f,
                combinedLightIn,
                combinedOverlayIn);
    }

    @Override
    public void removeFromWorld() {
        disconnect(false);
        super.removeFromWorld();
    }

    @Override
    public void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeVarInt(beamLength);
    }

    @Override
    public boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean redraw = super.readFromStream(data);
        int oldLength = beamLength;
        beamLength = data.readVarInt();
        return redraw || oldLength != beamLength;
    }

    @Override
    public void writeVisualStateToNBT(CompoundTag data) {
        super.writeVisualStateToNBT(data);
        data.putInt("beamLength", beamLength);
    }

    @Override
    public void readVisualStateFromNBT(CompoundTag data) {
        super.readVisualStateFromNBT(data);
        beamLength = data.getInt("beamLength");
    }

    @Override
    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeToNBT(tag, registries);
    }

    @Override
    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.readFromNBT(tag, registries);
        beamLength = 0;
    }

    /**
     * 确保与目标建立连接
     */
    private void ensureConnection(LaserBeamPart target, int length) {
        if (target == this) {
            disconnect();
            return;
        }

        IGridNode myNode = getGridNode();
        IGridNode targetNode = target.getGridNode();
        if (myNode == null || targetNode == null) {
            disconnect();
            return;
        }

        if (other != null && other != target) {
            disconnect();
        }
        if (target.other != null && target.other != this) {
            target.disconnect();
        }

        IGridConnection activeConnection = findConnection(myNode, targetNode);
        if (activeConnection == null) {
            if (isConnectionToDifferentNode(connection, myNode, targetNode)) {
                disconnect();
            }
            if (target.isConnectionToDifferentNode(target.connection, targetNode, myNode)) {
                target.disconnect();
            }

            activeConnection = findConnection(myNode, targetNode);
            if (activeConnection == null) {
                try {
                    activeConnection = GridHelper.createConnection(myNode, targetNode);
                } catch (IllegalStateException ignored) {
                    activeConnection = findConnection(myNode, targetNode);
                }
            }
        }

        if (activeConnection == null) {
            disconnect();
            return;
        }

        bindConnection(target, activeConnection, length);
    }

    /**
     * 绑定连接
     */
    private void bindConnection(LaserBeamPart target, IGridConnection activeConnection, int length) {
        boolean thisChanged = connection != activeConnection || other != target || beamLength != length;
        boolean targetChanged = target.connection != activeConnection || target.other != this
                || target.beamLength != length;

        connection = activeConnection;
        other = target;
        beamLength = length;

        target.connection = activeConnection;
        target.other = this;
        target.beamLength = length;

        if (thisChanged) {
            markStateChanged(false);
        }
        if (targetChanged) {
            target.markStateChanged(false);
        }
    }

    public boolean disconnect() {
        return disconnect(true);
    }

    private boolean disconnect(boolean notifySelf) {
        LaserBeamPart partner = other;
        IGridConnection activeConnection = connection;
        IGridNode myNode = getGridNode();

        boolean selfChanged = clearRuntimeState();
        boolean partnerChanged = false;

        if (partner != null && (partner.other == this || partner.connection == activeConnection)) {
            partnerChanged = partner.clearRuntimeState();
        }

        if (activeConnection != null && myNode != null && getOtherSide(activeConnection, myNode) != null) {
            try {
                activeConnection.destroy();
            } catch (IllegalStateException ignored) {
            }
        }

        if (notifySelf && selfChanged) {
            markStateChanged(false);
        }
        if (partnerChanged) {
            partner.markStateChanged(false);
        }

        return selfChanged || partnerChanged;
    }

    private boolean clearRuntimeState() {
        boolean changed = connection != null || other != null || beamLength != 0;
        connection = null;
        other = null;
        beamLength = 0;
        return changed;
    }

    private void markStateChanged(boolean persist) {
        IPartHost host = getHost();
        if (host == null) {
            return;
        }

        host.markForUpdate();
        if (persist) {
            host.markForSave();
        }
        host.partChanged();
    }

    @Nullable
    private Level getLevelOrNull() {
        BlockEntity blockEntity = getBlockEntity();
        return blockEntity != null ? blockEntity.getLevel() : null;
    }

    private ScanResult scanForTarget(Level level, BlockPos startPos, Direction direction) {
        BlockPos cursor = startPos;

        for (int i = 0; i < MAX_BEAM_RANGE; i++) {
            cursor = cursor.relative(direction);
            BlockState state = level.getBlockState(cursor);
            PartHostScan partHostScan = inspectPartHost(level.getBlockEntity(cursor), direction);

            if (state.canOcclude() && !state.isAir() && !partHostScan.hasBeamFormer) {
                return ScanResult.none();
            }

            if (partHostScan.target != null) {
                return new ScanResult(partHostScan.target, i + 1);
            }
        }

        return ScanResult.none();
    }

    private boolean isPathClearForRender(Level level, BlockPos startPos, Direction direction, int length) {
        BlockPos cursor = startPos;

        for (int i = 0; i < length; i++) {
            cursor = cursor.relative(direction);
            BlockState state = level.getBlockState(cursor);
            if (state.canOcclude() && !state.isAir()
                    && !inspectPartHost(level.getBlockEntity(cursor), direction).hasBeamFormer) {
                return false;
            }
        }

        return true;
    }

    private PartHostScan inspectPartHost(@Nullable BlockEntity blockEntity, Direction direction) {
        if (!(blockEntity instanceof IPartHost partHost)) {
            return PartHostScan.NONE;
        }

        LaserBeamPart target = null;
        boolean hasBeamFormer = false;
        Direction opposite = direction.getOpposite();

        for (Direction side : Direction.values()) {
            var part = partHost.getPart(side);
            if (part instanceof LaserBeamPart beamFormerPart) {
                hasBeamFormer = true;
                if (side == opposite) {
                    target = beamFormerPart;
                }
            }
        }

        return new PartHostScan(target, hasBeamFormer);
    }

    @Nullable
    private IGridConnection findConnection(IGridNode from, IGridNode to) {
        return from.getConnections().stream()
                .filter(connection -> getOtherSide(connection, from) == to)
                .findFirst()
                .orElse(null);
    }

    private boolean isConnectionToDifferentNode(@Nullable IGridConnection activeConnection, IGridNode selfNode,
            IGridNode expectedOtherNode) {
        if (activeConnection == null) {
            return false;
        }

        IGridNode actualOtherNode = getOtherSide(activeConnection, selfNode);
        return actualOtherNode != null && actualOtherNode != expectedOtherNode;
    }

    @Nullable
    private IGridNode getOtherSide(IGridConnection activeConnection, IGridNode node) {
        try {
            return activeConnection.getOtherSide(node);
        } catch (IllegalStateException ignored) {
            return null;
        }
    }

    private static final class ScanResult {
        private static final ScanResult NONE = new ScanResult(null, 0);

        @Nullable
        private final LaserBeamPart target;
        private final int length;

        private ScanResult(@Nullable LaserBeamPart target, int length) {
            this.target = target;
            this.length = length;
        }

        private static ScanResult none() {
            return NONE;
        }
    }

    private static final class PartHostScan {
        private static final PartHostScan NONE = new PartHostScan(null, false);

        @Nullable
        private final LaserBeamPart target;
        private final boolean hasBeamFormer;

        private PartHostScan(@Nullable LaserBeamPart target, boolean hasBeamFormer) {
            this.target = target;
            this.hasBeamFormer = hasBeamFormer;
        }
    }
}
