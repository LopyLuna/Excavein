package uwu.lopyluna.excavein.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import uwu.lopyluna.excavein.Excavein;
import uwu.lopyluna.excavein.Utils;
import uwu.lopyluna.excavein.config.ClientConfig;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static uwu.lopyluna.excavein.client.KeybindHandler.SELECTION_ACTIVATION;
import static uwu.lopyluna.excavein.client.KeybindHandler.keyActivated;
import static uwu.lopyluna.excavein.config.ClientConfig.*;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = Excavein.MOD_ID, value = Dist.CLIENT)
public class BlockOutlineRenderer {

    private static final Minecraft mc = Minecraft.getInstance();
    public static Set<BlockPos> outlineBlocks = new HashSet<>();
    private static boolean shouldRenderOutline = false;

    public static void setOutlineBlocks(Set<BlockPos> blocks) {
        outlineBlocks = blocks;
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderHighlightEvent.Block event) {
        PoseStack poseStack = event.getPoseStack();
        if (mc.getConnection() == null || !shouldRenderOutline || outlineBlocks.isEmpty() || outlineBlocks.size() > MAX_BLOCK_VIEW.get() || ClientCooldownHandler.isCooldownActive()) {
            return;
        }
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.defaultBlendFunc();
        VoxelShape selectionShape = convertSelectionToVoxelShape(outlineBlocks);

        float red = ClientConfig.SELECTION_COLOR_R.get() / 255f;
        float green = ClientConfig.SELECTION_COLOR_G.get() / 255f;
        float blue = ClientConfig.SELECTION_COLOR_B.get() / 255f;
        float alpha = ClientConfig.SELECTION_ALPHA.get() / 255f;
        RenderType blank = RenderTypes.getOutline(Utils.asResource("textures/special/blank.png"), false);
        RenderType selection = RenderTypes.getOutline(Utils.asResource("textures/special/selection.png"), BLUR_FACE.get());

        if (RENDER_OUTLINE.get())
            renderShape(poseStack, event.getMultiBufferSource().getBuffer(blank), selectionShape, event.getCamera().getPosition(), red, green, blue);
        if (RENDER_FACE.get())
            renderFaces(poseStack, event.getMultiBufferSource().getBuffer(selection), outlineBlocks, event.getCamera().getPosition(), new Vector4f(red, green, blue, alpha));

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (mc.getConnection() != null || event.phase == TickEvent.Phase.END) {
            shouldRenderOutline = ((!TOGGLEABLE_KEY.get() && SELECTION_ACTIVATION != null && SELECTION_ACTIVATION.isDown()) || (TOGGLEABLE_KEY.get() && keyActivated));
        }
    }

    private static VoxelShape convertSelectionToVoxelShape(Set<BlockPos> selectedBlocks) {
        VoxelShape combinedShape = Shapes.empty();

        for (BlockPos pos : selectedBlocks) {
            VoxelShape blockShape = Shapes.block();

            blockShape = blockShape.move(pos.getX(), pos.getY(), pos.getZ());
            combinedShape = Shapes.or(combinedShape, blockShape);
        }

        return combinedShape;
    }

    private static void renderShape(PoseStack pPoseStack, VertexConsumer pConsumer, VoxelShape pShape, Vec3 camPos, float pRed, float pGreen, float pBlue) {
        pShape.forAllEdges((x1, y1, z1, x2, y2, z2) -> bufferCuboidLine(pPoseStack, pConsumer, camPos, new Vec3(x1, y1, z1), new Vec3(x2, y2, z2),
                ClientConfig.OUTLINE_THICKNESS.get().floatValue() / 16.0f, new Vector4f(pRed, pGreen, pBlue, 1), LightTexture.FULL_BRIGHT, true));
    }

    //MOSTLY FROM CREATE'S OUTLINE.JAVA

