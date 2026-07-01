package com.leclowndu93150.thaumcraft.client.fx.render.particle;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.registry.TCParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCParticleProviders {
    private TCParticleProviders() {}

    @SubscribeEvent
    public static void onRegister(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(TCParticles.FX_GENERIC.get(), FXGenericParticle.Provider::new);
        event.registerSpriteSet(TCParticles.FIRE_MOTE.get(), FXFireMoteParticle.Provider::new);
        event.registerSpriteSet(TCParticles.NITOR_CORE.get(), NitorCoreParticle.Provider::new);
        event.registerSpriteSet(TCParticles.VENT.get(), VentParticle.Provider::new);
        event.registerSpriteSet(TCParticles.BLOCK_RUNES.get(), BlockRunesParticle.Provider::new);
        event.registerSpriteSet(TCParticles.SMOKE_SPIRAL.get(), SmokeSpiralParticle.Provider::new);
        event.registerSpriteSet(TCParticles.VIS_SPARKLE.get(), VisSparkleParticle.Provider::new);
        event.registerSpriteSet(TCParticles.WISP.get(), WispParticle.Provider::new);
        event.registerSpriteSet(TCParticles.ESSENTIA_DROP.get(), EssentiaDropParticle.Provider::new);
        event.registerSpecial(TCParticles.BORE_PARTICLES.get(), new BoreParticlesParticle.Provider());
        event.registerSpriteSet(TCParticles.BORE_SPARKLE.get(), BoreSparkleParticle.Provider::new);
        event.registerSpecial(TCParticles.FLUX_GOO_DROPLET.get(), new FluxGooDropletParticle.Provider());
    }
}
