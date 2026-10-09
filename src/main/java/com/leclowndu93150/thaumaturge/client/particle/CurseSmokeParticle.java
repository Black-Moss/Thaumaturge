package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class CurseSmokeParticle extends TTParticle {
    private static final int FRAME_COUNT = 4;
    private static final int LIFETIME = 8;
    private static final float FRICTION = 0.9F;
    private static final float RED_BASE = 0.41F;
    private static final float BLUE_BASE = 0.019F;
    private static final float COLOR_RANGE = 0.2F;
    private static final float SIZE_BASE = 2.0F;
    private static final float SIZE_RANGE = 4.0F;
    private static final float SIZE_UNIT = 0.1F;
    private static final int DELAY_RANGE = 4;
    private static final int FLICKER_PERIOD = 2;
    private static final int FADE_OUT_TICKS = 2;

    private float flickerFrom;
    private float flickerTo;

    private CurseSmokeParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(RED_BASE + this.random.nextFloat() * COLOR_RANGE, 0.0F, BLUE_BASE + this.random.nextFloat() * COLOR_RANGE);
        this.lifetime = LIFETIME;
        this.friction = FRICTION;
        this.quadSize = (SIZE_BASE + this.random.nextFloat() * SIZE_RANGE) * SIZE_UNIT;
        this.alpha = 0.0F;
        frame(this.random.nextInt(FRAME_COUNT));
        setSpin(this.random.nextFloat(), 0.0F);
        setDelay(this.random.nextInt(DELAY_RANGE));
        this.flickerTo = this.random.nextFloat();
    }

    @Override
    protected void update() {
        int phase = this.age % FLICKER_PERIOD;
        if (phase == 0) {
            this.flickerFrom = this.flickerTo;
            this.flickerTo = this.age >= this.lifetime - FADE_OUT_TICKS ? 0.0F : this.random.nextFloat();
        }
        this.alpha = Mth.lerp((float) phase / FLICKER_PERIOD, this.flickerFrom, this.flickerTo);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("curse_smoke");

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new CurseSmokeParticle(level, x, y, z, vx, vy, vz, SHEET);
        }
    }
}
