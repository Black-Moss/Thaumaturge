package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class SparkParticle extends TTParticle {
    private static final int ROWS = 3;
    private static final int FRAMES_PER_ROW = 8;
    private static final int LIFETIME_BASE = 5;
    private static final int LIFETIME_RANGE = 5;
    private static final float SIZE_UNIT = 0.1F;

    private final int rowStart;
    private final boolean mirrored;

    private SparkParticle(ClientLevel level, double x, double y, double z, SparkParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        setColor(options.color());
        this.alpha = options.alpha();
        this.quadSize = options.scale() * SIZE_UNIT;
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.rowStart = this.random.nextInt(ROWS) * FRAMES_PER_ROW;
        this.mirrored = this.random.nextBoolean();
        frame(this.rowStart);
    }

    @Override
    protected void update() {
        frame(this.rowStart + this.age % FRAMES_PER_ROW);
    }

    @Override
    protected float getU0() {
        return this.mirrored ? super.getU1() : super.getU0();
    }

    @Override
    protected float getU1() {
        return this.mirrored ? super.getU0() : super.getU1();
    }

    public static final class Provider implements ParticleProvider<SparkParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("spark");

        @Override
        public Particle createParticle(SparkParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new SparkParticle(level, x, y, z, options, SHEET);
        }
    }
}
