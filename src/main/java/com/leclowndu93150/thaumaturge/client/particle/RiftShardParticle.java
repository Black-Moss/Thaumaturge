package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.RiftShardParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class RiftShardParticle extends TTParticle {
    private static final float RED = 0.25F;
    private static final float GREEN = 0.25F;
    private static final float BLUE = 1.0F;
    private static final float SIZE_UNIT = 0.1F;
    private static final int LIFETIME_BASE = 16;
    private static final int LIFETIME_RANGE = 16;
    private static final float DRAG = 0.75F;
    private static final double WOBBLE = 0.01;

    private RiftShardParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RiftShardParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(RED, GREEN, BLUE);
        this.alpha = 1.0F;
        this.friction = DRAG;
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.quadSize = options.scale() * SIZE_UNIT;
        frame(this.random.nextInt(sheetFrames()));
    }

    @Override
    protected void update() {
        jitterVelocity(WOBBLE);
        this.alpha = 1.0F - progress();
    }

    public static final class Provider implements ParticleProvider<RiftShardParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("rift_shard");

        @Override
        public Particle createParticle(RiftShardParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new RiftShardParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
