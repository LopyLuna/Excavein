package uwu.lopyluna.excavein.network;

import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import uwu.lopyluna.excavein.Utils;
import uwu.lopyluna.excavein.client.SelectionMode;
import uwu.lopyluna.excavein.config.ServerConfig;
import uwu.lopyluna.excavein.tracker.BlockPositionTracker;

import java.util.Objects;
import java.util.Set;

public record SelectionInspectionPacket(int selectionMode) implements CustomPacketPayload {
    public static final Type<SelectionInspectionPacket> TYPE = new Type<>(Utils.asResource("selection_inspection"));
    public static final StreamCodec<FriendlyByteBuf, SelectionInspectionPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SelectionInspectionPacket::selectionMode,
            SelectionInspectionPacket::new
    );

    public static void handle(final SelectionInspectionPacket msg, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
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
                        (int) Objects.requireNonNull(player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE)).getValue() + ServerConfig.SELECTION_ADD_RANGE.get(),
                        SelectionMode.class.getEnumConstants()[msg.selectionMode]
                );
                BlockPositionTracker.update(player, rayTrace, validBlocks);
                if (!validBlocks.isEmpty()) {
                    PacketDistributor.sendToPlayer(player, new SelectionOutlinePacket(validBlocks));
                } else {
                    PacketDistributor.sendToPlayer(player, new SelectionOutlinePacket(Set.of()));
                }
            } else {
                PacketDistributor.sendToPlayer(player, new SelectionOutlinePacket(Set.of()));
            }
        });
    }

    private static BlockHitResult getPlayerRayTraceToBlock(ServerPlayer player) {
        double reachDistance = Objects.requireNonNull(player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE)).getValue();
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

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
