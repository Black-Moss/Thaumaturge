package com.leclowndu93150.thaumaturge.client.hud.knowledge.spark;

import net.minecraft.util.RandomSource;

public final class SparkSpawner {
    private static final int ODDS_BASE = 1;
    private static final int ODDS_SPAN = 10;
    private static final float POSITION_SPREAD = 5.0F;
    private static final float VELOCITY_SPREAD = 1.0F;
    private static final int DELAY_RANGE = 5;
    private static final int LIFE_BASE = 32;
    private static final int LIFE_RANGE = 8;
    private static final float STAR_CHANCE = 0.2F;
    private static final int GREEN_BASE = 189;
    private static final int BLUE_BASE = 64;
    private static final int MAX_CHANNEL = 255;

    public void maybeSpawn(SparkSystem system, RandomSource random, float lifeFraction, float x, float y) {
        SparkPool pool = system.pool();
        if (pool.isFull()) {
            return;
        }
        int odds = (int) (ODDS_BASE + ODDS_SPAN * lifeFraction);
        if (random.nextInt(Math.max(1, odds)) != 0) {
            return;
        }
        double startX = x + random.nextGaussian() * POSITION_SPREAD;
        double startY = y + random.nextGaussian() * POSITION_SPREAD;
        double velocityX = random.nextGaussian() * VELOCITY_SPREAD;
        double velocityY = random.nextGaussian() * VELOCITY_SPREAD;
        int delay = random.nextInt(DELAY_RANGE);
        int life = LIFE_BASE + random.nextInt(LIFE_RANGE);
        boolean star = random.nextFloat() < STAR_CHANCE;
        int green = GREEN_BASE + random.nextInt(MAX_CHANNEL - GREEN_BASE + 1);
        int blue = BLUE_BASE + random.nextInt(MAX_CHANNEL - BLUE_BASE + 1);
        pool.add(startX, startY, velocityX, velocityY, delay, life, star, green, blue);
    }
}
