package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.BlockRunesParticleOptions;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;

public final class BlockRunesParticle extends TTParticle {
    private static final int FRAME_COUNT = 16;
    private static final int SIDE_COUNT = 4;
    private static final float QUARTER_TURN = (float) (Math.PI / 2.0);
    private static final int LIFETIME_FACTOR = 3;
    private static final float PEAK_ALPHA = 0.5F;
    private static final float PEAK_PROGRESS = 0.2F;
    private static final float BASE_SIZE = 0.3F;
    private static final float NORMAL_SIZE_MEAN = 1.0F;
    private static final float VARIANT_SIZE_MEAN = 0.5F;
    private static final float SIZE_DEVIATION = 0.1F;
    private static final float FACE_DISTANCE = 0.51F;
    private static final float SIDE_RANGE = 0.3F;
    private static final float LIFT_RANGE = 0.2F;
    private static final float HITBOX = 0.01F;

    private final Quaternionf orientation;
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;

    private BlockRunesParticle(ClientLevel level, double x, double y, double z, BlockRunesParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        float red = options.r() == 0.0F ? 1.0F : options.r();
        setColor(red, options.g(), options.b());
        this.lifetime = LIFETIME_FACTOR * Math.max(1, options.duration());
        this.gravity = options.gravity();
        this.alpha = 0.0F;
        setSize(HITBOX, HITBOX);
        float sizeMean = options.variant() ? VARIANT_SIZE_MEAN : NORMAL_SIZE_MEAN;
        this.quadSize = BASE_SIZE * (sizeMean + (float) this.random.nextGaussian() * SIZE_DEVIATION);
        frame(this.random.nextInt(FRAME_COUNT));
        float angle = this.random.nextInt(SIDE_COUNT) * QUARTER_TURN;
        float sideways = (this.random.nextFloat() * 2.0F - 1.0F) * SIDE_RANGE;
        float lift = options.variant() ? 0.0F : this.random.nextFloat() * LIFT_RANGE;
        float sin = (float) Math.sin(angle);
        float cos = (float) Math.cos(angle);
        this.orientation = new Quaternionf().rotationY(angle);
        this.offsetX = sin * FACE_DISTANCE + cos * sideways;
        this.offsetY = lift;
        this.offsetZ = cos * FACE_DISTANCE - sin * sideways;
    }

    @Override
    protected void update() {
        float t = progress();
        float rising = PEAK_ALPHA * t / PEAK_PROGRESS;
        float falling = PEAK_ALPHA * (1.0F - t) / (1.0F - PEAK_PROGRESS);
        this.alpha = t < PEAK_PROGRESS ? rising : falling;
    }

    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTick) {
        extractRotatedQuad(state, camera, this.orientation, partialTick);
    }

    @Override
    protected void extractRotatedQuad(QuadParticleRenderState state, Quaternionf rotation, float x, float y, float z, float partialTick) {
        super.extractRotatedQuad(state, rotation, x + this.offsetX, y + this.offsetY, z + this.offsetZ, partialTick);
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    public static final class Provider implements ParticleProvider<BlockRunesParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("block_runes");

        @Override
        public Particle createParticle(BlockRunesParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new BlockRunesParticle(level, x, y, z, options, SHEET);
        }
    }
}
