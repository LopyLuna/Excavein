package uwu.lopyluna.excavein.packets;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import uwu.lopyluna.excavein.tracker.ExcaveinTacker;
import uwu.lopyluna.excavein.utils.Utils;

import java.util.UUID;

public record ExcaveinPacket(UUID playerID, boolean keyPressed, boolean displayChat) implements CustomPacketPayload {
    public static final Type<ExcaveinPacket> TYPE = new Type<>(Utils.asResource("excavein"));
    public static final StreamCodec<FriendlyByteBuf, ExcaveinPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ExcaveinPacket::playerID,
            ByteBufCodecs.BOOL, ExcaveinPacket::keyPressed,
            ByteBufCodecs.BOOL, ExcaveinPacket::displayChat,
            ExcaveinPacket::new
    );

    public static void handle(final ExcaveinPacket msg, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> ExcaveinTacker.updateTick((ServerPlayer) ctx.player(), ctx.player().getUUID().equals(msg.playerID) ? ctx.player().getUUID() : msg.playerID, msg.keyPressed, msg.displayChat));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
