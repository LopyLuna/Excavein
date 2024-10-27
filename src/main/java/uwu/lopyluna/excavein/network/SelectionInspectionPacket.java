package uwu.lopyluna.excavein.network;

import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import uwu.lopyluna.excavein.Excavein;
import uwu.lopyluna.excavein.Utils;
import uwu.lopyluna.excavein.client.SelectionMode;
import uwu.lopyluna.excavein.config.ServerConfig;
import uwu.lopyluna.excavein.tracker.BlockPositionTracker;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class SelectionInspectionPacket {

    private final SelectionMode selectionMode;

    public SelectionInspectionPacket(SelectionMode mode) {
        selectionMode = mode;
    }

    public static void encode(SelectionInspectionPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.selectionMode);
    }

    public static SelectionInspectionPacket decode(FriendlyByteBuf buf) {
        return new SelectionInspectionPacket(buf.readEnum(SelectionMode.class));
    }

    public static void handle(SelectionInspectionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                Level world = player.serverLevel();
                BlockHitResult rayTrace = getPlayerRayTraceToBlock(player);
                if (rayTrace != null) {
                    Vec3 eye = player.getEyePosition();
                    Set<BlockPos> validBlocks = Utils.selectionInspection(
                            world,
                            player,
                            rayTrace,
                            new BlockPos(new Vec3i((int) eye.x, (int) eye.y, (int) eye.z)),
                            ServerConfig.SELECTION_MAX_BLOCK.get(),
                            (int) Objects.requireNonNull(player.getAttribute(ForgeMod.BLOCK_REACH.get())).getValue() + ServerConfig.SELECTION_ADD_RANGE.get(),
                            msg.selectionMode

                    );
                    BlockPositionTracker.update(player, rayTrace, validBlocks);
                    if (!validBlocks.isEmpty()) {
                        Excavein.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SelectionOutlinePacket(validBlocks));
                    } else {
                        Excavein.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SelectionOutlinePacket(Set.of()));
                    }
                } else {
                    Excavein.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SelectionOutlinePacket(Set.of()));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static BlockHitResult getPlayerRayTraceToBlock(ServerPlayer player) {
        double reachDistance = Objects.requireNonNull(player.getAttribute(ForgeMod.BLOCK_REACH.get())).getValue();
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookVector = player.getLookAngle().scale(reachDistance);
        Vec3 reachPosition = eyePosition.add(lookVector);

        BlockHitResult hitResult = player.serverLevel().clip(new net.minecraft.world.level.ClipContext(
                eyePosition, reachPosition,
                net.minecraft.world.level.ClipContext.Block.OUTLINE,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                player
        ));

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            return hitResult;
        }

        return null;
    }
}
