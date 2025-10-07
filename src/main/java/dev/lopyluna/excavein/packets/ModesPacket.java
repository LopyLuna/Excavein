package dev.lopyluna.excavein.packets;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import dev.lopyluna.excavein.tracker.ExcaveinTacker;
import dev.lopyluna.excavein.utils.Utils;

import java.util.UUID;

public record ModesPacket(UUID playerID, boolean keyPressed, int switchMode) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ModesPacket> TYPE = new CustomPacketPayload.Type<>(Utils.asResource("modes"));
    public static final StreamCodec<FriendlyByteBuf, ModesPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ModesPacket::playerID,
            ByteBufCodecs.BOOL, ModesPacket::keyPressed,
            ByteBufCodecs.INT, ModesPacket::switchMode,
            ModesPacket::new
    );

    public static void handle(final ModesPacket msg, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> ExcaveinTacker.update((ServerPlayer) ctx.player(), ctx.player().getUUID().equals(msg.playerID) ? ctx.player().getUUID() : msg.playerID, msg.keyPressed, msg.switchMode));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
