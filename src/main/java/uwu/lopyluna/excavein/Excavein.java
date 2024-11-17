package uwu.lopyluna.excavein;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import uwu.lopyluna.excavein.config.ClientConfig;
import uwu.lopyluna.excavein.config.ServerConfig;
import uwu.lopyluna.excavein.packets.*;
import uwu.lopyluna.excavein.registry.ExcaveinModes;

@SuppressWarnings("unused")
@Mod(Excavein.MOD_ID)
public class Excavein {
    public static final String NAME = "ExcaVein";
    public static final String MOD_ID = "excavein";
    public static final String VERSION = "1.0a.Release";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Excavein(IEventBus modEventBus, Dist dist, ModContainer container) {

        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CLIENT_SPEC);
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SERVER_SPEC);

        ExcaveinModes.register();

        if (FMLEnvironment.dist.isClient()) ExcaveinClient.client(modEventBus);
        modEventBus.addListener(this::onRegisterPayloadHandlers);
    }

    private void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MOD_ID);
        registrar = registrar.executesOn(HandlerThread.NETWORK);
        registrar.playToServer(ExcaveinPacket.TYPE, ExcaveinPacket.CODEC, ExcaveinPacket::handle);
        registrar.playToServer(KeybindPacket.TYPE, KeybindPacket.CODEC, KeybindPacket::handle);

        registrar.playToClient(SelectedBlocksPacket.TYPE, SelectedBlocksPacket.CODEC, SelectedBlocksPacket::handle);
        registrar.playToClient(CooldownPacket.TYPE, CooldownPacket.CODEC, CooldownPacket::handle);

        registrar.playToClient(ClientHelperModesPacket.TYPE, ClientHelperModesPacket.CODEC, ClientHelperModesPacket::handle);
        registrar.playToClient(ClientHelperBoolsPacket.TYPE, ClientHelperBoolsPacket.CODEC, ClientHelperBoolsPacket::handle);
    }

}
