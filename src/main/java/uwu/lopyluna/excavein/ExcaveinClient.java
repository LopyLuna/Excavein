package uwu.lopyluna.excavein;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import uwu.lopyluna.excavein.client.BlockOutlineRenderer;
import uwu.lopyluna.excavein.client.KeybindHandler;

@Mod.EventBusSubscriber(modid = Excavein.MOD_ID, value = Dist.CLIENT)
public class ExcaveinClient {

    public static void client(IEventBus modEventBus) {
        modEventBus.addListener(ExcaveinClient::clientSetup);
        modEventBus.addListener(KeybindHandler::register);

    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(KeybindHandler.class);
        MinecraftForge.EVENT_BUS.register(BlockOutlineRenderer.class);
    }
}
