package uwu.lopyluna.excavein;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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

    private void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MOD_ID);
        registrar = registrar.executesOn(HandlerThread.NETWORK  );
        registrar.playToServer(KeybindPacket.TYPE, KeybindPacket.CODEC, KeybindPacket::handle);
        registrar.playToServer(SelectionInspectionPacket.TYPE, SelectionInspectionPacket.CODEC, SelectionInspectionPacket::handle);
        registrar.playToClient(SelectionOutlinePacket.TYPE, SelectionOutlinePacket.CODEC, SelectionOutlinePacket::handle);
        registrar.playToClient(CooldownPacket.TYPE, CooldownPacket.CODEC, CooldownPacket::handle);
        registrar.playToClient(IsBreakingPacket.TYPE, IsBreakingPacket.CODEC, IsBreakingPacket::handle);
    }

    @Override
    public void onInitialize() {
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CLIENT_SPEC);
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SERVER_SPEC);

        if (FMLEnvironment.dist.isClient()) ExcaveinClient.client(modEventBus);
        modEventBus.addListener(this::onRegisterPayloadHandlers);

        ServerTickEvents.END_WORLD_TICK.register(CooldownTracker::onWorldTick);
        ServerPlayConnectionEvents.JOIN.register(CooldownTracker::onPlayerLogin);

        ServerTickEvents.END_WORLD_TICK.register(BlockPositionTracker::onWorldTick);

        PlayerBlockBreakEvents.BEFORE.register(BlockPositionTracker::onBlockBreak);
    }
}
