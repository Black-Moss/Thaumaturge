package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;

public final class PechCurseParticle extends TTParticle {
    private static final int FRAME_COUNT = 4;
    private static final int LIFETIME_BASE = 50;
    private static final int LIFETIME_RANGE = 50;
    private static final float START_RED = 0.9F;
    private static final float START_GREEN = 0.1F;
    private static final float START_BLUE = 0.5F;
    private static final float END_RED_BASE = 0.1F;
    private static final float END_GREEN = 0.0F;
    private static final float END_BLUE_BASE = 0.5F;
    private static final float END_COLOR_JITTER = 0.1F;
    private static final float START_SIZE = 0.3F;
    private static final float END_SIZE_BASE = 5.0F;
    private static final float END_SIZE_RANGE = 2.0F;
    private static final float SIZE_UNIT = 0.1F;
    private static final float ALPHA_SCALE = 0.75F;
    private static final float SPIN_BASE = 3.0F;
    private static final float SPIN_RANGE = 3.0F;
    private static final double TILT_DEVIATION_DEGREES = 90.0;

    private final float endRed;
    private final float endBlue;
    private final float endSize;
    private final FacingCameraMode facing;

    private PechCurseParticle(ClientLevel level, double x, double y, double z, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.endRed = END_RED_BASE + this.random.nextFloat() * END_COLOR_JITTER;
        this.endBlue = END_BLUE_BASE + this.random.nextFloat() * END_COLOR_JITTER;
        this.endSize = (END_SIZE_BASE + this.random.nextFloat() * END_SIZE_RANGE) * SIZE_UNIT;
        setColor(START_RED, START_GREEN, START_BLUE);
        this.quadSize = START_SIZE;
        frame(this.random.nextInt(FRAME_COUNT));
        float spin = SPIN_BASE + this.random.nextFloat() * SPIN_RANGE;
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? spin : -spin);
        float tiltYaw = (float) Math.toRadians(this.random.nextGaussian() * TILT_DEVIATION_DEGREES);
        float tiltPitch = (float) Math.toRadians(this.random.nextGaussian() * TILT_DEVIATION_DEGREES);
        Quaternionf tilt = new Quaternionf().rotationYXZ(tiltYaw, tiltPitch, 0.0F);
        this.facing = (target, camera, partialTick) -> target.set(tilt);
    }

    @Override
    protected void update() {
        float t = progress();
        lerpColor(t, START_RED, START_GREEN, START_BLUE, this.endRed, END_GREEN, this.endBlue);
        this.quadSize = Keyframes.sample(t, START_SIZE, this.endSize);
        this.alpha = ALPHA_SCALE * (1.0F - t);
    }

    @Override
    public FacingCameraMode getFacingCameraMode() {
        return this.facing;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("pech_curse");

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new PechCurseParticle(level, x, y, z, SHEET);
        }
    }
}
