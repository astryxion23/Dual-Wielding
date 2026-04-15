package net.dualwielding;

import net.dualwielding.access.PlayerAccess;
import net.dualwielding.init.ParticleInit;
import net.dualwielding.network.AttackEntityPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class DualWieldingMain implements ModInitializer {

    public static final String MODID = "dualwielding";

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath(MODID, "offhand_sweeping"), ParticleInit.OFFHAND_SWEEPING);

        PayloadTypeRegistry.serverboundPlay().register(AttackEntityPayload.TYPE, AttackEntityPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(AttackEntityPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                player.resetLastActionTime();
                Entity entity = player.level().getEntity(payload.entityId());
                if (entity != null) {
                    ((PlayerAccess) player).attackOffhand(entity);
                }
            });
        });
    }
}
