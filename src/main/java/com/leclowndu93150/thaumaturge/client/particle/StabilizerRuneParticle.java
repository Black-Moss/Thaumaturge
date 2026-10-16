package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.StabilizerRuneParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class StabilizerRuneParticle extends TTParticle {
    private static final float RED = 0.5F;
    private static final float GREEN = 0.2F;
    private static final float BLUE = 0.5F;
    private static final float START_ALPHA = 0.3F;
    private static final float START_SIZE = 0.1F;
    private static final float END_SIZE = 1.0F;
    private static final float SPEED_UP = 1.01F;
    private static final float SPIN_SPEED = 1.0F;

    private StabilizerRuneParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, StabilizerRuneParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(RED, GREEN, BLUE);
        this.alpha = START_ALPHA;
        this.friction = SPEED_UP;
        int life = Math.max(1, options.life());
        this.lifetime = life + this.random.nextInt(life);
        this.quadSize = START_SIZE;
        frame(this.random.nextInt(sheetFrames()));
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? SPIN_SPEED : -SPIN_SPEED);
    }

    @Override
    protected void update() {
        float t = progress();
        this.quadSize = Mth.lerp(t, START_SIZE, END_SIZE);
        this.alpha = START_ALPHA * (1.0F - t);
    }

    public static final class Provider implements ParticleProvider<StabilizerRuneParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("stabilizer_rune");

        @Override
        public Particle createParticle(StabilizerRuneParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new StabilizerRuneParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
