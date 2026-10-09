package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class TaintSplosionParticle extends SingleQuadParticle {
    private static final float VARIANT_A_RED = 0.6F;
    private static final float VARIANT_A_BLUE = 0.3F;
    private static final float VARIANT_A_ALPHA = 0.4F;
    private static final float VARIANT_B_RED = 0.3F;
    private static final float VARIANT_B_BLUE = 0.3F;
    private static final float VARIANT_B_ALPHA = 0.6F;
    private static final float SIZE_BASE = 0.2F;
    private static final float SIZE_ROLL_MIN = 0.25F;
    private static final float SIZE_ROLL_SPAN = 0.25F;
    private static final float LIFETIME_NUMERATOR = 66.0F;
    private static final float LIFETIME_DIVISOR_MIN = 0.1F;
    private static final float LIFETIME_DIVISOR_SPAN = 0.9F;
    private static final float GRAVITY_FACTOR = 1.0F;
    private static final double MIN_DIRECTION_LENGTH = 1.0E-4;
    private static final double STRENGTH_STEP = 0.15;
    private static final double LAUNCH_DAMPING = 0.964;
    private static final double LAUNCH_LIFT = 0.1;
    private static final float CROP_GRID = 4.0F;
    private static final float CROP_ORIGIN_RANGE = 3.0F;

    private final float baseAlpha;
    private final float cropU;
    private final float cropV;
    private final SingleQuadParticle.Layer layer;

    private TaintSplosionParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);
        boolean variantA = this.random.nextBoolean();
        this.baseAlpha = variantA ? VARIANT_A_ALPHA : VARIANT_B_ALPHA;
        setColor(variantA ? VARIANT_A_RED : VARIANT_B_RED, 0.0F, variantA ? VARIANT_A_BLUE : VARIANT_B_BLUE);
        this.alpha = this.baseAlpha;
        this.quadSize = SIZE_BASE * (SIZE_ROLL_MIN + this.random.nextFloat() * SIZE_ROLL_SPAN);
        this.lifetime = (int) (LIFETIME_NUMERATOR / (LIFETIME_DIVISOR_MIN + this.random.nextFloat() * LIFETIME_DIVISOR_SPAN));
        this.gravity = GRAVITY_FACTOR;
        this.cropU = this.random.nextFloat() * CROP_ORIGIN_RANGE;
        this.cropV = this.random.nextFloat() * CROP_ORIGIN_RANGE;
        this.layer = SingleQuadParticle.Layer.bySprite(sprite);
        launch(dx, dy, dz);
    }

    private void launch(double dx, double dy, double dz) {
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < MIN_DIRECTION_LENGTH) {
            length = 1.0;
        }
        double strength = (this.random.nextFloat() + this.random.nextFloat() + 1.0F) * STRENGTH_STEP;
        double scale = strength * LAUNCH_DAMPING / length;
        this.xd = dx * scale;
        this.yd = dy * scale + LAUNCH_LIFT;
        this.zd = dz * scale;
    }

    @Override
    public void tick() {
        super.tick();
        this.alpha = this.baseAlpha * Math.max(0.0F, 1.0F - (float) this.age / this.lifetime);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return this.layer;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU((this.cropU + 1.0F) / CROP_GRID);
    }

    @Override
    protected float getU1() {
        return this.sprite.getU(this.cropU / CROP_GRID);
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(this.cropV / CROP_GRID);
    }

    @Override
    protected float getV1() {
        return this.sprite.getV((this.cropV + 1.0F) / CROP_GRID);
    }

    @Override
    public float getQuadSize(float partialTick) {
        return this.quadSize;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final ItemStackRenderState scratch = new ItemStackRenderState();

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new TaintSplosionParticle(level, x, y, z, vx, vy, vz, resolveSprite(level, random));
        }

        private TextureAtlasSprite resolveSprite(ClientLevel level, RandomSource random) {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.getItemModelResolver().updateForTopItem(this.scratch, new ItemStack(Items.SLIME_BALL), ItemDisplayContext.GROUND, level, null, 0);
            Material.Baked material = this.scratch.pickParticleMaterial(random);
            return material != null ? material.sprite() : minecraft.getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS).missingSprite();
        }
    }
}
