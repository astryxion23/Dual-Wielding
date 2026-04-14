package net.dualwielding.init;

import net.dualwielding.DualWieldingMain;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ParticleInit {

    public static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, DualWieldingMain.MODID);

    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, SimpleParticleType> OFFHAND_SWEEPING =
            PARTICLE_TYPES.register("offhand_sweeping", () -> new SimpleParticleType(true));

}
