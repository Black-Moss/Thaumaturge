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
    private static final float POOF_VOLUME = 0.35F;
    private static final float POOF_PITCH_WOBBLE = 0.1F;

    private static final int PUFF_COUNT = 10;
    private static final double PUFF_SPEED_BASE = 0.04;
    private static final double PUFF_SPEED_SPREAD = 0.07;
    private static final double PUFF_SIDE_PUSH = 0.12;
    private static final double PUFF_OPENING_TICKS = 1.5;
    private static final double PUFF_MOTION_SHARE = 0.5;
    private static final double PUFF_COLOR_WOBBLE = 0.06;

    private static final int FANCY_MOTE_COUNT = 4;
    private static final double FANCY_MOTE_SPEED = 0.025;
    private static final double FANCY_MOTE_RISE = 0.01;
    private static final float FANCY_MOTE_BUOYANCY = -0.012F;
    private static final int FANCY_MOTE_AGE = 30;
    private static final EffectColor FLASH_COLOR = new EffectColor(0.93F, 0.86F, 1.0F);

    private static final EffectColor WISP_TINT = new EffectColor(1.0F, 0.22F, 0.72F);
    private static final float WISP_TINT_SHARE = 0.5F;
    private static final int WISP_MAX = 2;
    private static final int WISP_FANCY_EXTRA = 2;
    private static final double WISP_SPEED_BASE = 0.02;
    private static final double WISP_SPEED_SPREAD = 0.04;
    private static final float WISP_SCALE = 0.55F;
    private static final float WISP_ALPHA = 0.65F;

    private PuffCloud() {}

    static void send(ServerLevel level, Vec3 pos, EffectColor base, boolean sound, boolean fancy, @Nullable Direction side) {
        RandomSource random = level.getRandom();
        if (sound) {
            float pitch = 1.0F + (random.nextFloat() - random.nextFloat()) * POOF_PITCH_WOBBLE;
            level.playSound(null, pos.x, pos.y, pos.z, TTSounds.POOF.get(), SoundSource.BLOCKS, POOF_VOLUME, pitch);
        }
        Vec3 push = side == null ? Vec3.ZERO : side.getUnitVec3().scale(PUFF_SIDE_PUSH);
        for (int i = 0; i < PUFF_COUNT; i++) {
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
        return Mth.clamp(channel + (float) (random.nextGaussian() * PUFF_COLOR_WOBBLE), 0.0F, 1.0F);
    }

    private static void fancyFlourish(ServerLevel level, Vec3 pos, RandomSource random) {
        for (int i = 0; i < FANCY_MOTE_COUNT; i++) {
            float heading = random.nextFloat() * Mth.TWO_PI;
            int color = EffectColor.randomMote(random).argb();
            WispyMoteParticleOptions mote = new WispyMoteParticleOptions(color, FANCY_MOTE_AGE, FANCY_MOTE_BUOYANCY, WispyMoteParticleOptions.NO_ENTITY);
            Effects.spawn(level, mote, pos.x, pos.y, pos.z, Mth.cos(heading) * FANCY_MOTE_SPEED, FANCY_MOTE_RISE, Mth.sin(heading) * FANCY_MOTE_SPEED);
        }
        Effects.spawn(level, TTParticles.colorOf(TTParticles.FLASH, FLASH_COLOR.r(), FLASH_COLOR.g(), FLASH_COLOR.b()), pos.x, pos.y, pos.z);
    }

    private static void wisps(ServerLevel level, Vec3 pos, EffectColor base, boolean fancy, @Nullable Direction side, RandomSource random) {
        int bound = fancy ? WISP_MAX + WISP_FANCY_EXTRA : WISP_MAX;
        int count = random.nextInt(bound + 1);
        float r = Mth.lerp(WISP_TINT_SHARE, base.r(), WISP_TINT.r());
        float g = Mth.lerp(WISP_TINT_SHARE, base.g(), WISP_TINT.g());
        float b = Mth.lerp(WISP_TINT_SHARE, base.b(), WISP_TINT.b());
        for (int seed = 0; seed < count; seed++) {
            Effects.curlyWisp(level, pos).motion(EffectRandom.signedSpeed(random, WISP_SPEED_BASE, WISP_SPEED_SPREAD), EffectRandom.signedSpeed(random, WISP_SPEED_BASE, WISP_SPEED_SPREAD),
                    EffectRandom.signedSpeed(random, WISP_SPEED_BASE, WISP_SPEED_SPREAD)).color(r, g, b).alpha(WISP_ALPHA).scale(WISP_SCALE).side(side).seed(seed).send();
        }
    }
}
