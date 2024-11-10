package uwu.lopyluna.excavein;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.client.ConfigScreenFactoryRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import uwu.lopyluna.excavein.client.BlockOutlineRenderer;
import uwu.lopyluna.excavein.client.KeybindHandler;
import uwu.lopyluna.excavein.client.ModeOverlay;
import uwu.lopyluna.excavein.network.CooldownPacket;
import uwu.lopyluna.excavein.network.IsBreakingPacket;
import uwu.lopyluna.excavein.network.SelectionOutlinePacket;

import static uwu.lopyluna.excavein.Excavein.MOD_ID;

public class ExcaveinClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ConfigScreenFactoryRegistry.INSTANCE.register(MOD_ID, ConfigurationScreen::new);

        KeybindHandler.register();
        WorldRenderEvents.BLOCK_OUTLINE.register(BlockOutlineRenderer::onRenderWorld);
        ClientTickEvents.END_CLIENT_TICK.register(BlockOutlineRenderer::onClientTick);

        HudRenderCallback.EVENT.register(ModeOverlay::onRenderGuiOverlay);
        ClientTickEvents.END_CLIENT_TICK.register(ModeOverlay::onClientTick);
    }

    public void registerClientPackets() {
        ClientPlayNetworking.registerGlobalReceiver(SelectionOutlinePacket.TYPE, SelectionOutlinePacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(CooldownPacket.TYPE, CooldownPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(IsBreakingPacket.TYPE, IsBreakingPacket::handle);
    }
}