    protected static final Vector3f diffPosTemp = new Vector3f();
    protected static final Vector3f minPosTemp = new Vector3f();
    protected static final Vector3f maxPosTemp = new Vector3f();
    protected static final Vector4f pPosTransformTemp = new Vector4f();
    protected static final Vector3f pNormalTransformTemp = new Vector3f();

    public static void bufferCuboidLine(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, Vec3 start, Vec3 end,
                                 float width, Vector4f color, int lightmap, boolean disableNormals) {
        Vector3f diff = diffPosTemp;
        diff.set((float) (end.x - start.x), (float) (end.y - start.y), (float) (end.z - start.z));

        float length = Mth.sqrt(diff.x() * diff.x() + diff.y() * diff.y() + diff.z() * diff.z());
        double h = Mth.atan2(diff.x(), diff.z());
        float hAngle = h == 0 ? 0 : (float) (h * 180 / Math.PI);
        float hDistance = Mth.sqrt(diff.x() * diff.x() + diff.z() * diff.z());
        double v = Mth.atan2(hDistance, diff.y());
        float vAngle = v == 0 ? 0 : (float) (v * 180 / Math.PI) - 90;

        poseStack.pushPose();
        poseStack.translate(start.x - camera.x, start.y - camera.y, start.z - camera.z);
        if (hAngle != 0)
            poseStack.mulPose(Vector3f.YP.rotationDegrees(hAngle));
        if (vAngle != 0)
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(vAngle));

