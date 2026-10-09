package com.leclowndu93150.thaumaturge.client.hud.knowledge.spark;

import net.minecraft.util.RandomSource;

public final class SparkSystem {
    private final SparkPool pool = new SparkPool();
    private final SparkIntegrator integrator = new SparkIntegrator();

    public SparkPool pool() {
        return pool;
    }

    public boolean isEmpty() {
        return pool.size() == 0;
    }

    public void clear() {
        pool.clear();
    }

    public void tick(RandomSource random) {
        int kept = 0;
        int count = pool.size();
        for (int i = 0; i < count; i++) {
            if (!integrator.step(pool, i, random)) {
                pool.move(i, kept);
                kept++;
            }
        }
        pool.truncate(kept);
    }
}
