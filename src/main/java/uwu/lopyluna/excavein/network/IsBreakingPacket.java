package uwu.lopyluna.excavein.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import uwu.lopyluna.excavein.Utils;
import uwu.lopyluna.excavein.client.BlockOutlineRenderer;

public record IsBreakingPacket(boolean isBreaking) implements CustomPacketPayload {

    public static final Type<IsBreakingPacket> TYPE = new Type<>(Utils.asResource("breaking"));
    public static final StreamCodec<FriendlyByteBuf, IsBreakingPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, IsBreakingPacket::isBreaking,
            IsBreakingPacket::new
    );

    public static void handle(IsBreakingPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> BlockOutlineRenderer.setBreaking(msg.isBreaking));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
