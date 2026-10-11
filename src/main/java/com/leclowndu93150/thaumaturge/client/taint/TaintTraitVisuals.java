package com.leclowndu93150.thaumaturge.client.taint;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.client.trait.MobTraitVisuals;
import com.leclowndu93150.thaumaturge.content.particle.FluxSwirlParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class TaintTraitVisuals {
    private static final float ALPHA = 0.3F;
    private static final float RED_BASE = 0.16F;
    private static final float RED_SPREAD = 0.1F;
    private static final float GREEN = 0.03F;
    private static final float BLUE_BASE = 0.14F;
    private static final float BLUE_SPREAD = 0.08F;
    private static final float SCALE_BASE = 4.0F;
    private static final float SCALE_SPREAD = 2.0F;
    private static final float END_GROWTH = 1.25F;
    private static final double DRIFT = -0.004D;

    private TaintTraitVisuals() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(TaintTraitVisuals::register);
    }

    private static void register() {
        MobTraitVisuals.registerParticles(MobTraits.TAINTED, TaintTraitVisuals::fluxSwirl);
        MobTraitVisuals.registerParticles(TTMobTraits.TAINT_BROOD.getKey(), TaintTraitVisuals::fluxSwirl);
    }

    private static void fluxSwirl(LivingEntity mob, Level level, RandomSource random, double x, double y, double z) {
        float shade = random.nextFloat();
        float red = RED_BASE + RED_SPREAD * shade;
        float blue = BLUE_BASE + BLUE_SPREAD * (1.0F - shade);
        float scale = SCALE_BASE + random.nextFloat() * SCALE_SPREAD;
        int color = ARGB.colorFromFloat(ALPHA, red, GREEN, blue);
        level.addParticle(new FluxSwirlParticleOptions(color, scale, scale * END_GROWTH), x, y, z, 0.0, DRIFT, 0.0);
    }
}
