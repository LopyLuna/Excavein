package uwu.lopyluna.excavein.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import uwu.lopyluna.excavein.Utils;
import uwu.lopyluna.excavein.client.ClientCooldownHandler;

public record CooldownPacket(int cooldownTicks) implements CustomPacketPayload {

    public static final Type<CooldownPacket> TYPE = new Type<>(Utils.asResource("cooldown"));
    public static final StreamCodec<FriendlyByteBuf, CooldownPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CooldownPacket::cooldownTicks,
            CooldownPacket::new
    );

    public static void handle(CooldownPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> ClientCooldownHandler.setCooldown(msg.cooldownTicks));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
