package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.client.particle.support.BoltLineShape;
import com.leclowndu93150.thaumaturge.content.particle.BoltParticleOptions;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public final class BoltParticle extends TTParticle {
    private static final int LIFETIME = 3;
    private static final int BEADS_PER_SECTION = 4;
    private static final int SEED_RANGE = 1000;
    private static final int PHASE_MULTIPLES = 8;
    private static final int FRAME_COUNT = 16;
    private static final float WIDTH_DIVISOR = 6.0F;
    private static final float SWAY_TIME_DIVISOR = 10.0F;
    private static final float MIN_ALPHA = 0.1F;
    private static final float HITBOX = 0.01F;

    private final float width;
    private final int seed;
    private final BoltLineShape shape;
    private final Quaternionf rotation = new Quaternionf();

    private BoltParticle(ClientLevel level, double x, double y, double z, BoltParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        this.width = options.width();
        setColor(options.r(), options.g(), options.b());
        this.lifetime = LIFETIME;
        setSize(HITBOX, HITBOX);
        this.seed = this.random.nextInt(SEED_RANGE);
        double phase = this.random.nextInt(PHASE_MULTIPLES) * Math.PI;
        this.shape = new BoltLineShape(x, y, z, options.targetX(), options.targetY(), options.targetZ(), this.seed, phase);
        if (this.shape.isEmpty()) {
            remove();
        }
        frame(this.seed % FRAME_COUNT);
    }

    @Override
    protected void update() {
        frame((this.age + this.seed) % FRAME_COUNT);
        this.shape.refreshJitter(this.age);
    }

    @Override
    public float getQuadSize(float partialTick) {
        return this.width / WIDTH_DIVISOR;
    }

    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTick) {
        if (this.removed || this.shape.isEmpty()) {
            return;
        }
        float time = this.age + partialTick;
        double amplitude = time / SWAY_TIME_DIVISOR;
        this.rotation.set(camera.rotation());
        Vec3 cameraPos = camera.position();
        float alpha = Mth.clamp(1.0F - time / this.lifetime, MIN_ALPHA, 1.0F);
        int color = ARGB.colorFromFloat(alpha, this.rCol, this.gCol, this.bCol);
        float size = getQuadSize(partialTick);
        float u0 = getU0();
        float u1 = getU1();
        float v0 = getV0();
        float v1 = getV1();
        int sections = this.shape.sections();
        for (int section = 0; section < sections; section++) {
            for (int bead = 0; bead < BEADS_PER_SECTION; bead++) {
                double f = (double) bead / BEADS_PER_SECTION;
                float bx = (float) (beadCoordinate(section, f, 0, amplitude) - cameraPos.x());
                float by = (float) (beadCoordinate(section, f, 1, amplitude) - cameraPos.y());
                float bz = (float) (beadCoordinate(section, f, 2, amplitude) - cameraPos.z());
                state.add(getLayer(), bx, by, bz, this.rotation.x, this.rotation.y, this.rotation.z, this.rotation.w, size, u0, u1, v0, v1, color, LightCoordsUtil.FULL_BRIGHT);
            }
        }
    }

    private double beadCoordinate(int section, double fraction, int axis, double amplitude) {
        return Mth.lerp(fraction, this.shape.coordinate(section, axis, amplitude), this.shape.coordinate(section + 1, axis, amplitude));
    }

    public static final class Provider implements ParticleProvider<BoltParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("bolt");

        @Override
        public Particle createParticle(BoltParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new BoltParticle(level, x, y, z, options, SHEET);
        }
    }
}
