package uwu.lopyluna.excavein.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import uwu.lopyluna.excavein.Excavein;
import uwu.lopyluna.excavein.config.ClientConfig;

import java.util.HashSet;
import java.util.Set;

import static uwu.lopyluna.excavein.config.ClientConfig.MAX_BLOCK_VIEW;

@SuppressWarnings({"removal", "unused"})
@Mod.EventBusSubscriber(modid = Excavein.MOD_ID, value = Dist.CLIENT)
public class BlockOutlineRenderer {

    private static final Minecraft mc = Minecraft.getInstance();
    public static Set<BlockPos> outlineBlocks = new HashSet<>();
    private static boolean shouldRenderOutline = false;

    public static void setOutlineBlocks(Set<BlockPos> blocks) {
        outlineBlocks = blocks;
    }

    @SubscribeEvent
    public static void onRenderWorld(net.minecraftforge.client.event.RenderLevelLastEvent event) {
        PoseStack poseStack = event.getPoseStack();
        if (mc.getConnection() == null || !shouldRenderOutline || outlineBlocks.isEmpty() || outlineBlocks.size() > MAX_BLOCK_VIEW.get() || ClientCooldownHandler.isCooldownActive()) {
            return;
        }


        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.LINES);

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        double camX = cameraPos.x;
        double camY = cameraPos.y;
        double camZ = cameraPos.z;

        VoxelShape selectionShape = convertSelectionToVoxelShape(outlineBlocks);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(ClientConfig.OUTLINE_THICKNESS.get().floatValue() * 1.5f);

        float red = ClientConfig.OUTLINE_COLOR_R.get() / 255f;
        float green = ClientConfig.OUTLINE_COLOR_G.get() / 255f;
        float blue = ClientConfig.OUTLINE_COLOR_B.get() / 255f;
        float alpha = ClientConfig.OUTLINE_ALPHA.get() / 100f;

        renderShape(poseStack, vertexConsumer, selectionShape, -camX, -camY, -camZ, red, green, blue, alpha);

        bufferSource.endBatch(RenderType.LINES);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (mc.getConnection() != null || event.phase == TickEvent.Phase.END) {
            shouldRenderOutline = KeybindHandler.SELECTION_ACTIVATION.isDown();
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

    private static void renderShape(PoseStack pPoseStack, VertexConsumer pConsumer, VoxelShape pShape, double pX, double pY, double pZ, float pRed, float pGreen, float pBlue, float pAlpha) {
        PoseStack.Pose posestack$pose = pPoseStack.last();
        pShape.forAllEdges((p_234280_, p_234281_, p_234282_, p_234283_, p_234284_, p_234285_) -> {
            float f = (float)(p_234283_ - p_234280_);
            float f1 = (float)(p_234284_ - p_234281_);
            float f2 = (float)(p_234285_ - p_234282_);
            float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
            f /= f3;
            f1 /= f3;
            f2 /= f3;
            pConsumer.vertex(posestack$pose.pose(), (float)(p_234280_ + pX), (float)(p_234281_ + pY), (float)(p_234282_ + pZ)).color(pRed, pGreen, pBlue, pAlpha).normal(posestack$pose.normal(), f, f1, f2).endVertex();
            pConsumer.vertex(posestack$pose.pose(), (float)(p_234283_ + pX), (float)(p_234284_ + pY), (float)(p_234285_ + pZ)).color(pRed, pGreen, pBlue, pAlpha).normal(posestack$pose.normal(), f, f1, f2).endVertex();
        });
    }
}
