package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.CurlyWispParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;

public final class CurlyWispParticle extends TTParticle {
    private static final int FRAME_COUNT = 4;
    private static final int LIFETIME_BASE = 25;
    private static final int LIFETIME_RANGE_BASE = 20;
    private static final int LIFETIME_RANGE_PER_SEED = 20;
    private static final float START_SIZE_FACTOR = 0.5F;
    private static final float END_SIZE_BASE = 1.0F;
    private static final float END_SIZE_RANGE = 0.4F;
    private static final float SPIN_BASE = 2.0F;
    private static final float SPIN_RANGE = 2.0F;
    private static final float END_RED = 0.1F;
    private static final float END_GREEN = 0.0F;
    private static final float END_BLUE = 0.1F;
    private static final double TILT_DEVIATION_DEGREES = 90.0;

    private final float startAlpha;
    private final float startRed;
    private final float startGreen;
    private final float startBlue;
    private final float startSize;
    private final float endSize;
    private final FacingCameraMode facing;

    private CurlyWispParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, CurlyWispParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        this.startRed = ARGB.red(options.color()) / 255.0F;
        this.startGreen = ARGB.green(options.color()) / 255.0F;
        this.startBlue = ARGB.blue(options.color()) / 255.0F;
        this.startAlpha = options.alpha();
        setColor(this.startRed, this.startGreen, this.startBlue);
        this.alpha = this.startAlpha;
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE_BASE + LIFETIME_RANGE_PER_SEED * options.seed());
        this.startSize = START_SIZE_FACTOR * options.scale();
        this.endSize = (END_SIZE_BASE + this.random.nextFloat() * END_SIZE_RANGE) * options.scale();
        this.quadSize = this.startSize;
        frame(this.random.nextInt(FRAME_COUNT));
        float spin = SPIN_BASE + this.random.nextFloat() * SPIN_RANGE;
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? spin : -spin);
        setDelay(options.delay());
        this.facing = options.seed() > 0 && this.random.nextBoolean() ? fixedFacing() : FacingCameraMode.LOOKAT_XYZ;
    }

    private FacingCameraMode fixedFacing() {
        float yaw = (float) Math.toRadians(this.random.nextGaussian() * TILT_DEVIATION_DEGREES);
        float pitch = (float) Math.toRadians(this.random.nextGaussian() * TILT_DEVIATION_DEGREES);
        Quaternionf tilt = new Quaternionf().rotationYXZ(yaw, pitch, 0.0F);
        return (target, camera, partialTick) -> target.set(tilt);
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = this.startAlpha * (1.0F - t);
        this.quadSize = Mth.lerp(t, this.startSize, this.endSize);
        lerpColor(t, this.startRed, this.startGreen, this.startBlue, END_RED, END_GREEN, END_BLUE);
    }

    @Override
    public FacingCameraMode getFacingCameraMode() {
        return this.facing;
    }

    public static final class Provider implements ParticleProvider<CurlyWispParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("curly_wisp");

        @Override
        public Particle createParticle(CurlyWispParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new CurlyWispParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
