package net.dualwielding.network;

import java.util.function.Supplier;

import net.dualwielding.access.PlayerAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

public class AttackEntityMessage {

    private final int entityId;

    public AttackEntityMessage(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(AttackEntityMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
    }

    public static AttackEntityMessage decode(FriendlyByteBuf buf) {
        return new AttackEntityMessage(buf.readInt());
    }

    public static void handle(AttackEntityMessage msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            player.resetLastActionTime();
            Entity entity = player.level().getEntity(msg.entityId);
            if (entity != null) {
                ((PlayerAccess) player).attackOffhand(entity);
            }
        });
        context.setPacketHandled(true);
    }
}
