package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.BoreSparkleParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class BoreSparkleParticle extends SeekerParticle {
    private static final float DRIFT_STRENGTH = 0.01F;
    private static final float SIZE_BASE = 0.5F;
    private static final float SIZE_RANGE = 0.5F;
    private static final float SIZE_UNIT = 0.1F;
    private static final float PULSE_DIVISOR = 3.0F;
    private static final float PULSE_AMPLITUDE = 0.5F;
    private static final int FRAME_COUNT = 4;

    private BoreSparkleParticle(ClientLevel level, double x, double y, double z, BoreSparkleParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, sheet, options.targetEntityId(), new Vec3(options.tx(), options.ty(), options.tz()), Vec3.ZERO, DRIFT_STRENGTH);
        setColor(options.r(), options.g(), options.b());
        this.alpha = 1.0F;
        this.quadSize = SIZE_BASE + this.random.nextFloat() * SIZE_RANGE;
    }

    @Override
    protected void update() {
        frame(this.age % FRAME_COUNT);
    }

    @Override
    public float getQuadSize(float partialTick) {
        return this.quadSize * SIZE_UNIT * (1.0F + PULSE_AMPLITUDE * (float) Math.sin(this.age / PULSE_DIVISOR));
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    public static final class Provider implements ParticleProvider<BoreSparkleParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("bore_sparkle");

        @Override
        public Particle createParticle(BoreSparkleParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new BoreSparkleParticle(level, x, y, z, options, SHEET);
        }
    }
}
