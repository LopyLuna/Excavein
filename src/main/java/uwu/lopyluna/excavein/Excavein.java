package uwu.lopyluna.excavein;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;
import uwu.lopyluna.excavein.client.BlockOutlineRenderer;
import uwu.lopyluna.excavein.client.KeybindHandler;
import uwu.lopyluna.excavein.config.ClientConfig;
import uwu.lopyluna.excavein.config.ServerConfig;
import uwu.lopyluna.excavein.network.CooldownPacket;
import uwu.lopyluna.excavein.network.KeybindPacket;
import uwu.lopyluna.excavein.network.SelectionInspectionPacket;
import uwu.lopyluna.excavein.network.SelectionOutlinePacket;

@SuppressWarnings({"unused", "removal"})
@Mod(Excavein.MOD_ID)
public class Excavein {
    public static final String NAME = "ExcaVein";
    public static final String MOD_ID = "excavein";
    public static final String VERSION = "1.0a.Release";
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

        modEventBus.addListener(KeybindHandler::register);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(KeybindHandler.class);
        MinecraftForge.EVENT_BUS.register(BlockOutlineRenderer.class);
    }

    @SuppressWarnings("all")
    private void commonSetup(final FMLCommonSetupEvent event) {
        int packetId = 0;
        CHANNEL.registerMessage(packetId++, SelectionInspectionPacket.class,
                SelectionInspectionPacket::encode,
                SelectionInspectionPacket::decode,
                SelectionInspectionPacket::handle);
        CHANNEL.registerMessage(packetId++, SelectionOutlinePacket.class,
                SelectionOutlinePacket::encode,
                SelectionOutlinePacket::decode,
                SelectionOutlinePacket::handle);
        CHANNEL.registerMessage(packetId++, CooldownPacket.class,
                CooldownPacket::encode,
                CooldownPacket::decode,
                CooldownPacket::handle);
        CHANNEL.registerMessage(packetId++, KeybindPacket.class,
                KeybindPacket::encode,
                (FriendlyByteBuf t) -> KeybindPacket.decode(),
                KeybindPacket::handle);

    }

}
