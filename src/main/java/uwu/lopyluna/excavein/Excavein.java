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
import uwu.lopyluna.excavein.network.CooldownPacket;
import uwu.lopyluna.excavein.network.KeybindPacket;
import uwu.lopyluna.excavein.network.SelectionInspectionPacket;
import uwu.lopyluna.excavein.network.SelectionOutlinePacket;

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

        if (FMLEnvironment.dist.isClient()) ExcaveinClient.client(modEventBus);
        modEventBus.addListener(this::onRegisterPayloadHandlers);
    }

    private void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MOD_ID);
        registrar = registrar.executesOn(HandlerThread.NETWORK  );
        registrar.playToServer(KeybindPacket.TYPE, KeybindPacket.CODEC, KeybindPacket::handle);
        registrar.playToServer(SelectionInspectionPacket.TYPE, SelectionInspectionPacket.CODEC, SelectionInspectionPacket::handle);
        registrar.playToClient(SelectionOutlinePacket.TYPE, SelectionOutlinePacket.CODEC, SelectionOutlinePacket::handle);
        registrar.playToClient(CooldownPacket.TYPE, CooldownPacket.CODEC, CooldownPacket::handle);
    }

}
