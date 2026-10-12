package com.leclowndu93150.thaumaturge.client.effect.instance;

import com.leclowndu93150.thaumaturge.client.effect.manager.IFXInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class BoltInstance implements IFXInstance {
    public static final int LIFE_TICKS = 3;

    private static final float CHANNEL_SCALE = 255.0F;
    private static final int AXES = 3;
    private static final int CHANNELS = 4;
    private static final int STEER_POINTS = 2;
    private static final int ENDS = 2;
    private static final double KINKS_PER_BLOCK = 3.0;
    private static final int MIN_KINKS = 3;
    private static final int MAX_KINKS = 96;
    private static final double JITTER = 0.06;
    private static final double WOBBLE = 0.22;
    private static final double[] WAVES_PER_BOLT = {1.3, 2.1, 1.7};
    private static final int THIN_ODDS = 4;
    private static final float THIN_FLOOR = 0.15F;
    private static final float MIN_OPACITY = 0.2F;
    private static final double SHEATH_RADIUS = 0.05;
    private static final double CORE_RADIUS = 0.015;
    private static final float SHEATH_OPACITY = 0.55F;

    private final Vec3 origin;
    private final double dx;
    private final double dy;
    private final double dz;
    private final float width;
    private final float red;
    private final float green;
    private final float blue;
    private final double[][] jitter;
    private final boolean[] thin;
    private final double[] phase = new double[AXES];
    private final double[][] points;
    private final double[] sheathRadii;
    private final double[] coreRadii;
    private final float[][] sheathColours;
    private final float[][] coreColours;
    private int age;
    private boolean expired;

    public BoltInstance(double startX, double startY, double startZ, double tx, double ty, double tz, int color, float width) {
        this.origin = new Vec3(startX, startY, startZ);
        this.dx = tx - startX;
        this.dy = ty - startY;
        this.dz = tz - startZ;
        this.width = width > 0.0F ? width : 1.0F;
        this.red = ARGB.red(color) / CHANNEL_SCALE;
        this.green = ARGB.green(color) / CHANNEL_SCALE;
        this.blue = ARGB.blue(color) / CHANNEL_SCALE;
        double length = Math.sqrt(this.dx * this.dx + this.dy * this.dy + this.dz * this.dz);
        int kinks = Mth.clamp(Mth.ceil(length * KINKS_PER_BLOCK), MIN_KINKS, MAX_KINKS);
        this.jitter = new double[kinks][AXES];
        this.thin = new boolean[kinks];
        int total = kinks + ENDS + STEER_POINTS;
        this.points = new double[total][AXES];
        this.sheathRadii = new double[total];
        this.coreRadii = new double[total];
        this.sheathColours = new float[total][CHANNELS];
        this.coreColours = new float[total][CHANNELS];
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            this.expired = true;
            return;
        }
        RandomSource random = level.getRandom();
        for (int axis = 0; axis < AXES; axis++) {
            this.phase[axis] = random.nextDouble() * Mth.TWO_PI;
        }
        for (int i = 0; i < kinks; i++) {
            for (int axis = 0; axis < AXES; axis++) {
                this.jitter[i][axis] = random.triangle(0.0, JITTER);
            }
            this.thin[i] = random.nextInt(THIN_ODDS) == 0;
        }
    }

    @Override
    public void tick() {
        this.age++;
        if (this.age >= LIFE_TICKS) {
            this.expired = true;
        }
    }

    @Override
    public boolean isExpired() {
        return this.expired;
    }

    public Vec3 origin() {
        return this.origin;
    }

    public void shape(float partialTick) {
        float life = Mth.clamp((this.age + partialTick) / LIFE_TICKS, 0.0F, 1.0F);
        float opacity = 1.0F - life * (1.0F - MIN_OPACITY);
        float thinning = Math.max(THIN_FLOOR, 1.0F - life);
        int kinks = this.jitter.length;
        int last = kinks + 1;
        setPoint(1, 0.0, 0.0, 0.0);
        for (int i = 0; i < kinks; i++) {
            double along = (double) (i + 1) / last;
            double pin = Math.sin(Math.PI * along);
            double[] offset = this.jitter[i];
            double wx = WOBBLE * life * pin * Math.sin(along * WAVES_PER_BOLT[0] * Mth.TWO_PI + this.phase[0]);
            double wy = WOBBLE * life * pin * Math.sin(along * WAVES_PER_BOLT[1] * Mth.TWO_PI + this.phase[1]);
            double wz = WOBBLE * life * pin * Math.sin(along * WAVES_PER_BOLT[2] * Mth.TWO_PI + this.phase[2]);
            setPoint(i + 2, this.dx * along + offset[0] + wx, this.dy * along + offset[1] + wy, this.dz * along + offset[2] + wz);
        }
        setPoint(last + 1, this.dx, this.dy, this.dz);
        extendSteer(0, 1, 2);
        extendSteer(last + 2, last + 1, last);
        for (int p = 0; p < this.points.length; p++) {
            int kink = p - 2;
            float shrink = kink >= 0 && kink < kinks && this.thin[kink] ? thinning : 1.0F;
            this.sheathRadii[p] = SHEATH_RADIUS * this.width * shrink;
            this.coreRadii[p] = CORE_RADIUS * this.width * shrink;
            fill(this.sheathColours[p], opacity * SHEATH_OPACITY);
            fill(this.coreColours[p], opacity);
        }
    }

    private void setPoint(int index, double x, double y, double z) {
        double[] point = this.points[index];
        point[0] = x;
        point[1] = y;
        point[2] = z;
    }

    private void extendSteer(int target, int end, int inner) {
        double[] from = this.points[end];
        double[] towards = this.points[inner];
        setPoint(target, from[0] * 2.0 - towards[0], from[1] * 2.0 - towards[1], from[2] * 2.0 - towards[2]);
    }

    private void fill(float[] colour, float alpha) {
        colour[0] = this.red;
        colour[1] = this.green;
        colour[2] = this.blue;
        colour[3] = alpha;
    }

    public double[][] points() {
        return this.points;
    }

    public double[] sheathRadii() {
        return this.sheathRadii;
    }

    public double[] coreRadii() {
        return this.coreRadii;
    }

    public float[][] sheathColours() {
        return this.sheathColours;
    }

    public float[][] coreColours() {
        return this.coreColours;
    }
}
