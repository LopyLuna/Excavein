package uwu.lopyluna.excavein.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static uwu.lopyluna.excavein.ExcaveinCosmetic.LOC;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class LopyLunaLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    static final Minecraft mc = Minecraft.getInstance();
    public LopyLunaLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> pRenderer) {
        super(pRenderer);
    }

    @Override
    public void render(@NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight, AbstractClientPlayer pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        if (("ab49cc7b-53e9-424e-8fa1-778186ffae33".equals(pLivingEntity.getUUID().toString()) || "Dev".equals(pLivingEntity.getName().getString())) && !pLivingEntity.isInvisible()) {
            VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.eyes(pLivingEntity.getSkinTextureLocation()));
            int i = LivingEntityRenderer.getOverlayCoords(pLivingEntity, 0.0F);
            pPoseStack.pushPose();
            PlayerModel<AbstractClientPlayer> player = this.getParentModel();
            ModelPart modelPart = mc.getEntityModels().bakeLayer(LOC);
            PlayerModel<AbstractClientPlayer> model = new PlayerModel<>(modelPart, true); 
            model.swimAmount = player.swimAmount;
            model.leftArmPose = player.leftArmPose;
            model.rightArmPose = player.rightArmPose;
            player.copyPropertiesTo(model);
            model.setupAnim(pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
            Color color = pLivingEntity.isCrouching() ? new Color(64, 64, 64, 255) : new Color(128, 128, 128, 255);
            model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, i, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
            pPoseStack.popPose();
        }
    }
}
