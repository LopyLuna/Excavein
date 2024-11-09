package uwu.lopyluna.excavein;

import com.mojang.logging.LogUtils;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import uwu.lopyluna.excavein.config.ClientConfig;
import uwu.lopyluna.excavein.config.ServerConfig;
import uwu.lopyluna.excavein.network.*;
import uwu.lopyluna.excavein.tracker.BlockPositionTracker;
import uwu.lopyluna.excavein.tracker.CooldownTracker;

@SuppressWarnings("unused")
public class Excavein implements ModInitializer {
    public static final String NAME = "ExcaVein";
    public static final String MOD_ID = "excavein";
    public static final String VERSION = "1.0a.Release";
    public static final Logger LOGGER = LogUtils.getLogger();

    private void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(KeybindPacket.TYPE, KeybindPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(SelectionInspectionPacket.TYPE, SelectionInspectionPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(SelectionOutlinePacket.TYPE, SelectionOutlinePacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(CooldownPacket.TYPE, CooldownPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(IsBreakingPacket.TYPE, IsBreakingPacket::handle);
    }

    @Override
    public void onInitialize() {
        NeoForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.CLIENT, ClientConfig.CLIENT_SPEC);
        NeoForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.SERVER, ServerConfig.SERVER_SPEC);

        registerPackets();

        ServerTickEvents.END_WORLD_TICK.register(CooldownTracker::onWorldTick);
        ServerPlayConnectionEvents.JOIN.register(CooldownTracker::onPlayerLogin);

        ServerTickEvents.END_WORLD_TICK.register(BlockPositionTracker::onWorldTick);

        PlayerBlockBreakEvents.BEFORE.register(BlockPositionTracker::onBlockBreak);
    }
}
