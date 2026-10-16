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
    private static final double EDGE_MARGIN = 0.1;
    private static final double HIT_NUDGE = 0.1;
    private static final double SURFACE_OFFSET = 0.005;
    private static final int LIFETIME_BASE = 12;
    private static final int LIFETIME_RANGE = 5;
    private static final float SIZE_BASE = 0.7F;
    private static final float SIZE_SPREAD = 0.15F;
    private static final float SIZE_MIN = 0.2F;
    private static final float SIZE_MAX = 1.2F;
    private static final float PEAK_ALPHA = 0.5F;
    private static final float RISE_SHARE = 0.2F;
    private static final float QUAD_FACING_PITCH = (float) (-Math.PI / 2.0);

    private final Quaternionf orientation;

    private WardFlashParticle(ClientLevel level, double x, double y, double z, Direction face, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.quadSize = Mth.clamp(SIZE_BASE + (float) this.random.nextGaussian() * SIZE_SPREAD, SIZE_MIN, SIZE_MAX);
        this.alpha = 0.0F;
        this.orientation = new Quaternionf(face.getRotation()).rotateX(QUAD_FACING_PITCH).rotateZ(this.random.nextFloat() * Mth.TWO_PI);
        frame(0);
    }

    @Override
    protected void update() {
        float t = progress();
        frame(Math.min(sheetFrames() - 1, (int) (sheetFrames() * t)));
        float threshold = this.lifetime * RISE_SHARE;
        this.alpha = this.age <= threshold ? PEAK_ALPHA * this.age / threshold : PEAK_ALPHA * (this.lifetime - this.age) / this.lifetime;
    }

    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTick) {
        extractRotatedQuad(state, camera, this.orientation, partialTick);
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    private static double placeAcross(double hit, RandomSource random) {
        return Mth.clamp(hit + (random.nextDouble() * 2.0 - 1.0) * HIT_NUDGE, EDGE_MARGIN, 1.0 - EDGE_MARGIN);
    }

    private static double placeAlong(int step, double hit, RandomSource random) {
        if (step == 0) {
            return placeAcross(hit, random);
        }
        return step > 0 ? 1.0 + SURFACE_OFFSET : -SURFACE_OFFSET;
    }

    public static final class Provider implements ParticleProvider<WardFlashParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("ward_flash");

        @Override
        public Particle createParticle(WardFlashParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            Direction face = options.face();
            int originX = Mth.floor(x);
            int originY = Mth.floor(y);
            int originZ = Mth.floor(z);
            double px = originX + placeAlong(face.getStepX(), options.hitX(), random);
            double py = originY + placeAlong(face.getStepY(), options.hitY(), random);
            double pz = originZ + placeAlong(face.getStepZ(), options.hitZ(), random);
            return new WardFlashParticle(level, px, py, pz, face, SHEET);
        }
    }
}
