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
        
        if (!te.shouldRenderBeam()) return;
        
        Direction dir = state.getValue(LaserBeamBlock.FACING);
        int len = Math.max(0, te.getBeamLength());
        double visibleLen = len;
        if (visibleLen <= 0) return;

        Level level = te.getLevel();
        BlockPos pos = te.getBlockPos();
        if (level == null) return;
        int checkLen = len > 0 ? len : 1;
        if (!isPathClearForRender(level, pos, dir, checkLen)) return;

        float[] sourceColor = LaserBeamRenderHelper.resolveBlockEndpointColor(level, pos);
        float[] targetColor = LaserBeamRenderHelper.resolveBlockEndpointColor(level, pos.relative(dir, len));
        float[] beamColor = LaserBeamRenderHelper.blendEndpointColors(sourceColor, targetColor);

        float thickness = 0.28f;
        LaserBeamRenderHelper.renderColoredBeam(
                poseStack,
                buffers,
                dir,
                visibleLen,
                beamColor[0],
                beamColor[1],
                beamColor[2],
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
