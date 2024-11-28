package uwu.lopyluna.excavein;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;
import uwu.lopyluna.excavein.config.ClientConfig;
import uwu.lopyluna.excavein.config.ServerConfig;
import uwu.lopyluna.excavein.packets.*;
import uwu.lopyluna.excavein.registry.ExcaveinModes;

@SuppressWarnings({"unused", "removal"})
@Mod(Excavein.MOD_ID)
public class Excavein {
    public static final String NAME = "ExcaVein";
    public static final String MOD_ID = "excavein";
    public static final String VERSION = "2.0a.Release";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public Excavein() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        ModLoadingContext modLoadingContext = ModLoadingContext.get();

        modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CLIENT_SPEC);
        modLoadingContext.registerConfig(ModConfig.Type.SERVER, ServerConfig.SERVER_SPEC);

        ExcaveinModes.register();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ExcaveinClient.client(modEventBus));
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }


    int packetId = 0;

    @SuppressWarnings("all")
    private void commonSetup(final FMLCommonSetupEvent event) {
        CHANNEL.registerMessage(packetId++, ExcaveinPacket.class, ExcaveinPacket::encode, ExcaveinPacket::decode, ExcaveinPacket::handle);
        CHANNEL.registerMessage(packetId++, ModesPacket.class, ModesPacket::encode, ModesPacket::decode, ModesPacket::handle);
        CHANNEL.registerMessage(packetId++, KeybindPacket.class, KeybindPacket::encode, KeybindPacket::decode, KeybindPacket::handle);

        CHANNEL.registerMessage(packetId++, SelectedBlocksPacket.class, SelectedBlocksPacket::encode, SelectedBlocksPacket::decode, SelectedBlocksPacket::handle);
        CHANNEL.registerMessage(packetId++, CooldownPacket.class, CooldownPacket::encode, CooldownPacket::decode, CooldownPacket::handle);

        CHANNEL.registerMessage(packetId++, ClientHelperModesPacket.class, ClientHelperModesPacket::encode, ClientHelperModesPacket::decode, ClientHelperModesPacket::handle);
        CHANNEL.registerMessage(packetId++, ClientHelperBoolsPacket.class, ClientHelperBoolsPacket::encode, ClientHelperBoolsPacket::decode, ClientHelperBoolsPacket::handle);
    }

}
