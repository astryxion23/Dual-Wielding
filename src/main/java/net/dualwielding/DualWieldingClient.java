package net.dualwielding;

import net.dualwielding.init.ParticleInit;
import net.minecraft.client.particle.AttackSweepParticle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DualWieldingMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DualWieldingClient {

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleInit.OFFHAND_SWEEPING.get(), AttackSweepParticle.Provider::new);
    }

}
