package uwu.lopyluna.excavein.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import uwu.lopyluna.excavein.client.BlockOutlineRenderer;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class SelectionOutlinePacket {
    private final Set<BlockPos> blockPositions;

    public SelectionOutlinePacket(Set<BlockPos> blockPositions) {
        this.blockPositions = blockPositions;
    }

    public static void encode(SelectionOutlinePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.blockPositions.size());
        for (BlockPos pos : msg.blockPositions) {
            buf.writeBlockPos(pos);
        }
    }

    public static SelectionOutlinePacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Set<BlockPos> blockPositions = new HashSet<>();
        for (int i = 0; i < size; i++) {
            blockPositions.add(buf.readBlockPos());
        }
        return new SelectionOutlinePacket(blockPositions);
    }

    public static void handle(SelectionOutlinePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (msg.blockPositions.isEmpty()) {
                BlockOutlineRenderer.setOutlineBlocks(Set.of());
            } else {
                BlockOutlineRenderer.setOutlineBlocks(msg.blockPositions);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
