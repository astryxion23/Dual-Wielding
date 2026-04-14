package net.dualwielding;

import net.dualwielding.init.ParticleInit;
import net.dualwielding.network.PlayerAttackPacket;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DualWieldingMain.MODID)
public class DualWieldingMain {

    public static final String MODID = "dualwielding";

    public DualWieldingMain() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ParticleInit.PARTICLE_TYPES.register(modEventBus);
        modEventBus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(PlayerAttackPacket::init);
    }

}
