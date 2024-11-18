package uwu.lopyluna.excavein.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import uwu.lopyluna.excavein.client.ClientHelper;
import uwu.lopyluna.excavein.utils.Utils;

public record ClientHelperModesPacket(String currentMode, String previousMode, String nextMode,
                                      String currentModifier, String previousModifier,
                                      String nextModifier) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientHelperModesPacket> TYPE = new CustomPacketPayload.Type<>(Utils.asResource("chelpermodes"));

    public static final StreamCodec<FriendlyByteBuf, ClientHelperModesPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientHelperModesPacket::currentMode,
            ByteBufCodecs.STRING_UTF8, ClientHelperModesPacket::previousMode,
            ByteBufCodecs.STRING_UTF8, ClientHelperModesPacket::nextMode,
            ByteBufCodecs.STRING_UTF8, ClientHelperModesPacket::currentModifier,
            ByteBufCodecs.STRING_UTF8, ClientHelperModesPacket::previousModifier,
            ByteBufCodecs.STRING_UTF8, ClientHelperModesPacket::nextModifier,
            ClientHelperModesPacket::new
    );

    public static void handle(ClientHelperModesPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> ClientHelper.update(
                msg.currentMode,
                msg.previousMode,
                msg.nextMode,
                msg.currentModifier,
                msg.previousModifier,
                msg.nextModifier
        ));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
