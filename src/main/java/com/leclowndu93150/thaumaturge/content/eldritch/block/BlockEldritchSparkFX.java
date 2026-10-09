package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public final class BlockEldritchSparkFX {
    private static final float COLOR_ALPHA = 1.0F;
    private static final float RED_BASE = 0.65F;
    private static final float RED_SPREAD = 0.1F;
    private static final float GREEN = 1.0F;
    private static final float BLUE = 1.0F;
    private static final float PARTICLE_ALPHA = 0.8F;
    private static final float PARTICLE_SCALE = 0.5F;

    private BlockEldritchSparkFX() {}

    public static void spawnShockSpark(Level level, BlockPos pos, RandomSource random) {
        int color = ARGB.colorFromFloat(COLOR_ALPHA, RED_BASE + random.nextFloat() * RED_SPREAD, GREEN, BLUE);
        level.addParticle(new SparkParticleOptions(color, PARTICLE_ALPHA, PARTICLE_SCALE), pos.getX() + random.nextFloat(), pos.getY() + random.nextFloat(), pos.getZ() + random.nextFloat(), 0.0, 0.0,
                0.0);
    }
}
