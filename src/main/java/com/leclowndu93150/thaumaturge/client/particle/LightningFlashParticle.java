package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.LightningFlashParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class LightningFlashParticle extends TTParticle {
    private static final int FRAME_COUNT = 4;
    private static final int LIFETIME_BASE = 5;
    private static final int LIFETIME_RANGE = 5;
    private static final float SIZE_UNIT = 0.1F;

    private final float peakAlpha;

    private LightningFlashParticle(ParticleSheet sheet, ClientLevel level, Vec3 origin, LightningFlashParticleOptions options) {
        super(level, origin.x, origin.y, origin.z, 0.0, 0.0, 0.0, sheet);
        setColor(options.color());
        this.peakAlpha = options.alpha();
        this.alpha = this.peakAlpha;
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.quadSize = options.scale() * SIZE_UNIT;
        frame(this.random.nextInt(FRAME_COUNT));
        setSpin(this.random.nextFloat(), 0.0F);
    }

    @Override
    protected void update() {
        this.alpha = this.peakAlpha * (1.0F - progress());
    }

    public static final class Provider implements ParticleProvider<LightningFlashParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("lightning_flash");

        @Override
        public Particle createParticle(LightningFlashParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new LightningFlashParticle(SHEET, level, new Vec3(x, y, z), options);
        }
    }
}
