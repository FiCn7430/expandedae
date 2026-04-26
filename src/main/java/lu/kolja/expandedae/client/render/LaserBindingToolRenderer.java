package lu.kolja.expandedae.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import lu.kolja.expandedae.block.entity.OmniLaserBeamBlockEntity;
import lu.kolja.expandedae.item.LaserBindingTool;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.OptionalDouble;

/**
 * 激光绑定工具高亮渲染器
 *
 * 当玩家手持激光绑定工具时，高亮显示已绑定的连接器位置
 * - 主连接器（源）：红色边框
 * - 从连接器（目标）：绿色边框
 */
@OnlyIn(Dist.CLIENT)
public class LaserBindingToolRenderer {

    // 创建自定义RenderType，线宽为3.0
    private static final RenderType BLOCK_HIGHLIGHT_LINE = RenderType.create("expandedae_block_highlight_line",
            DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 65536, false, false,
            RenderType.CompositeState.builder()
                    .setLineState(new RenderType.LineStateShard(OptionalDouble.of(3.0)))
                    .setTransparencyState(RenderType.TransparencyStateShard.GLINT_TRANSPARENCY)
                    .setTextureState(RenderType.NO_TEXTURE)
                    .setDepthTestState(RenderType.NO_DEPTH_TEST)
                    .setCullState(RenderType.NO_CULL)
                    .setLightmapState(RenderType.NO_LIGHTMAP)
                    .setWriteMaskState(RenderType.COLOR_DEPTH_WRITE)
                    .setShaderState(RenderType.RENDERTYPE_LINES_SHADER)
                    .createCompositeState(false)
    );

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        // 检查玩家是否手持激光绑定工具
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        ItemStack toolStack = null;
        if (mainHand.getItem() instanceof LaserBindingTool) {
            toolStack = mainHand;
        } else if (offHand.getItem() instanceof LaserBindingTool) {
            toolStack = offHand;
        }

        if (toolStack == null) {
            return;
        }

        // 获取工具NBT数据
        CompoundTag tag = toolStack.getOrDefault(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY
        ).copyTag();

        if (!tag.contains("SelectedPos")) {
            return;
        }

        BlockPos selectedPos = readBlockPos(tag.getCompound("SelectedPos"));
        if (selectedPos == null) {
            return;
        }

        Level level = player.level();
        Camera camera = event.getCamera();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();

        // 开始渲染
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();

        // 渲染选中的连接器 - 黄色
        drawBlockOutline(poseStack, bufferSource, camera, new AABB(selectedPos), 1f, 1f, 0f, 1f);

        // 获取选中的方块实体，如果已连接则渲染连接目标
        BlockEntity be = level.getBlockEntity(selectedPos);
        if (be instanceof OmniLaserBeamBlockEntity omniBe) {
            // 使用 getClientLinkedTarget 获取客户端同步的连接目标
            BlockPos targetPos = omniBe.getClientLinkedTarget();
            if (targetPos != null) {
                // 渲染连接目标 - 绿色
                drawBlockOutline(poseStack, bufferSource, camera, new AABB(targetPos), 0f, 1f, 0f, 1f);
            }
        }

        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
    }

    /**
     * 绘制方块边框
     */
    private static void drawBlockOutline(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                         Camera camera, AABB box, float r, float g, float b, float a) {
        if (!camera.isInitialized()) {
            return;
        }

        Vec3 camPos = camera.getPosition();
        // 将AABB移动到相机相对坐标
        AABB aabb = box.move(-camPos.x, -camPos.y, -camPos.z);

        // 计算8个顶点
        Vec3 topRight = new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ);
        Vec3 bottomRight = new Vec3(aabb.maxX, aabb.minY, aabb.maxZ);
        Vec3 bottomLeft = new Vec3(aabb.minX, aabb.minY, aabb.maxZ);
        Vec3 topLeft = new Vec3(aabb.minX, aabb.maxY, aabb.maxZ);
        Vec3 topRight2 = new Vec3(aabb.maxX, aabb.maxY, aabb.minZ);
        Vec3 bottomRight2 = new Vec3(aabb.maxX, aabb.minY, aabb.minZ);
        Vec3 bottomLeft2 = new Vec3(aabb.minX, aabb.minY, aabb.minZ);
        Vec3 topLeft2 = new Vec3(aabb.minX, aabb.maxY, aabb.minZ);

        VertexConsumer buf = bufferSource.getBuffer(BLOCK_HIGHLIGHT_LINE);

        // 渲染两个面和连接边
        renderBox(buf, poseStack, topLeft, bottomLeft, topRight, bottomRight, r, g, b, a);
        renderBox(buf, poseStack, topLeft2, bottomLeft2, topRight2, bottomRight2, r, g, b, a);
        renderLine(buf, poseStack, topRight, topRight2, r, g, b, a);
        renderLine(buf, poseStack, bottomRight, bottomRight2, r, g, b, a);
        renderLine(buf, poseStack, bottomLeft, bottomLeft2, r, g, b, a);
        renderLine(buf, poseStack, topLeft, topLeft2, r, g, b, a);
    }

    /**
     * 渲染一个面（4条边）
     */
    private static void renderBox(VertexConsumer buf, PoseStack poseStack,
                                  Vec3 topLeft, Vec3 bottomLeft, Vec3 topRight, Vec3 bottomRight,
                                  float r, float g, float b, float a) {
        renderLine(buf, poseStack, topLeft, bottomLeft, r, g, b, a);
        renderLine(buf, poseStack, topLeft, topRight, r, g, b, a);
        renderLine(buf, poseStack, bottomRight, bottomLeft, r, g, b, a);
        renderLine(buf, poseStack, bottomRight, topRight, r, g, b, a);
    }

    /**
     * 渲染单条线
     */
    private static void renderLine(VertexConsumer buf, PoseStack poseStack,
                                   Vec3 from, Vec3 to, float r, float g, float b, float a) {
        var mat = poseStack.last().pose();
        var normal = from.subtract(to);
        buf.addVertex(mat, (float) from.x, (float) from.y, (float) from.z)
                .setColor(r, g, b, a)
                .setNormal((float) normal.x, (float) normal.y, (float) normal.z);
        buf.addVertex(mat, (float) to.x, (float) to.y, (float) to.z)
                .setColor(r, g, b, a)
                .setNormal((float) normal.x, (float) normal.y, (float) normal.z);
    }

    /**
     * 从CompoundTag读取BlockPos
     */
    private static BlockPos readBlockPos(CompoundTag tag) {
        if (!tag.contains("x") || !tag.contains("y") || !tag.contains("z")) {
            return null;
        }
        return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }
}
