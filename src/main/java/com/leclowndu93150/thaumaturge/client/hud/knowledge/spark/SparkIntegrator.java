package com.leclowndu93150.thaumaturge.client.hud.knowledge.spark;

import net.minecraft.util.RandomSource;

public final class SparkIntegrator {
    private static final double DRAG = 0.9;
    private static final double KICK = 0.025;
    private static final double GRAVITY = 0.04;

    public boolean step(SparkPool pool, int index, RandomSource random) {
        if (pool.delay[index] > 0) {
            pool.delay[index]--;
            return false;
        }
        pool.lastX[index] = pool.x[index];
        pool.lastY[index] = pool.y[index];
        pool.x[index] += pool.velocityX[index];
        pool.y[index] += pool.velocityY[index];
        pool.velocityX[index] = pool.velocityX[index] * DRAG + random.nextGaussian() * KICK;
        pool.velocityY[index] = pool.velocityY[index] * DRAG + random.nextGaussian() * KICK + GRAVITY;
        pool.age[index]++;
        return pool.age[index] >= pool.life[index];
    }
}
