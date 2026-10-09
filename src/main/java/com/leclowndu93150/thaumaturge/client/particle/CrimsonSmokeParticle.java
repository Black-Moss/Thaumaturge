package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public final class CrimsonSmokeParticle extends TTParticle {
    private static final int LIFETIME_BASE = 10;
    private static final int LIFETIME_RANGE = 10;
    private static final float ALPHA = 0.8F;
    private static final float SIZE_BASE = 3.0F;
    private static final float SIZE_RANGE = 2.0F;
    private static final float SIZE_UNIT = 0.1F;
    private static final float END_RED = 0.6F;

    private final ParticleSheet smokeSheet;

    private CrimsonSmokeParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        this.smokeSheet = sheet;
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.alpha = ALPHA;
        this.quadSize = (SIZE_BASE + this.random.nextFloat() * SIZE_RANGE) * SIZE_UNIT;
        frame(0);
    }

    @Override
    protected void update() {
        lerpColor(progress(), 1.0F, 1.0F, 1.0F, END_RED, 0.0F, 0.0F);
        frameByProgress();
    }

    @Override
    public Layer getLayer() {
        return TTParticleLayers.translucent(this.smokeSheet);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("crimson_smoke");

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new CrimsonSmokeParticle(level, x, y, z, vx, vy, vz, SHEET);
        }
    }
}
