package com.leclowndu93150.thaumaturge.client.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class WhirlwindSmokeEmitter {
    private static final int SPIRAL_COUNT = 5;
    private static final float SPIRAL_RADIUS = 1.5F;
    private static final int SPIRAL_ANGLE_RANGE = 360;
    private static final int SPIRAL_COLOR = 0xDDDDDD;
    private static final int SPIRAL_AIRBORNE_DEPTH = 2;
    private static final double RING_HEIGHT = 0.1;
    private static final double RING_SPEED = 0.2;
    private static final double RING_FULL_TURN = Math.PI * 2.0;
    private static final double HALF = 2.0;

    public void emit(Level level, LivingEntity user) {
        RandomSource random = level.getRandom();
        int floor = Mth.floor(user.getY()) - (user.onGround() ? 0 : SPIRAL_AIRBORNE_DEPTH);
        double middle = user.getY() + user.getBbHeight() / HALF;
        for (int i = 0; i < SPIRAL_COUNT; i++) {
            ClientEffects.spiralSmoke(level, user.getX(), middle, user.getZ(), SPIRAL_RADIUS, random.nextInt(SPIRAL_ANGLE_RANGE), floor, SPIRAL_COLOR);
        }
        if (user.onGround()) {
            double angle = random.nextDouble() * RING_FULL_TURN;
            level.addParticle(ParticleTypes.SMOKE, user.getX(), user.getY() + RING_HEIGHT, user.getZ(), Math.cos(angle) * RING_SPEED, 0.0, Math.sin(angle) * RING_SPEED);
        }
    }
}
