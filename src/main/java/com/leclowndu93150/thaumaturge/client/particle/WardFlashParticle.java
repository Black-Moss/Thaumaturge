package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.WardFlashParticleOptions;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;

public final class WardFlashParticle extends TTParticle {
    private static final float CENTRE = 0.5F;
    private static final float JITTER = 0.1F;
    private static final float EDGE_LIMIT = 0.4F;
    private static final double NORMAL_NUDGE = 0.005;
    private static final int LIFETIME_BASE = 12;
    private static final int LIFETIME_RANGE = 5;
    private static final float SIZE_MEAN = 1.4F;
    private static final float SIZE_DEVIATION = 0.3F;
    private static final float HALF = 0.5F;
    private static final int DEGREES_RANGE = 360;
    private static final float RAMP_DIVISOR = 5.0F;
    private static final float ALPHA_SCALE = 0.5F;
    private static final int FRAME_COUNT = 16;
    private static final float HITBOX = 0.01F;

    private final Quaternionf orientation;

    private WardFlashParticle(ClientLevel level, double x, double y, double z, WardFlashParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        Direction face = options.face();
        double px = x + offset(options.hitX(), face.getStepX() != 0) + NORMAL_NUDGE * face.getStepX();
        double py = y + offset(options.hitY(), face.getStepY() != 0) + NORMAL_NUDGE * face.getStepY();
        double pz = z + offset(options.hitZ(), face.getStepZ() != 0) + NORMAL_NUDGE * face.getStepZ();
        setSize(HITBOX, HITBOX);
        setPos(px, py, pz);
        this.xo = px;
        this.yo = py;
        this.zo = pz;
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.quadSize = (SIZE_MEAN + (float) this.random.nextGaussian() * SIZE_DEVIATION) * HALF;
        this.alpha = 0.0F;
        this.orientation = new Quaternionf().rotateTo(0.0F, 0.0F, 1.0F, face.getStepX(), face.getStepY(), face.getStepZ()).rotateZ((float) Math.toRadians(this.random.nextInt(DEGREES_RANGE)));
    }

    private double offset(float hit, boolean onFaceAxis) {
        float centred = hit - CENTRE;
        if (onFaceAxis) {
            return centred;
        }
        float jitter = (this.random.nextFloat() * 2.0F - 1.0F) * JITTER;
        return Mth.clamp(centred + jitter, -EDGE_LIMIT, EDGE_LIMIT);
    }

    @Override
    protected void update() {
        float ramp = this.lifetime / RAMP_DIVISOR;
        float raw = this.age <= ramp ? this.age / ramp : (float) (this.lifetime - this.age) / this.lifetime;
        this.alpha = Mth.clamp(raw, 0.0F, 1.0F) * ALPHA_SCALE;
        frame(Math.min(FRAME_COUNT - 1, (int) (FRAME_COUNT * progress())));
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTick) {
        extractRotatedQuad(state, camera, this.orientation, partialTick);
    }

    public static final class Provider implements ParticleProvider<WardFlashParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("ward_flash");

        @Override
        public Particle createParticle(WardFlashParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new WardFlashParticle(level, x, y, z, options, SHEET);
        }
    }
}
