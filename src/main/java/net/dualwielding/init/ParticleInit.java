package net.dualwielding.init;

import net.dualwielding.DualWieldingMain;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ParticleInit {

    public static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, DualWieldingMain.MODID);

    public static final RegistryObject<SimpleParticleType> OFFHAND_SWEEPING =
            PARTICLE_TYPES.register("offhand_sweeping", () -> new SimpleParticleType(true));

}
