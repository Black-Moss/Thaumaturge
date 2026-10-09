package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.BoreDebrisParticleOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BoreDebrisParticle extends SeekerParticle {
    private static final float DRIFT_STRENGTH = 0.005F;
    private static final float GREY = 0.6F;
    private static final float SIZE_BASE = 0.4F;
    private static final float SIZE_RANGE = 0.3F;
    private static final float SIZE_UNIT = 0.1F;
    private static final float WINDOW_STEPS = 4.0F;
    private static final float OFFSET_RANGE = 3.0F;

    private final float windowU;
    private final float windowV;
    private final Layer layer;

    private BoreDebrisParticle(ClientLevel level, double x, double y, double z, BoreDebrisParticleOptions options, TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite, options.targetEntityId(), new Vec3(options.tx(), options.ty(), options.tz()), new Vec3(options.sx(), options.sy(), options.sz()), DRIFT_STRENGTH);
        setColor(GREY, GREY, GREY);
        this.alpha = 1.0F;
        this.quadSize = (SIZE_BASE + this.random.nextFloat() * SIZE_RANGE) * SIZE_UNIT;
        this.windowU = this.random.nextFloat() * OFFSET_RANGE;
        this.windowV = this.random.nextFloat() * OFFSET_RANGE;
        this.layer = Layer.bySprite(sprite);
    }

    @Override
    protected void update() {}

    @Override
    public Layer getLayer() {
        return this.layer;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU((this.windowU + 1.0F) / WINDOW_STEPS);
    }

    @Override
    protected float getU1() {
        return this.sprite.getU(this.windowU / WINDOW_STEPS);
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(this.windowV / WINDOW_STEPS);
    }

    @Override
    protected float getV1() {
        return this.sprite.getV((this.windowV + 1.0F) / WINDOW_STEPS);
    }

    public static final class Provider implements ParticleProvider<BoreDebrisParticleOptions> {
        @Override
        public @Nullable Particle createParticle(BoreDebrisParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            BlockState state = options.state();
            if (state.isAir()) {
                return null;
            }
            TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite();
            return new BoreDebrisParticle(level, x, y, z, options, sprite);
        }
    }
}
