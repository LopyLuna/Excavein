package uwu.lopyluna.excavein.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import uwu.lopyluna.excavein.Utils;
import uwu.lopyluna.excavein.tracker.BlockPositionTracker;

import java.util.UUID;

public record KeybindPacket(boolean selectionKeyIsDown, UUID playerID) implements CustomPacketPayload {

    public static final Type<KeybindPacket> TYPE = new Type<>(Utils.asResource("keybind"));
    public static final StreamCodec<FriendlyByteBuf, KeybindPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, KeybindPacket::selectionKeyIsDown,
            UUIDUtil.STREAM_CODEC, KeybindPacket::playerID,
            KeybindPacket::new
    );

    public static void handle(final KeybindPacket msg, final IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player && player.getUUID().equals(msg.playerID))
            context.enqueueWork(() -> BlockPositionTracker.update(msg.selectionKeyIsDown, msg.playerID));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
