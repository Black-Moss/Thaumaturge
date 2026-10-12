package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.SmokeSpiralParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class SmokeSpiralParticle extends TTParticle {
    private static final int LIFETIME_BASE = 20;
    private static final int LIFETIME_RANGE = 10;
    private static final float SIZE = 0.16F;
    private static final float START_ALPHA = 0.6F;
    private static final double TURNS = 2.0;
    private static final double FLOOR_CLEARANCE = 0.15;

    private final double centreX;
    private final double centreY;
    private final double centreZ;
    private final double radius;
    private final double startAngle;
    private final double floorY;

    private SmokeSpiralParticle(ClientLevel level, double x, double y, double z, SmokeSpiralParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        this.centreX = x;
        this.centreY = y;
        this.centreZ = z;
        this.radius = options.radius();
        this.startAngle = Math.toRadians(options.start());
        this.floorY = options.minY() + FLOOR_CLEARANCE;
        setColor(Mth.clamp(options.r(), 0.0F, 1.0F), Mth.clamp(options.g(), 0.0F, 1.0F), Mth.clamp(options.b(), 0.0F, 1.0F));
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.quadSize = SIZE;
        this.alpha = START_ALPHA;
        placeOnPath(0.0F);
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    private void placeOnPath(float t) {
        double climb = Math.PI * t;
        double ring = this.radius * Math.sin(climb);
        double heading = this.startAngle + TURNS * Mth.TWO_PI * t;
        double px = this.centreX + ring * Math.cos(heading);
        double py = Math.max(this.floorY, this.centreY - this.radius * Math.cos(climb));
        double pz = this.centreZ + ring * Math.sin(heading);
        setPos(px, py, pz);
    }

    @Override
    protected void update() {
        float t = progress();
        placeOnPath(t);
        frameByProgress();
        this.alpha = START_ALPHA * (1.0F - t);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TTParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<SmokeSpiralParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("smoke_spiral");

        @Override
        public Particle createParticle(SmokeSpiralParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new SmokeSpiralParticle(level, x, y, z, options, SHEET);
        }
    }
}
