package uwu.lopyluna.excavein;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.EntityRenderersEvent;
import uwu.lopyluna.excavein.client.LopyLunaLayer;
import uwu.lopyluna.excavein.utils.Utils;

import static uwu.lopyluna.excavein.Excavein.MOD_ID;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ExcaveinCosmetic {

    public static ModelLayerLocation LOC = new ModelLayerLocation(Utils.asResource("playerslim"), "main");

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(LOC, () -> LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE.extend(0.5F), true), 64, 64));
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.AddLayers event) {
        event.getSkins().forEach(skin -> {
            LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> playerModel = event.getSkin(skin);
            if (playerModel != null) {
                playerModel.addLayer(new LopyLunaLayer(playerModel));
            }
        });
    }
}
