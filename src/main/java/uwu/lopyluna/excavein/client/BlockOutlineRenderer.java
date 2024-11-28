package uwu.lopyluna.excavein.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import uwu.lopyluna.excavein.Excavein;
import uwu.lopyluna.excavein.config.ClientConfig;
import uwu.lopyluna.excavein.utils.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static uwu.lopyluna.excavein.client.ClientHandler.keyPressed;
import static uwu.lopyluna.excavein.config.ClientConfig.*;
import static uwu.lopyluna.excavein.config.ServerConfig.*;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = Excavein.MOD_ID, value = Dist.CLIENT)
public class BlockOutlineRenderer {

    protected static final Vector3f diffPosTemp = new Vector3f();
    protected static final Vector3f minPosTemp = new Vector3f();
    protected static final Vector3f maxPosTemp = new Vector3f();
    protected static final Vector4f pPosTransformTemp = new Vector4f();
    protected static final Vector3f pNormalTransformTemp = new Vector3f();
    protected static final Vector3f pos0Temp = new Vector3f();

    //MOSTLY FROM CREATE'S OUTLINE.JAVA
    protected static final Vector3f pos1Temp = new Vector3f();
    protected static final Vector3f pos2Temp = new Vector3f();
    protected static final Vector3f pos3Temp = new Vector3f();
    protected static final Vector3f normalTemp = new Vector3f();
    protected static final Vector3f originTemp = new Vector3f();
    private static final Minecraft mc = Minecraft.getInstance();
    private static final Cluster cluster = new Cluster();
    public static int amountBreak = 0;
    public static int amountInteract = 0;
    public static Set<BlockPos> outlineBlocks = new HashSet<>();
    public static Set<BlockPos> outlineBlocksPlacing = new HashSet<>();
    public static Set<BlockPos> outlineBlocksMixed = new HashSet<>();
    private static Set<BlockPos> pos;

    public static void updateBlocks(Set<BlockPos> breaking, Set<BlockPos> interaction) {
        boolean flag = BLOCK_PLACING.get() || HAND_INTERACTION.get() || ITEM_INTERACTION.get();
        Set<BlockPos> breaks = fixBlocks(breaking);
        Set<BlockPos> interactions = flag ? fixBlocks(interaction) : new HashSet<>();
        Set<BlockPos> mixes = new HashSet<>();
        breaks.forEach(position -> {if (position != null && interactions.contains(position)) mixes.add(position);});
        breaks.removeAll(mixes);
        interactions.removeAll(mixes);
        outlineBlocksMixed = fixBlocks(mixes);
        outlineBlocksPlacing = interactions;
        outlineBlocks = breaks;
        amountBreak = breaking.size();
        amountInteract = flag ? interaction.size() : 0;
    }

