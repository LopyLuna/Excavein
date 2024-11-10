package uwu.lopyluna.excavein;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import uwu.lopyluna.excavein.client.BlockOutlineRenderer;
import uwu.lopyluna.excavein.client.KeybindHandler;
import uwu.lopyluna.excavein.client.ModeOverlay;

public class ExcaveinClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KeybindHandler.register();
        WorldRenderEvents.BLOCK_OUTLINE.register(BlockOutlineRenderer::onRenderWorld);
        ClientTickEvents.END_CLIENT_TICK.register(BlockOutlineRenderer::onClientTick);

        HudRenderCallback.EVENT.register(ModeOverlay::onRenderGuiOverlay);
        ClientTickEvents.END_CLIENT_TICK.register(ModeOverlay::onClientTick);
    }
}
