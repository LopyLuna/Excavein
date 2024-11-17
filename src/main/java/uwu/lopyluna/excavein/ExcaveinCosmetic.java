package uwu.lopyluna.excavein;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ExcaveinCosmetic {


    @SubscribeEvent
    public void onRegisterRenderers(EntityRenderersEvent.AddLayers event) {
        event.getSkins().forEach(skin -> {
            LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> player = event.getSkin(skin);
            if (player != null) {
                //player.addLayer(new Deadmau5EarsLayer(player));
            }
        });
    }
}
