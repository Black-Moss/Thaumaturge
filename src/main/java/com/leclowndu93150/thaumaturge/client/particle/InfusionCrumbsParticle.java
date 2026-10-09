package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.InfusionCrumbsParticleOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class InfusionCrumbsParticle extends SeekerParticle {
    private static final float DRIFT_STRENGTH = 0.005F;
    private static final float GREY = 0.6F;
    private static final float ALPHA = 0.3F;
    private static final float SIZE_BASE = 0.4F;
    private static final float SIZE_RANGE = 0.3F;
    private static final float SIZE_UNIT = 0.1F;
    private static final float WINDOW_FRACTION = 0.25F;

    private final float windowU;
    private final float windowV;
    private final Layer layer;

    private InfusionCrumbsParticle(ClientLevel level, double x, double y, double z, InfusionCrumbsParticleOptions options, TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite, NO_ENTITY, new Vec3(options.tx(), options.ty(), options.tz()), new Vec3(options.sx(), options.sy(), options.sz()), DRIFT_STRENGTH);
        setColor(GREY, GREY, GREY);
        this.alpha = ALPHA;
        this.quadSize = (SIZE_BASE + this.random.nextFloat() * SIZE_RANGE) * SIZE_UNIT;
        this.windowU = this.random.nextFloat() * (1.0F - WINDOW_FRACTION);
        this.windowV = this.random.nextFloat() * (1.0F - WINDOW_FRACTION);
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
        return this.sprite.getU(this.windowU + WINDOW_FRACTION);
    }

    @Override
    protected float getU1() {
        return this.sprite.getU(this.windowU);
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(this.windowV);
    }

    @Override
    protected float getV1() {
        return this.sprite.getV(this.windowV + WINDOW_FRACTION);
    }

    public static final class Provider implements ParticleProvider<InfusionCrumbsParticleOptions> {
        private final ItemStackRenderState scratchRenderState = new ItemStackRenderState();

        @Override
        public @Nullable Particle createParticle(InfusionCrumbsParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            ItemStack stack = options.stack().create();
            if (stack.isEmpty()) {
                return null;
            }
            return new InfusionCrumbsParticle(level, x, y, z, options, resolveSprite(stack, level, random));
        }

        private TextureAtlasSprite resolveSprite(ItemStack stack, ClientLevel level, RandomSource random) {
            Minecraft minecraft = Minecraft.getInstance();
            if (stack.getItem() instanceof BlockItem blockItem) {
                return minecraft.getModelManager().getBlockStateModelSet().getParticleMaterial(blockItem.getBlock().defaultBlockState()).sprite();
            }
            minecraft.getItemModelResolver().updateForTopItem(this.scratchRenderState, stack, ItemDisplayContext.GROUND, level, null, 0);
            Material.Baked material = this.scratchRenderState.pickParticleMaterial(random);
            return material != null ? material.sprite() : minecraft.getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS).missingSprite();
        }
    }
}
