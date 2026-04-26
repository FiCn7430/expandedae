package lu.kolja.expandedae.client.render;

import lu.kolja.expandedae.block.LaserBeamBlock;
import lu.kolja.expandedae.block.OmniLaserBeamBlock;
import lu.kolja.expandedae.block.entity.OmniLaserBeamBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 全向激光线缆方块实体渲染器
 * 
 * 渲染从全向激光线缆到多个目标的光束
 */
public class OmniLaserBeamBER implements BlockEntityRenderer<OmniLaserBeamBlockEntity> {
    
    /** 全向核心中心偏移 */
    private static final double OMNI_CORE_CENTER_OFFSET = 3.5d / 16.0d;
    
    /** 方块光束中心偏移 */
    private static final double BLOCK_BEAM_CENTER_OFFSET = 0.25d;
    
    /** 向量渲染偏移补偿 */
    private static final double VECTOR_RENDER_SHIFT_COMPENSATION = 0.25d;

    public OmniLaserBeamBER(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(OmniLaserBeamBlockEntity be) {
        return be != null && be.getClientLinkedTarget() != null;
    }

    @Override
    public boolean shouldRender(OmniLaserBeamBlockEntity be, Vec3 cameraPos) {
        if (be == null) {
            return false;
        }

        return isWithinViewDistance(this.getRenderBoundingBox(be), cameraPos, this.getViewDistance());
    }

    @Override
    public AABB getRenderBoundingBox(OmniLaserBeamBlockEntity be) {
        return be != null ? be.getRenderBoundingBox() : AABB.INFINITE;
    }

    @Override
    public void render(OmniLaserBeamBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (be == null) return;
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof OmniLaserBeamBlock)) return;
        Level level = be.getLevel();
        if (level == null) return;

        // 检查是否应该渲染光束
        if (!be.shouldRenderBeam()) return;

        BlockPos pos = be.getBlockPos();
        Direction facing = state.getValue(OmniLaserBeamBlock.FACING);
        float[] sourceColor = LaserBeamRenderHelper.resolveBlockEndpointColor(level, pos);


        
        BlockPos targetPos = be.getClientLinkedTarget();
        if (targetPos == null) return;
        
        Vec3 sourceAnchor = getOmniBeamAnchor(pos, facing);
        
        float thickness = 0.08f;
        BlockState targetState = level.getBlockState(targetPos);
        Vec3 targetAnchor = getTargetAnchor(targetPos, targetState);
        Vec3 beamVector = targetAnchor.subtract(sourceAnchor);

        double vectorLength = beamVector.length();
        if (vectorLength <= 0.2) return;

        Vec3 normalized = beamVector.scale(1.0d / vectorLength);
        Vec3 renderOrigin = sourceAnchor.add(normalized.scale(VECTOR_RENDER_SHIFT_COMPENSATION));

        // 获取目标的颜色
        float[] targetColor = LaserBeamRenderHelper.resolveBlockEndpointColor(level, targetPos);
        
        poseStack.pushPose();
        
        poseStack.translate(
            renderOrigin.x - (pos.getX() + 0.5d), 
            renderOrigin.y - (pos.getY() + 0.5d), 
            renderOrigin.z - (pos.getZ() + 0.5d)
        );
        
        // 使用渐变渲染，从源端颜色渐变到目标端颜色
        LaserBeamRenderHelper.renderGradientBeamVector(
                poseStack,
                buffers,
                (float) beamVector.x,
                (float) beamVector.y,
                (float) beamVector.z,
                sourceColor != null ? sourceColor[0] : 1.0f,
                sourceColor != null ? sourceColor[1] : 1.0f,
                sourceColor != null ? sourceColor[2] : 1.0f,
                targetColor != null ? targetColor[0] : 1.0f,
                targetColor != null ? targetColor[1] : 1.0f,
                targetColor != null ? targetColor[2] : 1.0f,
                packedLight,
                packedOverlay,
                thickness);
        
        poseStack.popPose();
    }

    /**
     * 检查是否在视距内
     */
    private static boolean isWithinViewDistance(AABB bounds, Vec3 cameraPos, double maxDistance) {
        double nearestX = Math.max(bounds.minX, Math.min(cameraPos.x, bounds.maxX));
        double nearestY = Math.max(bounds.minY, Math.min(cameraPos.y, bounds.maxY));
        double nearestZ = Math.max(bounds.minZ, Math.min(cameraPos.z, bounds.maxZ));

        double dx = cameraPos.x - nearestX;
        double dy = cameraPos.y - nearestY;
        double dz = cameraPos.z - nearestZ;
        double maxDistanceSq = maxDistance * maxDistance;
        return dx * dx + dy * dy + dz * dz <= maxDistanceSq;
    }

    /**
     * 获取目标锚点
     */
    private static Vec3 getTargetAnchor(BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof LaserBeamBlock) {
            Direction facing = state.getValue(LaserBeamBlock.FACING);
            return getForwardAnchor(pos, facing, BLOCK_BEAM_CENTER_OFFSET);
        }
        if (state.getBlock() instanceof OmniLaserBeamBlock) {
            Direction facing = state.getValue(OmniLaserBeamBlock.FACING);
            return getOmniBeamAnchor(pos, facing);
        }
        return Vec3.atCenterOf(pos);
    }

    /**
     * 获取全向光束锚点
     */
    private static Vec3 getOmniBeamAnchor(BlockPos pos, Direction facing) {
        return getForwardAnchor(pos, facing, OMNI_CORE_CENTER_OFFSET);
    }

    /**
     * 获取前方锚点
     */
    private static Vec3 getForwardAnchor(BlockPos pos, Direction direction, double offset) {
        return new Vec3(
                pos.getX() + 0.5d + direction.getStepX() * offset,
                pos.getY() + 0.5d + direction.getStepY() * offset,
                pos.getZ() + 0.5d + direction.getStepZ() * offset
        );
    }

}
