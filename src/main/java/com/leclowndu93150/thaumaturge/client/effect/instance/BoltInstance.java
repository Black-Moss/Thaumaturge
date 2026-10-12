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
    private static final double MIN_POINTS = 3.0;
    private static final double MAX_POINTS = 512.0;
    private static final double SWAY = 0.1;
    private static final double WOBBLE_PER_TICK = 0.1;
    private static final double[] WAVE_DIVISORS = {4.0, 3.0, 2.0};
    private static final int PHASE_STEPS = 50;
    private static final int THIN_ODDS = 4;
    private static final float THIN_STEP = 0.25F;
    private static final float MIN_OPACITY = 0.1F;
    private static final double SHEATH_DIVISOR = 10.0;
    private static final double CORE_DIVISOR = 30.0;

    private final Vec3 origin;
    private final double dx;
    private final double dy;
    private final double dz;
    private final double span;
    private final float width;
    private final float red;
    private final float green;
    private final float blue;
    private final int count;
    private final double[][] sway;
    private final boolean[] thin;
    private final double[][] points;
    private final double[] sheathRadii;
    private final double[] coreRadii;
    private final float[][] sheathColours;
    private final float[][] coreColours;
    private double phase;
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
        this.span = Math.sqrt(this.dx * this.dx + this.dy * this.dy + this.dz * this.dz);
        boolean finite = Double.isFinite(this.span);
        this.count = finite ? (int) Mth.clamp(Math.floor(this.span * Math.PI), MIN_POINTS, MAX_POINTS) : 0;
        int total = finite ? this.count + 1 : 0;
        this.sway = new double[this.count][AXES];
        this.thin = new boolean[this.count];
        this.points = new double[total][AXES];
        this.sheathRadii = new double[total];
        this.coreRadii = new double[total];
        this.sheathColours = new float[total][CHANNELS];
        this.coreColours = new float[total][CHANNELS];
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !finite) {
            this.expired = true;
            return;
        }
        RandomSource random = level.getRandom();
        this.phase = random.nextInt(PHASE_STEPS) * Math.PI;
        for (int i = 1; i < this.count - 1; i++) {
            for (int axis = 0; axis < AXES; axis++) {
                this.sway[i][axis] = random.triangle(0.0, SWAY);
            }
            this.thin[i] = random.nextInt(THIN_ODDS) == 0;
        }
    }

    @Override
    public void tick() {
        if (this.age >= LIFE_TICKS) {
            this.expired = true;
        }
        this.age++;
    }

    @Override
    public boolean isExpired() {
        return this.expired;
    }

    public Vec3 origin() {
        return this.origin;
    }

    public void shape(float partialTick) {
        float lived = this.age + partialTick;
        float opacity = Mth.clamp(1.0F - lived / LIFE_TICKS, MIN_OPACITY, 1.0F);
        double amplitude = lived * WOBBLE_PER_TICK;
        double stride = this.span * Math.PI / this.count;
        int last = this.count - 1;
        setPoint(0, 0.0, 0.0, 0.0);
        for (int i = 1; i < last; i++) {
            double along = (double) i / last;
            double s = i * stride + this.phase;
            double[] offset = this.sway[i];
            double wx = amplitude * Math.sin(s / WAVE_DIVISORS[0]);
            double wy = amplitude * Math.sin(s / WAVE_DIVISORS[1]);
            double wz = amplitude * Math.sin(s / WAVE_DIVISORS[2]);
            setPoint(i, this.dx * along + offset[0] + wx, this.dy * along + offset[1] + wy, this.dz * along + offset[2] + wz);
        }
        setPoint(last, this.dx, this.dy, this.dz);
        double[] inner = this.points[last - 1];
        setPoint(this.count, this.dx * 2.0 - inner[0], this.dy * 2.0 - inner[1], this.dz * 2.0 - inner[2]);
        float thinning = 1.0F - THIN_STEP * this.age;
        for (int p = 0; p < this.points.length; p++) {
            float shrink = p > 0 && p < last && this.thin[p] ? thinning : 1.0F;
            this.sheathRadii[p] = this.width / SHEATH_DIVISOR * shrink;
            this.coreRadii[p] = this.width / CORE_DIVISOR * shrink;
            fill(this.sheathColours[p], opacity);
            fill(this.coreColours[p], opacity);
        }
    }

    private void setPoint(int index, double x, double y, double z) {
        double[] point = this.points[index];
        point[0] = x;
        point[1] = y;
        point[2] = z;
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
