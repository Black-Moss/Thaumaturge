package com.leclowndu93150.thaumaturge.client.effect.instance;

import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import com.leclowndu93150.thaumaturge.client.effect.manager.IFXInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class ArcInstance implements IFXInstance {
    public static final int LIFE_TICKS = 3;

    private static final float CHANNEL_SCALE = 255.0F;
    private static final float MIN_ARC_HEIGHT = 0.1F;
    private static final double FALL_RATE = 1.0;
    private static final double POINTS_PER_BLOCK = 2.5;
    private static final int MIN_INTERIOR_POINTS = 4;
    private static final int MAX_INTERIOR_POINTS = 32;
    private static final double SCATTER = 0.07;
    private static final float CRACKLE_PEAK = 0.95F;
    private static final float MIN_CHANNEL = 1.0E-3F;
    private static final float CRACKLE_SCALE = 0.5F;
    private static final int CRACKLE_DELAY_RANGE = 5;
    private static final float CRACKLE_DRAG = 0.9F;
    private static final float CRACKLE_SINK = 0.05F;
    private static final int CRACKLE_BASE_AGE = 13;

    private final Vec3 origin;
    private final double[][] points;
    private final float red;
    private final float green;
    private final float blue;
    private int age;
    private boolean crackled;
    private boolean expired;

    public ArcInstance(double sx, double sy, double sz, double tx, double ty, double tz, int color, float hint) {
        this.origin = new Vec3(sx, sy, sz);
        this.red = ARGB.red(color) / CHANNEL_SCALE;
        this.green = ARGB.green(color) / CHANNEL_SCALE;
        this.blue = ARGB.blue(color) / CHANNEL_SCALE;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            this.points = new double[0][];
            this.expired = true;
            return;
        }
        this.points = trace(level.getRandom(), tx - sx, ty - sy, tz - sz, Math.max(MIN_ARC_HEIGHT, hint));
    }

    private static double[][] trace(RandomSource random, double dx, double dy, double dz, float height) {
        double peak = Math.max(0.0, dy) + height;
        double rise = Math.sqrt(2.0 * FALL_RATE * peak);
        double timeUp = rise / FALL_RATE;
        double timeDown = Math.sqrt(2.0 * (peak - dy) / FALL_RATE);
        double flight = timeUp + timeDown;
        double travel = Math.sqrt(dx * dx + dz * dz) + peak + (peak - dy);
        int interior = Mth.clamp(Mth.ceil(travel * POINTS_PER_BLOCK), MIN_INTERIOR_POINTS, MAX_INTERIOR_POINTS);
        double[][] path = new double[interior + 2][];
        path[0] = new double[]{0.0, 0.0, 0.0};
        for (int i = 1; i <= interior; i++) {
            double t = flight * i / (interior + 1);
            double fraction = t / flight;
            double x = dx * fraction + random.triangle(0.0, SCATTER);
            double y = rise * t - 0.5 * FALL_RATE * t * t + random.triangle(0.0, SCATTER);
            double z = dz * fraction + random.triangle(0.0, SCATTER);
            path[i] = new double[]{x, y, z};
        }
        path[interior + 1] = new double[]{dx, dy, dz};
        return path;
    }

    @Override
    public void tick() {
        if (!this.crackled) {
            this.crackled = true;
            crackle();
        }
        this.age++;
        if (this.age >= LIFE_TICKS) {
            this.expired = true;
        }
    }

    private void crackle() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        RandomSource random = level.getRandom();
        float strongest = Math.max(MIN_CHANNEL, Math.max(this.red, Math.max(this.green, this.blue)));
        float boost = CRACKLE_PEAK / strongest;
        float r = Math.min(1.0F, this.red * boost);
        float g = Math.min(1.0F, this.green * boost);
        float b = Math.min(1.0F, this.blue * boost);
        for (int i = 1; i < this.points.length - 1; i++) {
            double[] point = this.points[i];
            ClientEffects.sparkle(level, random, this.origin.x + point[0], this.origin.y + point[1], this.origin.z + point[2], 0.0, 0.0, 0.0, r, g, b, CRACKLE_SCALE,
                    random.nextInt(CRACKLE_DELAY_RANGE), CRACKLE_DRAG, CRACKLE_SINK, CRACKLE_BASE_AGE);
        }
    }

    @Override
    public boolean isExpired() {
        return this.expired;
    }

    public Vec3 origin() {
        return this.origin;
    }

    public double[][] points() {
        return this.points;
    }

    public float alpha(float partialTick) {
        return Mth.clamp(1.0F - (this.age + partialTick) / LIFE_TICKS, 0.0F, 1.0F);
    }

    public float red() {
        return this.red;
    }

    public float green() {
        return this.green;
    }

    public float blue() {
        return this.blue;
    }
}
