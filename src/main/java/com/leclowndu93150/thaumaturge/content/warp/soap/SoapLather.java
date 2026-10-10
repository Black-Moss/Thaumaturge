package com.leclowndu93150.thaumaturge.content.warp.soap;

import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public record SoapLather(int bubbles, double spread, Supplier<SoundEvent> sound, float soundChance, float volume, float basePitch, float pitchSpread) {
    private static final double BUBBLE_RISE = 0.02;
    private static final double NO_DRIFT = 0.0;
    private static final float CERTAIN = 1.0F;
    private static final float FLAT = 0.0F;
    private static final double HALF = 0.5;

    public void play(Level level, LivingEntity user) {
        if (!level.isClientSide()) {
            return;
        }
        RandomSource random = user.getRandom();
        if (soundChance >= CERTAIN || random.nextFloat() < soundChance) {
            float pitch = pitchSpread > FLAT ? basePitch + random.nextFloat() * pitchSpread : basePitch;
            level.playLocalSound(user.getX(), user.getY(), user.getZ(), sound.get(), SoundSource.PLAYERS, volume, pitch, false);
        }
        AABB body = user.getBoundingBox();
        double halfWidth = spread * HALF;
        for (int i = 0; i < bubbles; i++) {
            double x = user.getX() + Mth.nextDouble(random, -halfWidth, halfWidth);
            double y = Mth.nextDouble(random, body.minY, body.maxY);
            double z = user.getZ() + Mth.nextDouble(random, -halfWidth, halfWidth);
            level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, NO_DRIFT, BUBBLE_RISE, NO_DRIFT);
        }
    }
}
