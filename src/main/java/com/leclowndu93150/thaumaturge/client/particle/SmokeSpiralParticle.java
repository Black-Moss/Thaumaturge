package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.SmokeSpiralParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;

public final class SmokeSpiralParticle extends TTParticle {
    private static final int LIFETIME_BASE = 20;
    private static final int LIFETIME_RANGE = 10;
    private static final float SIZE = 0.15F;
    private static final float COLLISION_SIZE = 0.01F;
    private static final float ALPHA_SCALE = 0.66F;
    private static final float GRAVITY = -0.01F;
    private static final int ACTIVE_FRAMES = 4;
    private static final double TURNS = 2.0;
    private static final double FLOOR_CLEARANCE = 0.1;

    private final double radius;
    private final double startAngle;
    private final double floorY;

    private SmokeSpiralParticle(ClientLevel level, double x, double y, double z, SmokeSpiralParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        this.radius = options.radius();
        this.startAngle = Math.toRadians(options.start());
        this.floorY = options.minY() + FLOOR_CLEARANCE;
        this.gravity = GRAVITY;
        setSize(COLLISION_SIZE, COLLISION_SIZE);
        setColor(Mth.clamp(options.r(), 0.0F, 1.0F), Mth.clamp(options.g(), 0.0F, 1.0F), Mth.clamp(options.b(), 0.0F, 1.0F));
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.quadSize = SIZE;
        this.alpha = ALPHA_SCALE;
    }

    @Override
    protected void update() {
        float t = progress();
        frame(Math.min((int) (t * ACTIVE_FRAMES), ACTIVE_FRAMES - 1));
        this.alpha = ALPHA_SCALE * (1.0F - t);
    }

    @Override
    protected void extractRotatedQuad(QuadParticleRenderState state, Quaternionf rotation, float x, float y, float z, float partialTick) {
        float time = Mth.clamp((this.age + partialTick) / this.lifetime, 0.0F, 1.0F);
        double climb = Math.PI * time;
        double ring = this.radius * Math.sin(climb);
        double azimuth = this.startAngle + TURNS * Mth.TWO_PI * time;
        double centreY = Mth.lerp(partialTick, this.yo, this.y);
        double offsetY = -this.radius * Math.cos(climb);
        double clampedOffsetY = Math.max(this.floorY, centreY + offsetY) - centreY;
        super.extractRotatedQuad(state, rotation, x - (float) (Math.sin(azimuth) * ring), y + (float) clampedOffsetY, z + (float) (Math.cos(azimuth) * ring), partialTick);
    }

    public static final class Provider implements ParticleProvider<SmokeSpiralParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("smoke_spiral");

        @Override
        public Particle createParticle(SmokeSpiralParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new SmokeSpiralParticle(level, x, y, z, options, SHEET);
        }
    }
}
