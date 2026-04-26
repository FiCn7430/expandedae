package lu.kolja.expandedae.client.render;

import lu.kolja.expandedae.block.LaserBeamBlock;
import lu.kolja.expandedae.block.entity.LaserBeamBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 定向激光线缆方块实体渲染器
 * 
 * 渲染从激光线缆发射出的光束到目标激光线缆
 */
public class LaserBeamBER implements BlockEntityRenderer<LaserBeamBlockEntity> {

    public LaserBeamBER(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(LaserBeamBlockEntity te) {
        return te != null && te.getBeamLength() > 0;
    }

    @Override
    public void render(LaserBeamBlockEntity te, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (te == null) return;
        BlockState state = te.getBlockState();
        if (!(state.getBlock() instanceof LaserBeamBlock)) return;
        
        // 光束始终渲染（只要有长度）
        
        Direction dir = state.getValue(LaserBeamBlock.FACING);
        int len = Math.max(0, te.getBeamLength());
        double visibleLen = len;
        if (visibleLen <= 0) return;

        Level level = te.getLevel();
        BlockPos pos = te.getBlockPos();
        if (level == null) return;
        int checkLen = len > 0 ? len : 1;
        if (!isPathClearForRender(level, pos, dir, checkLen)) return;

        // 获取源端颜色：从激光线缆后方的AE线缆获取
        float[] sourceColor = LaserBeamRenderHelper.resolveBlockEndpointColor(level, pos);
        // 获取目标端颜色：从目标激光线缆后方的AE线缆获取
        BlockPos targetPos = pos.relative(dir, len);
        float[] targetColor = LaserBeamRenderHelper.resolveBlockEndpointColor(level, targetPos);

        float thickness = 0.28f;
        // 使用渐变渲染，从源端颜色渐变到目标端颜色
        LaserBeamRenderHelper.renderGradientBeam(
                poseStack,
                buffers,
                dir,
                visibleLen,
                sourceColor != null ? sourceColor[0] : 1.0f,
                sourceColor != null ? sourceColor[1] : 1.0f,
                sourceColor != null ? sourceColor[2] : 1.0f,
                targetColor != null ? targetColor[0] : 1.0f,
                targetColor != null ? targetColor[1] : 1.0f,
                targetColor != null ? targetColor[2] : 1.0f,
                packedLight,
                packedOverlay,
                thickness);
    }

    /**
     * 检查渲染路径是否清晰
     * 
     * @param level 世界
     * @param start 起始位置
     * @param dir 方向
     * @param length 长度
     * @return 路径是否清晰
     */
    private boolean isPathClearForRender(Level level, BlockPos start, Direction dir, int length) {
        BlockPos cur = start;
        for (int i = 0; i < length; i++) {
            cur = cur.relative(dir);
            var state = level.getBlockState(cur);
            if (state.canOcclude() && !state.isAir()) {
                var other = level.getBlockEntity(cur);
                if (other instanceof LaserBeamBlockEntity) {
                    Direction otherFacing = level.getBlockState(cur).getValue(LaserBeamBlock.FACING);
                    if (i == length - 1 && otherFacing == dir.getOpposite()) {
                        return true;
                    }
                }
                return false;
            }
            var be = level.getBlockEntity(cur);
            if (be instanceof LaserBeamBlockEntity) {
                Direction otherFacing = level.getBlockState(cur).getValue(LaserBeamBlock.FACING);
                if (otherFacing == dir) return false;
            }
        }
        return true;
    }
}
