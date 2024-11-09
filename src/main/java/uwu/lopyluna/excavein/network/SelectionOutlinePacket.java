package uwu.lopyluna.excavein.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import uwu.lopyluna.excavein.Utils;
import uwu.lopyluna.excavein.client.BlockOutlineRenderer;

import java.util.List;
import java.util.Set;

public record SelectionOutlinePacket(Set<BlockPos> blockPositions) implements CustomPacketPayload {

    public static final Type<SelectionOutlinePacket> TYPE = new Type<>(Utils.asResource("selection_outline"));
    public static final StreamCodec<FriendlyByteBuf, SelectionOutlinePacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()).map(Set::copyOf, List::copyOf), SelectionOutlinePacket::blockPositions,
            SelectionOutlinePacket::new
    );

    public static void handle(SelectionOutlinePacket msg, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            if (msg.blockPositions.isEmpty()) {
                BlockOutlineRenderer.setOutlineBlocks(Set.of());
            } else {
                BlockOutlineRenderer.setOutlineBlocks(msg.blockPositions);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
