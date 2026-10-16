package com.leclowndu93150.thaumaturge.client.effect.instance;

import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import com.leclowndu93150.thaumaturge.client.effect.manager.IFXInstance;
import java.util.ArrayList;
import java.util.List;
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
    private static final double ARC_ENERGY = 0.115;
    private static final double RISE_DECAY_DIVISOR = 1.9;
    private static final double NEGLIGIBLE_RUN = 1.0E-9;
    private static final int MAX_STEPS = 50;
    private static final double JITTER = 0.25;
    private static final float CRACKLE_SCALE = 0.5F;
    private static final float CRACKLE_CHANNEL_BOOST = 3.0F;
    private static final int CRACKLE_DELAY_BASE = 2;
    private static final int CRACKLE_DELAY_RANGE = 3;
    private static final float CRACKLE_DRAG = 0.995F;
    private static final float CRACKLE_SINK = 0.1F;
    private static final int CRACKLE_BASE_AGE = 8;

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
        double apex = Math.max(height, dy + height);
        double launch = Math.sqrt(apex * ARC_ENERGY);
        double run = Math.sqrt(dx * dx + dz * dz);
        double stepX = 0.0;
        double stepZ = 0.0;
        if (run >= NEGLIGIBLE_RUN) {
            double reach = launch * run / (2.0 * (apex + Math.sqrt(apex * (apex - dy))));
            stepX = reach * dx / run;
            stepZ = reach * dz / run;
        }
        double firstLengthSq = stepX * stepX + launch * launch + stepZ * stepZ;
        double riseDecay = ARC_ENERGY / RISE_DECAY_DIVISOR;
        List<double[]> path = new ArrayList<>();
        path.add(new double[]{0.0, 0.0, 0.0});
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        for (int step = 0; step < MAX_STEPS; step++) {
            double remainingX = dx - x;
            double remainingY = dy - y;
            double remainingZ = dz - z;
            if (remainingX * remainingX + remainingY * remainingY + remainingZ * remainingZ <= firstLengthSq) {
                break;
            }
            x += stepX;
            y += launch - step * riseDecay;
            z += stepZ;
            path.add(new double[]{x + random.triangle(0.0, JITTER), y + random.triangle(0.0, JITTER), z + random.triangle(0.0, JITTER)});
        }
        path.add(new double[]{dx, dy, dz});
        return path.toArray(new double[0][]);
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
        float r = Math.min(1.0F, CRACKLE_CHANNEL_BOOST * this.red);
        float g = Math.min(1.0F, CRACKLE_CHANNEL_BOOST * this.green);
        float b = Math.min(1.0F, CRACKLE_CHANNEL_BOOST * this.blue);
        for (int i = 1; i < this.points.length - 1; i++) {
            double[] point = this.points[i];
            ClientEffects.sparkle(level, random, this.origin.x + point[0], this.origin.y + point[1], this.origin.z + point[2], 0.0, 0.0, 0.0, r, g, b, CRACKLE_SCALE,
                    CRACKLE_DELAY_BASE + random.nextInt(CRACKLE_DELAY_RANGE), CRACKLE_DRAG, CRACKLE_SINK, CRACKLE_BASE_AGE);
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
