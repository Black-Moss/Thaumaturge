package com.leclowndu93150.thaumaturge.client.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class WhirlwindSmokeEmitter {
    private static final int SPIRALS_PER_TICK = 5;
    private static final float SPIRAL_RADIUS = 1.5F;
    private static final int FULL_TURN_DEGREES = 360;
    private static final int AIRBORNE_FLOOR_DROP = 2;
    private static final int SPIRAL_COLOR = 0xDDDDDD;
    private static final double MID_HEIGHT = 0.5;
    private static final double PUFF_SPEED = 0.2;
    private static final double PUFF_LIFT = 0.1;

    public WhirlwindSmokeEmitter() {}

    public void emit(Level level, LivingEntity user) {
        RandomSource random = level.getRandom();
        double centreY = user.getY() + user.getBbHeight() * MID_HEIGHT;
        int floor = Mth.floor(user.getY()) - (user.onGround() ? 0 : AIRBORNE_FLOOR_DROP);
        for (int i = 0; i < SPIRALS_PER_TICK; i++) {
            ClientEffects.spiralSmoke(level, user.getX(), centreY, user.getZ(), SPIRAL_RADIUS, random.nextInt(FULL_TURN_DEGREES), floor, SPIRAL_COLOR);
        }
        if (user.onGround()) {
            double heading = random.nextDouble() * Mth.TWO_PI;
            level.addParticle(ParticleTypes.SMOKE, user.getX(), user.getY() + PUFF_LIFT, user.getZ(), Math.cos(heading) * PUFF_SPEED, 0.0,
                    Math.sin(heading) * PUFF_SPEED);
        }
    }
}