    public static Set<BlockPos> fixBlocks(Set<BlockPos> blocks) {
        Set<BlockPos> renderedBlocks = new HashSet<>();
        if (blocks != null && (RENDER_OUTLINE.get() || RENDER_FACE.get() && !blocks.isEmpty()))
            blocks.forEach(pos -> { if (mc.levelRenderer.isChunkCompiled(pos)) renderedBlocks.add(pos); });
        return renderedBlocks;
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderHighlightEvent.Block event) {
        PoseStack poseStack = event.getPoseStack();
        if (mc.getConnection() == null || mc.player == null || (!RENDER_OUTLINE.get() && !RENDER_FACE.get()))
            return;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.defaultBlendFunc();

        boolean additive = ADDITIVE_SHADER_SELECTION.get();

        float divide = additive ? 512f : 255f;

        float alphaM = ClientConfig.MIXED_SELECTION_ALPHA.get() / divide;
        float redM = Mth.clamp(((ClientConfig.MIXED_SELECTION_COLOR_R.get() / divide) - (additive ? alphaM : 0)), 0, 1);
        float greenM = Mth.clamp(((ClientConfig.MIXED_SELECTION_COLOR_G.get() / divide) - (additive ? alphaM : 0)), 0, 1);
        float blueM = Mth.clamp(((ClientConfig.MIXED_SELECTION_COLOR_B.get() / divide) - (additive ? alphaM : 0)), 0, 1);

        float alphaD = ClientConfig.DESTROY_SELECTION_ALPHA.get() / divide;
        float redD = Mth.clamp(((ClientConfig.DESTROY_SELECTION_COLOR_R.get() / divide) - (additive ? alphaD : 0)), 0, 1);
        float greenD = Mth.clamp(((ClientConfig.DESTROY_SELECTION_COLOR_G.get() / divide) - (additive ? alphaD : 0)), 0, 1);
        float blueD = Mth.clamp(((ClientConfig.DESTROY_SELECTION_COLOR_B.get() / divide) - (additive ? alphaD : 0)), 0, 1);

        float alphaI = ClientConfig.INTERACTION_SELECTION_ALPHA.get() / divide;
        float redI = Mth.clamp(((ClientConfig.INTERACTION_SELECTION_COLOR_R.get() / divide) - (additive ? alphaI : 0)), 0, 1);
        float greenI = Mth.clamp(((ClientConfig.INTERACTION_SELECTION_COLOR_G.get() / divide) - (additive ? alphaI : 0)), 0, 1);
        float blueI = Mth.clamp(((ClientConfig.INTERACTION_SELECTION_COLOR_B.get() / divide) - (additive ? alphaI : 0)), 0, 1);

        float alphaW = ClientConfig.WARN_SELECTION_ALPHA.get() / divide;
        float redW = Mth.clamp(((ClientConfig.WARN_SELECTION_COLOR_R.get() / divide) - (additive ? alphaW : 0)), 0, 1);
        float greenW = Mth.clamp(((ClientConfig.WARN_SELECTION_COLOR_G.get() / divide) - (additive ? alphaW : 0)), 0, 1);
        float blueW = Mth.clamp(((ClientConfig.WARN_SELECTION_COLOR_B.get() / divide) - (additive ? alphaW : 0)), 0, 1);

        boolean flagI = ClientHelper.flag;
        boolean flagB = (!ClientHelper.currentlyBreaking || DELAY_BETWEEN_BREAK.get() == 0 || !WAIT_TILL_BROKEN.get()) && ClientHelper.flag;

        RenderType warn = RenderTypes.getOutline(Utils.asResource("textures/special/warn.png"), WARN_BLUR_FACE.get(), additive);
        RenderType blank = RenderTypes.getOutline(Utils.asResource("textures/special/blank.png"), false, additive);
        RenderType selection = flagB ? RenderTypes.getOutline(Utils.asResource("textures/special/hazard.png"), DESTROY_BLUR_FACE.get(), additive) : warn;
        RenderType interaction = flagI ? RenderTypes.getOutline(Utils.asResource("textures/special/plated.png"), INTERACTION_BLUR_FACE.get(), additive) : warn;
        RenderType mixed = flagI && flagB ? RenderTypes.getOutline(Utils.asResource("textures/special/checker.png"), MIXED_BLUR_FACE.get(), additive) : warn;
        var multiBufferSource = event.getMultiBufferSource();
        var camPos = event.getCamera().getPosition();

        Vector4f wColor = new Vector4f(redW, greenW, blueW, alphaW);
        Vector4f mColor = flagI && flagB ? new Vector4f(redM, greenM, blueM, alphaM) : wColor;
        Vector4f dColor = flagB ? new Vector4f(redM, greenD, blueD, alphaD) : wColor;
        Vector4f iColor = flagI ? new Vector4f(redI, greenI, blueI, alphaI) : wColor;

        if (keyPressed) {
            if (outlineBlocks != null && !outlineBlocks.isEmpty() && outlineBlocks.size() <= MAX_BLOCK_VIEW.get()) {
                if (RENDER_OUTLINE.get()) renderShape(poseStack, multiBufferSource.getBuffer(blank), outlineBlocks, camPos, dColor, 0.9f);
                if (RENDER_FACE.get()) renderFaces(poseStack, multiBufferSource.getBuffer(selection), outlineBlocks, camPos, dColor);
            } if (outlineBlocksPlacing != null && !outlineBlocksPlacing.isEmpty() && outlineBlocksPlacing.size() <= MAX_BLOCK_VIEW.get()) {
                if (RENDER_OUTLINE.get()) renderShape(poseStack, multiBufferSource.getBuffer(blank), outlineBlocksPlacing, camPos, iColor, 0.95f);
                if (RENDER_FACE.get()) renderFaces(poseStack, multiBufferSource.getBuffer(interaction), outlineBlocksPlacing, camPos, iColor);
            } if (outlineBlocksMixed != null && !outlineBlocksMixed.isEmpty() && outlineBlocksMixed.size() <= MAX_BLOCK_VIEW.get()) {
                if (RENDER_OUTLINE.get()) renderShape(poseStack, multiBufferSource.getBuffer(blank), outlineBlocksMixed, camPos, mColor, 1f);
                if (RENDER_FACE.get()) renderFaces(poseStack, multiBufferSource.getBuffer(mixed), outlineBlocksMixed, camPos, mColor);
            }
        }

        event.setCanceled(true);
    }

    //MOSTLY FROM CREATE'S BLOCKCLUSTEROUTLINE.JAVA

    private static VoxelShape convertSelectionToVoxelShape(Set<BlockPos> selectedBlocks) {
        VoxelShape combinedShape = Shapes.empty();

        for (BlockPos pos : selectedBlocks) {
            VoxelShape blockShape = Shapes.block();
            blockShape = blockShape.move(pos.getX(), pos.getY(), pos.getZ());
            combinedShape = Shapes.or(combinedShape, blockShape);
        }

        return combinedShape;
    }

