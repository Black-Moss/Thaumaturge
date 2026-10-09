package com.leclowndu93150.thaumaturge.content.effect;

import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

final class SparkleSettings {
    private static final float DEFAULT_SCALE = 0.4F;
    private static final float DEFAULT_DECAY = 0.98F;
    private static final int DEFAULT_BASE_AGE = 16;

    Vec3 motion = Vec3.ZERO;
    EffectColor color = EffectColor.WHITE;
    float scale = DEFAULT_SCALE;
    int delay;
    float decay = DEFAULT_DECAY;
    float gravity;
    int baseAge = DEFAULT_BASE_AGE;

    void send(ServerLevel level, Vec3 pos, boolean flicker) {
        SparkleParticleOptions options = new SparkleParticleOptions(color.argb(), scale, delay, decay, gravity, baseAge, flicker);
        Effects.spawn(level, options, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
    }
}
