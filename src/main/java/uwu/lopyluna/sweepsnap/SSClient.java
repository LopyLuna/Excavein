package uwu.lopyluna.sweepsnap;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class SSClient {
    public static void onCtorClient(IEventBus modEventBus, IEventBus forgeEventBus) {
        modEventBus.addListener(SSClient::clientInit);
    }

    public static void clientInit(final FMLClientSetupEvent event) {
    }
}
