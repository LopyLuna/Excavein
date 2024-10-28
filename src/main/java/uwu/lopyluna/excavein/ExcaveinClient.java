package uwu.lopyluna.excavein;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import uwu.lopyluna.excavein.client.BlockOutlineRenderer;
import uwu.lopyluna.excavein.client.KeybindHandler;

public class ExcaveinClient {

    public static void client(IEventBus modEventBus) {
        modEventBus.addListener(ExcaveinClient::clientSetup);
        modEventBus.addListener(KeybindHandler::register);

    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(KeybindHandler.class);
        NeoForge.EVENT_BUS.register(BlockOutlineRenderer.class);
    }
}
