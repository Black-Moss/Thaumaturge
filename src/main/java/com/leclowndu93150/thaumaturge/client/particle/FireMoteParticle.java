package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.FireMoteParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;

public final class FireMoteParticle extends TTParticle {
    private static final int LIFETIME = 16;
    private static final int EXTRA_AGE_ODDS = 6;
    private static final float BYTE_RANGE = 255.0F;
    private static final float SIZE_UNIT = 0.1F;
    private static final float SPIN_PER_TICK = 1.0F;
    private static final float START_ROLL = (float) (Math.PI * 2.0);

    private final ParticleSheet moteSheet;
    private final boolean translucent;
    private final float startAlpha;
    private final float startSize;

    private FireMoteParticle(ClientLevel level, double x, double y, double z, FireMoteParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, options.vx(), options.vy(), options.vz(), sheet);
        this.moteSheet = sheet;
        this.translucent = options.translucent();
        float divisor = Math.max(options.r(), Math.max(options.g(), options.b())) > 1.0F ? BYTE_RANGE : 1.0F;
        setColor(options.r() / divisor, options.g() / divisor, options.b() / divisor);
        this.startAlpha = options.alpha();
        this.alpha = this.startAlpha;
        this.startSize = options.scale() * SIZE_UNIT;
        this.quadSize = this.startSize;
        this.lifetime = LIFETIME;
        this.roll = START_ROLL;
        this.oRoll = START_ROLL;
    }

    @Override
    protected void update() {
        if (this.random.nextInt(EXTRA_AGE_ODDS) == 0) {
            this.age++;
        }
        this.roll += SPIN_PER_TICK;
        float remaining = 1.0F - progress();
        this.alpha = this.startAlpha * remaining;
        this.quadSize = this.startSize * remaining;
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    @Override
    public Layer getLayer() {
        return this.translucent ? TTParticleLayers.translucent(this.moteSheet) : TTParticleLayers.additive(this.moteSheet);
    }

    public static final class Provider implements ParticleProvider<FireMoteParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("fire_mote");

        @Override
        public Particle createParticle(FireMoteParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new FireMoteParticle(level, x, y, z, options, SHEET);
        }
    }
}
