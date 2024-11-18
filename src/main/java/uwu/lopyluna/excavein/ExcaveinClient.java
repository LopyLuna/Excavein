package uwu.lopyluna.excavein;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import uwu.lopyluna.excavein.client.BlockOutlineRenderer;
import uwu.lopyluna.excavein.client.ClientHandler;
import uwu.lopyluna.excavein.client.ModeOverlay;

public class ExcaveinClient {

    public static void client(IEventBus modEventBus) {
        modEventBus.addListener(ExcaveinClient::clientSetup);
        modEventBus.addListener(ClientHandler::new);
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(ClientHandler.class);
        NeoForge.EVENT_BUS.register(BlockOutlineRenderer.class);
        NeoForge.EVENT_BUS.register(ModeOverlay.class);
    }
}
