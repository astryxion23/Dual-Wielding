package net.dualwielding;

import net.dualwielding.init.ParticleInit;
import net.dualwielding.network.AttackEntityPayload;
import net.dualwielding.access.PlayerAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Mod(DualWieldingMain.MODID)
public class DualWieldingMain {

    public static final String MODID = "dualwielding";

    public DualWieldingMain(IEventBus modEventBus) {
        ParticleInit.PARTICLE_TYPES.register(modEventBus);
        modEventBus.addListener(DualWieldingMain::registerPayloadHandlers);
    }

    private static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(AttackEntityPayload.TYPE, AttackEntityPayload.STREAM_CODEC, DualWieldingMain::handleAttackEntity);
    }

    private static void handleAttackEntity(AttackEntityPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            player.resetLastActionTime();
            Entity entity = player.level().getEntity(payload.entityId());
            if (entity != null) {
                ((PlayerAccess) player).attackOffhand(entity);
            }
        });
    }

}
