package net.dualwielding;

import net.dualwielding.init.ParticleInit;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.particle.AttackSweepParticle;

public class DualWieldingClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ParticleProviderRegistry.getInstance().register(ParticleInit.OFFHAND_SWEEPING, AttackSweepParticle.Provider::new);
    }
}
