package com.leclowndu93150.thaumaturge.content.spell.fx;

import com.leclowndu93150.thaumaturge.content.particle.AirGustParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.CrackShardParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.EarthPebbleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FlameFanParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FluxSwirlParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FrostFlakeParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.RiftShardParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.ShieldSparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispyMoteParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;

final class FxFamilies {
    private static final float OPAQUE = 1.0F;
    private static final float NO_CHANNEL = 0.0F;

    private FxFamilies() {}

    static ParticleOptions sparkle(FxParams params, int color, RandomSource random) {
        return new SparkleParticleOptions(ARGB.opaque(color), params.scale().uniform(random), params.delay().whole(random), params.decay(), params.vertical(), params.age().whole(random),
                params.toggle());
    }

    static ParticleOptions mote(FxParams params, int color, RandomSource random) {
        return new WispyMoteParticleOptions(ARGB.opaque(color), params.age().whole(random), params.vertical(), WispyMoteParticleOptions.NO_ENTITY, params.toggle());
    }

    static ParticleOptions flame(FxParams params, int color, RandomSource random) {
        return new FlameFanParticleOptions(params.scale().gaussian(random), params.vertical(), params.alpha());
    }

    static ParticleOptions frost(FxParams params, int color, RandomSource random) {
        return new FrostFlakeParticleOptions(params.scale().gaussian(random));
    }

    static ParticleOptions gust(FxParams params, int color, RandomSource random) {
        return new AirGustParticleOptions(params.scale().gaussian(random));
    }

    static ParticleOptions pebble(FxParams params, int color, RandomSource random) {
        return new EarthPebbleParticleOptions(params.scale().gaussian(random));
    }

    static ParticleOptions rift(FxParams params, int color, RandomSource random) {
        return new RiftShardParticleOptions(params.scale().gaussian(random));
    }

    static ParticleOptions flux(FxParams params, int color, RandomSource random) {
        float shade = params.shade().uniform(random);
        return new FluxSwirlParticleOptions(ARGB.colorFromFloat(OPAQUE, shade, NO_CHANNEL, shade), params.scale().uniform(random), params.endScale().uniform(random));
    }

    static ParticleOptions crack(FxParams params, int color, RandomSource random) {
        return new CrackShardParticleOptions(params.fixedColor(), params.variant().whole(random), params.scale().gaussian(random), params.age().whole(random));
    }

    static ParticleOptions ward(FxParams params, int color, RandomSource random) {
        return new ShieldSparkParticleOptions(ARGB.opaque(color), params.alpha(), params.scale().uniform(random), params.age().whole(random), params.delay().whole(random), params.toggle());
    }

    static ParticleOptions bubble(FxParams params, int color, RandomSource random) {
        return new BubbleParticleOptions(ARGB.opaque(color), params.alpha(), params.scale().uniform(random), params.age().whole(random), params.vertical(), params.toggle());
    }

    static ParticleOptions spark(FxParams params, int color, RandomSource random) {
        return new SparkParticleOptions(ARGB.opaque(color), params.alpha(), params.scale().uniform(random));
    }

    static ParticleOptions leaf(FxParams params, int color, RandomSource random) {
        return TTParticles.colorOf(TTParticles.LEAF_MOTE, color);
    }

    static ParticleOptions heal(FxParams params, int color, RandomSource random) {
        return TTParticles.HEAL_FLASH.get();
    }

    static ParticleOptions curse(FxParams params, int color, RandomSource random) {
        return TTParticles.CURSE_SMOKE.get();
    }

    static ParticleOptions primal(FxParams params, int color, RandomSource random) {
        return TTParticles.PRIMAL_FLARE.get();
    }

    static ParticleOptions smoke(FxParams params, int color, RandomSource random) {
        return ParticleTypes.LARGE_SMOKE;
    }
}
