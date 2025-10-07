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

public record KeybindPacket(UUID playerID, boolean keyPressed, int id, String mode) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<KeybindPacket> TYPE = new CustomPacketPayload.Type<>(Utils.asResource("keybind"));
    public static final StreamCodec<FriendlyByteBuf, KeybindPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, KeybindPacket::playerID,
            ByteBufCodecs.BOOL, KeybindPacket::keyPressed,
            ByteBufCodecs.INT, KeybindPacket::id,
            ByteBufCodecs.STRING_UTF8, KeybindPacket::mode,
            KeybindPacket::new
    );

    public static void handle(final KeybindPacket msg, final IPayloadContext ctx) {
        ctx.enqueueWork(() ->
                ExcaveinTacker.updateKey((ServerPlayer) ctx.player(), ctx.player().getUUID().equals(msg.playerID) ? ctx.player().getUUID() : msg.playerID, msg.keyPressed, msg.id, msg.mode)
        );
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
