package com.leclowndu93150.thaumaturge.content.effect;

import com.leclowndu93150.thaumaturge.content.particle.WispyMoteParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

final class PuffCloud {
    private static final float SOUND_VOLUME = 0.4F;
    private static final double SOUND_PITCH_BASE = 1.0;
    private static final double SOUND_PITCH_DEVIATION = 0.05;
    private static final int PUFF_BASE_COUNT = 6;
    private static final int PUFF_COUNT_BONUS = 2;
    private static final int PUFF_COUNT_RANGE = 3;
    private static final double PUFF_SPEED_BASE = 0.05;
    private static final double PUFF_SPEED_SPREAD = 0.05;
    private static final double PUFF_SIDE_PUSH = 0.1;
    private static final double PUFF_SPAWN_LEAD = 2.0;
    private static final double PUFF_MOTION_FACTOR = 0.5;
    private static final double PUFF_COLOR_DEVIATION = 0.1;
    private static final int MOTE_BASE_COUNT = 2;
    private static final int MOTE_COUNT_RANGE = 3;
    private static final double MOTE_SPEED_BASE = 0.025;
    private static final double MOTE_SPEED_SPREAD = 0.025;
    private static final double MOTE_SPAWN_LEAD = 2.0;
    private static final int MOTE_AGE_BASE = 15;
    private static final int MOTE_AGE_RANGE = 10;
    private static final float MOTE_GRAVITY = -0.01F;
    private static final float FLASH_RED = 1.0F;
    private static final float FLASH_GREEN = 0.9F;
    private static final float FLASH_BLUE = 1.0F;
    private static final int FANCY_WISP_BONUS = 2;
    private static final int WISP_COUNT_RANGE = 3;
    private static final float WISP_RED_BASE = 0.9F;
    private static final float WISP_RED_SPREAD = 0.1F;
    private static final float WISP_GREEN_BASE = 0.1F;
    private static final float WISP_BLUE_BASE = 0.5F;
    private static final float WISP_BLUE_SPREAD = 0.1F;
    private static final float WISP_ALPHA = 0.75F;

    private PuffCloud() {}

    static void send(ServerLevel level, Vec3 pos, EffectColor base, boolean sound, boolean fancy, @Nullable Direction side) {
        RandomSource random = level.getRandom();
        if (sound) {
            float pitch = (float) (SOUND_PITCH_BASE + random.nextGaussian() * SOUND_PITCH_DEVIATION);
            level.playSound(null, pos.x, pos.y, pos.z, TTSounds.POOF.get(), SoundSource.BLOCKS, SOUND_VOLUME, pitch);
        }
        sendPuffs(level, random, pos, base, side);
        if (fancy) {
            sendMotes(level, random, pos);
            Effects.spawn(level, TTParticles.colorOf(TTParticles.FLASH, FLASH_RED, FLASH_GREEN, FLASH_BLUE), pos.x, pos.y, pos.z);
        }
        sendWisps(level, random, pos, base, fancy, side);
    }

    private static void sendPuffs(ServerLevel level, RandomSource random, Vec3 pos, EffectColor base, @Nullable Direction side) {
        int count = PUFF_BASE_COUNT + random.nextInt(PUFF_COUNT_RANGE) + PUFF_COUNT_BONUS;
        for (int i = 0; i < count; i++) {
            double vx = EffectRandom.signedSpeed(random, PUFF_SPEED_BASE, PUFF_SPEED_SPREAD);
            double vy = EffectRandom.signedSpeed(random, PUFF_SPEED_BASE, PUFF_SPEED_SPREAD);
            double vz = EffectRandom.signedSpeed(random, PUFF_SPEED_BASE, PUFF_SPEED_SPREAD);
            if (side != null) {
                vx += side.getStepX() * PUFF_SIDE_PUSH;
                vy += side.getStepY() * PUFF_SIDE_PUSH;
                vz += side.getStepZ() * PUFF_SIDE_PUSH;
            }
            float r = jitter(random, base.r());
            float g = jitter(random, base.g());
            float b = jitter(random, base.b());
            Effects.spawn(level, TTParticles.colorOf(TTParticles.PUFF, r, g, b), pos.x + vx * PUFF_SPAWN_LEAD, pos.y + vy * PUFF_SPAWN_LEAD, pos.z + vz * PUFF_SPAWN_LEAD, vx * PUFF_MOTION_FACTOR,
                    vy * PUFF_MOTION_FACTOR, vz * PUFF_MOTION_FACTOR);
        }
    }

    private static void sendMotes(ServerLevel level, RandomSource random, Vec3 pos) {
        int count = MOTE_BASE_COUNT + random.nextInt(MOTE_COUNT_RANGE);
        for (int i = 0; i < count; i++) {
            double vx = EffectRandom.signedSpeed(random, MOTE_SPEED_BASE, MOTE_SPEED_SPREAD);
            double vy = EffectRandom.signedSpeed(random, MOTE_SPEED_BASE, MOTE_SPEED_SPREAD);
            double vz = EffectRandom.signedSpeed(random, MOTE_SPEED_BASE, MOTE_SPEED_SPREAD);
            int age = MOTE_AGE_BASE + random.nextInt(MOTE_AGE_RANGE);
            WispyMoteParticleOptions options = new WispyMoteParticleOptions(EffectColor.randomMote(random).argb(), age, MOTE_GRAVITY, WispyMoteParticleOptions.NO_ENTITY);
            Effects.spawn(level, options, pos.x + vx * MOTE_SPAWN_LEAD, pos.y + vy * MOTE_SPAWN_LEAD, pos.z + vz * MOTE_SPAWN_LEAD, vx, vy, vz);
        }
    }

    private static void sendWisps(ServerLevel level, RandomSource random, Vec3 pos, EffectColor base, boolean fancy, @Nullable Direction side) {
        int count = (fancy ? FANCY_WISP_BONUS : 0) + random.nextInt(WISP_COUNT_RANGE);
        for (int i = 0; i < count; i++) {
            float r = (WISP_RED_BASE + random.nextFloat() * WISP_RED_SPREAD + base.r()) / 2.0F;
            float g = (WISP_GREEN_BASE + base.g()) / 2.0F;
            float b = (WISP_BLUE_BASE + random.nextFloat() * WISP_BLUE_SPREAD + base.b()) / 2.0F;
            Effects.curlyWisp(level, pos).color(r, g, b).alpha(WISP_ALPHA).side(side).seed(i).send();
        }
    }

    private static double signedSpeed(RandomSource random, double base, double spread) {
        double speed = base + random.nextDouble() * spread;
        return random.nextBoolean() ? -speed : speed;
    }

    private static float jitter(RandomSource random, float channel) {
        return Mth.clamp((float) (channel * (1.0 + random.nextGaussian() * PUFF_COLOR_DEVIATION)), 0.0F, 1.0F);
    }
}
