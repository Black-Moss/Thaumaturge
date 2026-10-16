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
    private static final float POOF_VOLUME = 0.4F;
    private static final float POOF_PITCH_WOBBLE = 0.05F;

    private static final int PUFF_COUNT_BASE = 6;
    private static final int PUFF_COUNT_BOUND = 3;
    private static final int PUFF_COUNT_EXTRA = 2;
    private static final double PUFF_SPEED_BASE = 0.05;
    private static final double PUFF_SPEED_SPREAD = 0.05;
    private static final double PUFF_SIDE_PUSH = 0.1;
    private static final double PUFF_OPENING_TICKS = 2.0;
    private static final double PUFF_MOTION_SHARE = 0.5;
    private static final double PUFF_COLOR_WOBBLE = 0.1;

    private static final int FANCY_MOTE_BASE = 2;
    private static final int FANCY_MOTE_BOUND = 3;
    private static final double FANCY_MOTE_SPEED_BASE = 0.025;
    private static final double FANCY_MOTE_SPEED_SPREAD = 0.025;
    private static final double FANCY_MOTE_OPENING_TICKS = 2.0;
    private static final float FANCY_MOTE_BUOYANCY = -0.01F;
    private static final int FANCY_MOTE_AGE_BASE = 15;
    private static final int FANCY_MOTE_AGE_BOUND = 10;
    private static final EffectColor FLASH_COLOR = new EffectColor(1.0F, 0.9F, 1.0F);

    private static final int WISP_BOUND = 3;
    private static final int WISP_FANCY_EXTRA = 2;
    private static final float WISP_SCALE = 1.0F;
    private static final float WISP_ALPHA = 0.75F;
    private static final float WISP_RED_BASE = 0.9F;
    private static final float WISP_RED_SPREAD = 0.1F;
    private static final float WISP_GREEN_BASE = 0.1F;
    private static final float WISP_BLUE_BASE = 0.5F;
    private static final float WISP_BLUE_SPREAD = 0.1F;
    private static final float WISP_AVERAGE_DIVISOR = 2.0F;

    private PuffCloud() {}

    static void send(ServerLevel level, Vec3 pos, EffectColor base, boolean sound, boolean fancy, @Nullable Direction side) {
        RandomSource random = level.getRandom();
        if (sound) {
            float pitch = 1.0F + (float) (random.nextGaussian() * POOF_PITCH_WOBBLE);
            level.playSound(null, pos.x, pos.y, pos.z, TTSounds.POOF.get(), SoundSource.BLOCKS, POOF_VOLUME, pitch);
        }
        Vec3 push = side == null ? Vec3.ZERO : side.getUnitVec3().scale(PUFF_SIDE_PUSH);
        int puffs = PUFF_COUNT_BASE + random.nextInt(PUFF_COUNT_BOUND) + PUFF_COUNT_EXTRA;
        for (int i = 0; i < puffs; i++) {
            puff(level, pos, base, push, random);
        }
        if (fancy) {
            fancyFlourish(level, pos, random);
        }
        wisps(level, pos, base, fancy, side, random);
    }

    private static void puff(ServerLevel level, Vec3 pos, EffectColor base, Vec3 push, RandomSource random) {
        Vec3 launch = new Vec3(EffectRandom.signedSpeed(random, PUFF_SPEED_BASE, PUFF_SPEED_SPREAD), EffectRandom.signedSpeed(random, PUFF_SPEED_BASE, PUFF_SPEED_SPREAD),
                EffectRandom.signedSpeed(random, PUFF_SPEED_BASE, PUFF_SPEED_SPREAD)).add(push);
        Vec3 start = pos.add(launch.scale(PUFF_OPENING_TICKS));
        EffectColor shade = new EffectColor(wobble(base.r(), random), wobble(base.g(), random), wobble(base.b(), random));
        Vec3 drift = launch.scale(PUFF_MOTION_SHARE);
        Effects.spawn(level, TTParticles.colorOf(TTParticles.PUFF, shade.r(), shade.g(), shade.b()), start.x, start.y, start.z, drift.x, drift.y, drift.z);
    }

    private static float wobble(float channel, RandomSource random) {
        return Mth.clamp(channel * (1.0F + (float) (random.nextGaussian() * PUFF_COLOR_WOBBLE)), 0.0F, 1.0F);
    }

    private static void fancyFlourish(ServerLevel level, Vec3 pos, RandomSource random) {
        int motes = FANCY_MOTE_BASE + random.nextInt(FANCY_MOTE_BOUND);
        for (int i = 0; i < motes; i++) {
            Vec3 velocity = new Vec3(EffectRandom.signedSpeed(random, FANCY_MOTE_SPEED_BASE, FANCY_MOTE_SPEED_SPREAD), EffectRandom.signedSpeed(random, FANCY_MOTE_SPEED_BASE, FANCY_MOTE_SPEED_SPREAD),
                    EffectRandom.signedSpeed(random, FANCY_MOTE_SPEED_BASE, FANCY_MOTE_SPEED_SPREAD));
            Vec3 start = pos.add(velocity.scale(FANCY_MOTE_OPENING_TICKS));
            int color = EffectColor.randomMote(random).argb();
            int age = FANCY_MOTE_AGE_BASE + random.nextInt(FANCY_MOTE_AGE_BOUND);
            WispyMoteParticleOptions mote = new WispyMoteParticleOptions(color, age, FANCY_MOTE_BUOYANCY, WispyMoteParticleOptions.NO_ENTITY);
            Effects.spawn(level, mote, start.x, start.y, start.z, velocity.x, velocity.y, velocity.z);
        }
        Effects.spawn(level, TTParticles.colorOf(TTParticles.FLASH, FLASH_COLOR.r(), FLASH_COLOR.g(), FLASH_COLOR.b()), pos.x, pos.y, pos.z);
    }

    private static void wisps(ServerLevel level, Vec3 pos, EffectColor base, boolean fancy, @Nullable Direction side, RandomSource random) {
        int count = (fancy ? WISP_FANCY_EXTRA : 0) + random.nextInt(WISP_BOUND);
        for (int seed = 0; seed < count; seed++) {
            float r = (WISP_RED_BASE + random.nextFloat() * WISP_RED_SPREAD + base.r()) / WISP_AVERAGE_DIVISOR;
            float g = (WISP_GREEN_BASE + base.g()) / WISP_AVERAGE_DIVISOR;
            float b = (WISP_BLUE_BASE + random.nextFloat() * WISP_BLUE_SPREAD + base.b()) / WISP_AVERAGE_DIVISOR;
            Effects.curlyWisp(level, pos).color(r, g, b).alpha(WISP_ALPHA).scale(WISP_SCALE).side(side).seed(seed).send();
        }
    }
}