        bufferCuboidLine(poseStack.last(), consumer, Vector3f.ZERO, getAxisByVec3(start, end).isVertical() ? Direction.UP : Direction.SOUTH, length, width, color, lightmap,
                disableNormals);
        poseStack.popPose();
    }

    public static Direction.Axis getAxisByVec3(Vec3 vec1, Vec3 vec2) {
        Vec3 difference = vec2.subtract(vec1);

        double absX = Math.abs(difference.x);
        double absY = Math.abs(difference.y);
        double absZ = Math.abs(difference.z);

        if (absX > absY && absX > absZ) {
            return Direction.Axis.X;
        } else if (absY > absX && absY > absZ) {
            return Direction.Axis.Y;
        } else {
            return Direction.Axis.Z;
        }
    }

    public static void bufferCuboidLine(PoseStack.Pose pose, VertexConsumer consumer, Vector3f origin, Direction direction,
                                 float length, float width, Vector4f color, int lightmap, boolean disableNormals) {
        Vector3f minPos = minPosTemp;
        Vector3f maxPos = maxPosTemp;

        float halfWidth = width / 2;
        minPos.set(origin.x() - halfWidth, origin.y() - halfWidth, origin.z() - halfWidth);
        maxPos.set(origin.x() + halfWidth, origin.y() + halfWidth, origin.z() + halfWidth);

        switch (direction) {
            case DOWN -> minPos.add(0, -length, 0);
            case UP -> maxPos.add(0, length, 0);
            case NORTH -> minPos.add(0, 0, -length);
            case SOUTH -> maxPos.add(0, 0, length);
            case WEST -> minPos.add(-length, 0, 0);
            case EAST -> maxPos.add(length, 0, 0);
        }

        bufferCuboid(pose, consumer, minPos, maxPos, color, lightmap, disableNormals);
    }

    public static void bufferCuboid(PoseStack.Pose pose, VertexConsumer consumer, Vector3f minPos, Vector3f maxPos,
                             Vector4f color, int lightmap, boolean disableNormals) {
        Vector4f posTransformTemp = pPosTransformTemp;
        Vector3f normalTransformTemp = pNormalTransformTemp;

        float minX = minPos.x();
        float minY = minPos.y();
        float minZ = minPos.z();
        float maxX = maxPos.x();
        float maxY = maxPos.y();
        float maxZ = maxPos.z();

        Matrix4f posMatrix = pose.pose();

        posTransformTemp.set(minX, minY, maxZ, 1);
        posTransformTemp.transform(posMatrix);
        double x0 = posTransformTemp.x();
        double y0 = posTransformTemp.y();
        double z0 = posTransformTemp.z();

        posTransformTemp.set(minX, minY, minZ, 1);
        posTransformTemp.transform(posMatrix);
        double x1 = posTransformTemp.x();
        double y1 = posTransformTemp.y();
        double z1 = posTransformTemp.z();

        posTransformTemp.set(maxX, minY, minZ, 1);
        posTransformTemp.transform(posMatrix);
        double x2 = posTransformTemp.x();
        double y2 = posTransformTemp.y();
        double z2 = posTransformTemp.z();

        posTransformTemp.set(maxX, minY, maxZ, 1);
        posTransformTemp.transform(posMatrix);
        double x3 = posTransformTemp.x();
        double y3 = posTransformTemp.y();
        double z3 = posTransformTemp.z();

        posTransformTemp.set(minX, maxY, minZ, 1);
        posTransformTemp.transform(posMatrix);
        double x4 = posTransformTemp.x();
        double y4 = posTransformTemp.y();
        double z4 = posTransformTemp.z();

        posTransformTemp.set(minX, maxY, maxZ, 1);
        posTransformTemp.transform(posMatrix);
        double x5 = posTransformTemp.x();
        double y5 = posTransformTemp.y();
        double z5 = posTransformTemp.z();

        posTransformTemp.set(maxX, maxY, maxZ, 1);
        posTransformTemp.transform(posMatrix);
        double x6 = posTransformTemp.x();
        double y6 = posTransformTemp.y();
        double z6 = posTransformTemp.z();

        posTransformTemp.set(maxX, maxY, minZ, 1);
        posTransformTemp.transform(posMatrix);
        double x7 = posTransformTemp.x();
        double y7 = posTransformTemp.y();
        double z7 = posTransformTemp.z();

        float r = color.x();
        float g = color.y();
        float b = color.z();
        float a = color.w();

        Matrix3f normalMatrix = pose.normal();

        // down

        if (disableNormals) {
            normalTransformTemp.set(0, 1, 0);
        } else {
            normalTransformTemp.set(0, -1, 0);
        }
        normalTransformTemp.transform(normalMatrix);
        float nx0 = normalTransformTemp.x();
        float ny0 = normalTransformTemp.y();
        float nz0 = normalTransformTemp.z();

        consumer.vertex(x0, y0, z0)
                .color(r, g, b, a)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx0, ny0, nz0)
                .endVertex();

        consumer.vertex(x1, y1, z1)
                .color(r, g, b, a)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx0, ny0, nz0)
                .endVertex();

        consumer.vertex(x2, y2, z2)
                .color(r, g, b, a)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx0, ny0, nz0)
                .endVertex();

        consumer.vertex(x3, y3, z3)
                .color(r, g, b, a)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx0, ny0, nz0)
                .endVertex();

        // up

        normalTransformTemp.set(0, 1, 0);
        normalTransformTemp.transform(normalMatrix);
        float nx1 = normalTransformTemp.x();
        float ny1 = normalTransformTemp.y();
        float nz1 = normalTransformTemp.z();

        consumer.vertex(x4, y4, z4)
                .color(r, g, b, a)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx1, ny1, nz1)
                .endVertex();

        consumer.vertex(x5, y5, z5)
                .color(r, g, b, a)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx1, ny1, nz1)
                .endVertex();

        consumer.vertex(x6, y6, z6)
                .color(r, g, b, a)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx1, ny1, nz1)
                .endVertex();

        consumer.vertex(x7, y7, z7)
                .color(r, g, b, a)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx1, ny1, nz1)
                .endVertex();

        // north

        if (disableNormals) {
            normalTransformTemp.set(0, 1, 0);
        } else {
            normalTransformTemp.set(0, 0, -1);
        }
        normalTransformTemp.transform(normalMatrix);
        float nx2 = normalTransformTemp.x();
        float ny2 = normalTransformTemp.y();
        float nz2 = normalTransformTemp.z();

        consumer.vertex(x7, y7, z7)
                .color(r, g, b, a)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx2, ny2, nz2)
                .endVertex();

        consumer.vertex(x2, y2, z2)
                .color(r, g, b, a)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx2, ny2, nz2)
                .endVertex();

        consumer.vertex(x1, y1, z1)
                .color(r, g, b, a)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx2, ny2, nz2)
                .endVertex();

        consumer.vertex(x4, y4, z4)
                .color(r, g, b, a)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx2, ny2, nz2)
                .endVertex();

        // south

        if (disableNormals) {
            normalTransformTemp.set(0, 1, 0);
        } else {
            normalTransformTemp.set(0, 0, 1);
        }
        normalTransformTemp.transform(normalMatrix);
        float nx3 = normalTransformTemp.x();
        float ny3 = normalTransformTemp.y();
        float nz3 = normalTransformTemp.z();

        consumer.vertex(x5, y5, z5)
                .color(r, g, b, a)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx3, ny3, nz3)
                .endVertex();

        consumer.vertex(x0, y0, z0)
                .color(r, g, b, a)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx3, ny3, nz3)
                .endVertex();

        consumer.vertex(x3, y3, z3)
                .color(r, g, b, a)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx3, ny3, nz3)
                .endVertex();

        consumer.vertex(x6, y6, z6)
                .color(r, g, b, a)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx3, ny3, nz3)
                .endVertex();

        // west

        if (disableNormals) {
            normalTransformTemp.set(0, 1, 0);
        } else {
            normalTransformTemp.set(-1, 0, 0);
        }
        normalTransformTemp.transform(normalMatrix);
        float nx4 = normalTransformTemp.x();
        float ny4 = normalTransformTemp.y();
        float nz4 = normalTransformTemp.z();

        consumer.vertex(x4, y4, z4)
                .color(r, g, b, a)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx4, ny4, nz4)
                .endVertex();

        consumer.vertex(x1, y1, z1)
                .color(r, g, b, a)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx4, ny4, nz4)
                .endVertex();

        consumer.vertex(x0, y0, z0)
                .color(r, g, b, a)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx4, ny4, nz4)
                .endVertex();

        consumer.vertex(x5, y5, z5)
                .color(r, g, b, a)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx4, ny4, nz4)
                .endVertex();

        // east

        if (disableNormals) {
            normalTransformTemp.set(0, 1, 0);
        } else {
            normalTransformTemp.set(1, 0, 0);
        }
        normalTransformTemp.transform(normalMatrix);
        float nx5 = normalTransformTemp.x();
        float ny5 = normalTransformTemp.y();
        float nz5 = normalTransformTemp.z();

        consumer.vertex(x6, y6, z6)
                .color(r, g, b, a)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx5, ny5, nz5)
                .endVertex();

        consumer.vertex(x3, y3, z3)
                .color(r, g, b, a)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx5, ny5, nz5)
                .endVertex();

        consumer.vertex(x2, y2, z2)
                .color(r, g, b, a)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx5, ny5, nz5)
                .endVertex();

        consumer.vertex(x7, y7, z7)
                .color(r, g, b, a)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx5, ny5, nz5)
                .endVertex();
    }

    public static void bufferQuad(PoseStack.Pose pose, VertexConsumer consumer, Vector3f pos0, Vector3f pos1, Vector3f pos2,
                           Vector3f pos3, Vector4f color, int lightmap, Vector3f normal) {
        bufferQuad(pose, consumer, pos0, pos1, pos2, pos3, color, 0, 0, 1, 1, lightmap, normal);
    }

    public static void bufferQuad(PoseStack.Pose pose, VertexConsumer consumer, Vector3f pos0, Vector3f pos1, Vector3f pos2,
                           Vector3f pos3, Vector4f color, float minU, float minV, float maxU, float maxV, int lightmap, Vector3f normal) {
        Vector4f posTransformTemp = pPosTransformTemp;
        Vector3f normalTransformTemp = pNormalTransformTemp;

        Matrix4f posMatrix = pose.pose();

        posTransformTemp.set(pos0.x(), pos0.y(), pos0.z(), 1);
        posTransformTemp.transform(posMatrix);
        double x0 = posTransformTemp.x();
        double y0 = posTransformTemp.y();
        double z0 = posTransformTemp.z();

        posTransformTemp.set(pos1.x(), pos1.y(), pos1.z(), 1);
        posTransformTemp.transform(posMatrix);
        double x1 = posTransformTemp.x();
        double y1 = posTransformTemp.y();
        double z1 = posTransformTemp.z();

        posTransformTemp.set(pos2.x(), pos2.y(), pos2.z(), 1);
        posTransformTemp.transform(posMatrix);
        double x2 = posTransformTemp.x();
        double y2 = posTransformTemp.y();
        double z2 = posTransformTemp.z();

        posTransformTemp.set(pos3.x(), pos3.y(), pos3.z(), 1);
        posTransformTemp.transform(posMatrix);
        double x3 = posTransformTemp.x();
        double y3 = posTransformTemp.y();
        double z3 = posTransformTemp.z();

        float r = color.x();
        float g = color.y();
        float b = color.z();
        float a = color.w();

        normalTransformTemp.load(normal);
        normalTransformTemp.transform(pose.normal());
        float nx = normalTransformTemp.x();
        float ny = normalTransformTemp.y();
        float nz = normalTransformTemp.z();

        consumer.vertex(x0, y0, z0)
                .color(r, g, b, a)
                .uv(minU, minV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx, ny, nz)
                .endVertex();

        consumer.vertex(x1, y1, z1)
                .color(r, g, b, a)
                .uv(minU, maxV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx, ny, nz)
                .endVertex();

        consumer.vertex(x2, y2, z2)
                .color(r, g, b, a)
                .uv(maxU, maxV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx, ny, nz)
                .endVertex();

        consumer.vertex(x3, y3, z3)
                .color(r, g, b, a)
                .uv(maxU, minV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightmap)
                .normal(nx, ny, nz)
                .endVertex();
    }
    
    //MOSTLY FROM CREATE'S BLOCKCLUSTEROUTLINE.JAVA

    private static final Cluster cluster = new Cluster();
    private static Set<BlockPos> pos;

    protected static final Vector3f pos0Temp = new Vector3f();
    protected static final Vector3f pos1Temp = new Vector3f();
    protected static final Vector3f pos2Temp = new Vector3f();
    protected static final Vector3f pos3Temp = new Vector3f();
    protected static final Vector3f normalTemp = new Vector3f();
    protected static final Vector3f originTemp = new Vector3f();

    protected static void renderFaces(PoseStack ms, VertexConsumer consumer, Set<BlockPos> blocks, Vec3 camera, Vector4f color) {
        blocks.forEach(cluster::include);

        ms.pushPose();
        ms.translate(cluster.anchor.getX() - camera.x, cluster.anchor.getY() - camera.y,
                cluster.anchor.getZ() - camera.z);

        PoseStack.Pose pose = ms.last();

        cluster.visibleFaces.forEach((face, axisDirection) -> {
            Direction direction = Direction.get(axisDirection, face.axis);
            BlockPos pPos = face.pos;
            if (axisDirection == Direction.AxisDirection.POSITIVE)
                pPos = pPos.relative(direction.getOpposite());
            bufferBlockFace(pose, consumer, pPos, direction, color);
        });
        if (!cluster.visibleFaces.isEmpty())
            cluster.visibleFaces.clear();
        ms.popPose();
    }
    
    public static void loadFaceData(Direction face, Vector3f pos0, Vector3f pos1, Vector3f pos2, Vector3f pos3, Vector3f normal) {
        switch (face) {
            case DOWN -> {
                // 0 1 2 3
                pos0.set(0, 0, 1);
                pos1.set(0, 0, 0);
                pos2.set(1, 0, 0);
                pos3.set(1, 0, 1);
                normal.set(0, -1, 0);
            }
            case UP -> {
                // 4 5 6 7
                pos0.set(0, 1, 0);
                pos1.set(0, 1, 1);
                pos2.set(1, 1, 1);
                pos3.set(1, 1, 0);
                normal.set(0, 1, 0);
            }
            case NORTH -> {
                // 7 2 1 4
                pos0.set(1, 1, 0);
                pos1.set(1, 0, 0);
                pos2.set(0, 0, 0);
                pos3.set(0, 1, 0);
                normal.set(0, 0, -1);
            }
            case SOUTH -> {
                // 5 0 3 6
                pos0.set(0, 1, 1);
                pos1.set(0, 0, 1);
                pos2.set(1, 0, 1);
                pos3.set(1, 1, 1);
                normal.set(0, 0, 1);
            }
            case WEST -> {
                // 4 1 0 5
                pos0.set(0, 1, 0);
                pos1.set(0, 0, 0);
                pos2.set(0, 0, 1);
                pos3.set(0, 1, 1);
                normal.set(-1, 0, 0);
            }
            case EAST -> {
                // 6 3 2 7
                pos0.set(1, 1, 1);
                pos1.set(1, 0, 1);
                pos2.set(1, 0, 0);
                pos3.set(1, 1, 0);
                normal.set(1, 0, 0);
            }
        }
    }

    public static void addPos(float x, float y, float z, Vector3f pos0, Vector3f pos1, Vector3f pos2, Vector3f pos3) {
        pos0.add(x, y, z);
        pos1.add(x, y, z);
        pos2.add(x, y, z);
        pos3.add(x, y, z);
    }

    protected static void bufferBlockFace(PoseStack.Pose pose, VertexConsumer consumer, BlockPos pos, Direction face, Vector4f color) {
        Vector3f pos0 = pos0Temp;
        Vector3f pos1 = pos1Temp;
        Vector3f pos2 = pos2Temp;
        Vector3f pos3 = pos3Temp;
        Vector3f normal = normalTemp;

        loadFaceData(face, pos0, pos1, pos2, pos3, normal);
        addPos(pos.getX() + face.getStepX() / 128f,
                pos.getY() + face.getStepY() / 128f,
                pos.getZ() + face.getStepZ() / 128f,
                pos0, pos1, pos2, pos3);

        bufferQuad(pose, consumer, pos0, pos1, pos2, pos3, color, LightTexture.FULL_BRIGHT, normal);
    }
    private static class Cluster {

        private BlockPos anchor;
        private final Map<MergeEntry, Direction.AxisDirection> visibleFaces;

        public Cluster() {
            visibleFaces = new HashMap<>();
        }

        public boolean isEmpty() {
            return anchor == null;
        }

        public void include(BlockPos pos) {
            if (anchor == null)
                anchor = pos;

            pos = pos.subtract(anchor);

            // 6 FACES
            for (Direction.Axis axis : Direction.Axis.values()) {
                Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, axis);
                for (int offset :  new int[]{0, 1}) {
                    MergeEntry entry = new MergeEntry(axis, pos.relative(direction, offset));
                    if (visibleFaces.remove(entry) == null)
                        visibleFaces.put(entry, offset == 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE);
                }
            }
        }
    }

    private record MergeEntry(Direction.Axis axis, BlockPos pos) {
        @Override
            public boolean equals(Object o) {
                if (this == o)
                    return true;
                if (!(o instanceof MergeEntry other))
                    return false;

                return this.axis == other.axis && this.pos.equals(other.pos);
            }

            @Override
            public int hashCode() {
                return this.pos.hashCode() * 31 + axis.ordinal();
            }
        }
}
