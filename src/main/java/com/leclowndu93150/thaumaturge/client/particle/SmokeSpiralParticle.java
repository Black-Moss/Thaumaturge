package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.SmokeSpiralParticleOptions;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public final class SmokeSpiralParticle extends TTParticle {
    private static final int LIFETIME_BASE = 20;
    private static final int LIFETIME_RANGE = 10;
    private static final float SIZE = 0.15F;
    private static final float HITBOX = 0.01F;
    private static final float GRAVITY = -0.01F;
    private static final float ALPHA_SCALE = 0.66F;
    private static final int FRAME_STEPS = 4;
    private static final double TURNS = 2.0;
    private static final double FLOOR_OFFSET = 0.1;

    private final float radius;
    private final double startAzimuth;
    private final int floorY;

    private SmokeSpiralParticle(ClientLevel level, double x, double y, double z, SmokeSpiralParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        setColor(options.r(), options.g(), options.b());
        this.radius = options.radius();
        this.startAzimuth = Math.toRadians(options.start());
        this.floorY = options.minY();
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.quadSize = SIZE;
        this.gravity = GRAVITY;
        setSize(HITBOX, HITBOX);
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = ALPHA_SCALE * (1.0F - t);
        frame((int) (t * FRAME_STEPS));
    }

    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTick) {
        float t = Mth.clamp((this.age - 1 + partialTick) / this.lifetime, 0.0F, 1.0F);
        double polar = Math.PI * t;
        double azimuth = this.startAzimuth + TURNS * Math.PI * 2.0 * t;
        double ring = this.radius * Math.sin(polar);
        double centreX = Mth.lerp((double) partialTick, this.xo, this.x);
        double centreY = Mth.lerp((double) partialTick, this.yo, this.y);
        double centreZ = Mth.lerp((double) partialTick, this.zo, this.z);
        double drawX = centreX + ring * Math.cos(azimuth);
        double drawY = Math.max(centreY - this.radius * Math.cos(polar), this.floorY + FLOOR_OFFSET);
        double drawZ = centreZ + ring * Math.sin(azimuth);
        Vec3 cameraPos = camera.position();
        Quaternionf rotation = new Quaternionf();
        getFacingCameraMode().setRotation(rotation, camera, partialTick);
        extractRotatedQuad(state, rotation, (float) (drawX - cameraPos.x()), (float) (drawY - cameraPos.y()), (float) (drawZ - cameraPos.z()), partialTick);
    }

    public static final class Provider implements ParticleProvider<SmokeSpiralParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("smoke_spiral");

        @Override
        public Particle createParticle(SmokeSpiralParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new SmokeSpiralParticle(level, x, y, z, options, SHEET);
        }
    }
}
