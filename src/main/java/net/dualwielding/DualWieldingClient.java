package net.dualwielding;

import net.dualwielding.init.ParticleInit;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.AttackSweepParticle;

public class DualWieldingClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ParticleFactoryRegistry.getInstance().register(ParticleInit.OFFHAND_SWEEPING, sprites -> new AttackSweepParticle.Provider(sprites));
    }
}
