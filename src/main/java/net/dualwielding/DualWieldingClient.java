package net.dualwielding;

import net.dualwielding.init.ParticleInit;
import net.minecraft.client.particle.AttackSweepParticle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = DualWieldingMain.MODID, value = Dist.CLIENT)
public class DualWieldingClient {

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleInit.OFFHAND_SWEEPING.get(), AttackSweepParticle.Provider::new);
    }

}
