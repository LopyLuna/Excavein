package uwu.lopyluna.excavein;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.common.MinecraftForge;
import uwu.lopyluna.excavein.client.BlockOutlineRenderer;
import uwu.lopyluna.excavein.client.ClientHandler;
import uwu.lopyluna.excavein.client.ModeOverlay;

public class ExcaveinClient {

    public static void client(IEventBus modEventBus) {
        modEventBus.addListener(ExcaveinClient::clientSetup);
        modEventBus.addListener(ClientHandler::new);
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(ClientHandler.class);
        MinecraftForge.EVENT_BUS.register(BlockOutlineRenderer.class);
        MinecraftForge.EVENT_BUS.register(ModeOverlay.class);
    }
}