    private static void renderShape(PoseStack pPoseStack, VertexConsumer pConsumer, Set<BlockPos> pPositions, Vec3 camPos, Vector4f pColor, float pThicknessMultiplier) {
        convertSelectionToVoxelShape(pPositions).optimize().forAllEdges((x1, y1, z1, x2, y2, z2) -> bufferCuboidLine(pPoseStack, pConsumer, camPos, new Vec3(x1, y1, z1), new Vec3(x2, y2, z2),
                (ClientConfig.OUTLINE_THICKNESS.get().floatValue() / 16.0f) * pThicknessMultiplier, new Vector4f(pColor.x, pColor.y, pColor.z, 1), LightTexture.FULL_BRIGHT, true));
    }

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
            poseStack.mulPose(Axis.YP.rotationDegrees(hAngle));
        if (vAngle != 0)
            poseStack.mulPose(Axis.XP.rotationDegrees(vAngle));

        bufferCuboidLine(poseStack.last(), consumer, new Vector3f(), getAxisByVec3(start, end).isVertical() ? Direction.UP : Direction.SOUTH, length, width, color, lightmap,
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
        posTransformTemp.mul(posMatrix);
        float x0 = posTransformTemp.x();
        float y0 = posTransformTemp.y();
        float z0 = posTransformTemp.z();

        posTransformTemp.set(minX, minY, minZ, 1);
        posTransformTemp.mul(posMatrix);
        float x1 = posTransformTemp.x();
        float y1 = posTransformTemp.y();
        float z1 = posTransformTemp.z();

        posTransformTemp.set(maxX, minY, minZ, 1);
        posTransformTemp.mul(posMatrix);
        float x2 = posTransformTemp.x();
        float y2 = posTransformTemp.y();
        float z2 = posTransformTemp.z();

        posTransformTemp.set(maxX, minY, maxZ, 1);
        posTransformTemp.mul(posMatrix);
        float x3 = posTransformTemp.x();
        float y3 = posTransformTemp.y();
        float z3 = posTransformTemp.z();

        posTransformTemp.set(minX, maxY, minZ, 1);
        posTransformTemp.mul(posMatrix);
        float x4 = posTransformTemp.x();
        float y4 = posTransformTemp.y();
        float z4 = posTransformTemp.z();

        posTransformTemp.set(minX, maxY, maxZ, 1);
        posTransformTemp.mul(posMatrix);
        float x5 = posTransformTemp.x();
        float y5 = posTransformTemp.y();
        float z5 = posTransformTemp.z();

        posTransformTemp.set(maxX, maxY, maxZ, 1);
        posTransformTemp.mul(posMatrix);
        float x6 = posTransformTemp.x();
        float y6 = posTransformTemp.y();
        float z6 = posTransformTemp.z();

        posTransformTemp.set(maxX, maxY, minZ, 1);
        posTransformTemp.mul(posMatrix);
        float x7 = posTransformTemp.x();
        float y7 = posTransformTemp.y();
        float z7 = posTransformTemp.z();

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
        normalTransformTemp.mul(normalMatrix);
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
        normalTransformTemp.mul(normalMatrix);
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
        normalTransformTemp.mul(normalMatrix);
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
        normalTransformTemp.mul(normalMatrix);
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
        normalTransformTemp.mul(normalMatrix);
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
        normalTransformTemp.mul(normalMatrix);
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
        posTransformTemp.mul(posMatrix);
        float x0 = posTransformTemp.x();
        float y0 = posTransformTemp.y();
        float z0 = posTransformTemp.z();

        posTransformTemp.set(pos1.x(), pos1.y(), pos1.z(), 1);
        posTransformTemp.mul(posMatrix);
        float x1 = posTransformTemp.x();
        float y1 = posTransformTemp.y();
        float z1 = posTransformTemp.z();

        posTransformTemp.set(pos2.x(), pos2.y(), pos2.z(), 1);
        posTransformTemp.mul(posMatrix);
        float x2 = posTransformTemp.x();
        float y2 = posTransformTemp.y();
        float z2 = posTransformTemp.z();

        posTransformTemp.set(pos3.x(), pos3.y(), pos3.z(), 1);
        posTransformTemp.mul(posMatrix);
        float x3 = posTransformTemp.x();
        float y3 = posTransformTemp.y();
        float z3 = posTransformTemp.z();

        float r = color.x();
        float g = color.y();
        float b = color.z();
        float a = color.w();

        normalTransformTemp.set(normal);
        normalTransformTemp.mul(pose.normal());
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

        private final Map<MergeEntry, Direction.AxisDirection> visibleFaces;
        private BlockPos anchor;

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
                for (int offset : new int[]{0, 1}) {
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
