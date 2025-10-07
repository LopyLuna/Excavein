package dev.lopyluna.excavein.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import dev.lopyluna.excavein.client.ClientHelper;
import dev.lopyluna.excavein.utils.Utils;

public record ClientHelperBoolsPacket(boolean currentlyBreaking, boolean requiredFlags,
                                      boolean flag) implements CustomPacketPayload {
    public static final Type<ClientHelperBoolsPacket> TYPE = new Type<>(Utils.asResource("chelperbools"));

    public static final StreamCodec<FriendlyByteBuf, ClientHelperBoolsPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ClientHelperBoolsPacket::currentlyBreaking,
            ByteBufCodecs.BOOL, ClientHelperBoolsPacket::requiredFlags,
            ByteBufCodecs.BOOL, ClientHelperBoolsPacket::flag,
            ClientHelperBoolsPacket::new
    );

    public static void handle(ClientHelperBoolsPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> ClientHelper.update(
                msg.currentlyBreaking,
                msg.requiredFlags,
                msg.flag
        ));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
