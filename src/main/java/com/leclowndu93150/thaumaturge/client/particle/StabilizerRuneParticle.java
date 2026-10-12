package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.StabilizerRuneParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class StabilizerRuneParticle extends TTParticle {
    private static final float RED = 0.42F;
    private static final float GREEN = 0.28F;
    private static final float BLUE = 0.58F;
    private static final float START_ALPHA = 0.3F;
    private static final float START_SIZE = 0.02F;
    private static final float END_SIZE = 1.0F;
    private static final float SPEED_UP = 1.015F;
    private static final float SPIN_BASE = 0.003F;
    private static final float SPIN_RANGE = 0.004F;

    private StabilizerRuneParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, StabilizerRuneParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(RED, GREEN, BLUE);
        this.alpha = START_ALPHA;
        this.friction = SPEED_UP;
        int life = Math.max(1, options.life());
        this.lifetime = life + this.random.nextInt(life + 1);
        this.quadSize = START_SIZE;
        frame(this.random.nextInt(sheetFrames()));
        float spin = SPIN_BASE + this.random.nextFloat() * SPIN_RANGE;
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? spin : -spin);
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
