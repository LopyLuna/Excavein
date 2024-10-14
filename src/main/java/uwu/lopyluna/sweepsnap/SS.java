package uwu.lopyluna.sweepsnap;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import static uwu.lopyluna.sweepsnap.SS.MOD_ID;

@SuppressWarnings("unused")
@Mod(MOD_ID)
public class SS {

    public static final String NAME = "SweepSnap";
    public static final String MOD_ID = "sweepsnap";
    public static final String VERSION = "1.0a.Release";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SS() {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;


        modEventBus.addListener(SS::init);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SSClient.onCtorClient(modEventBus, forgeEventBus));
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void init(final FMLCommonSetupEvent event) {

        event.enqueueWork(() -> {});
    }
}
