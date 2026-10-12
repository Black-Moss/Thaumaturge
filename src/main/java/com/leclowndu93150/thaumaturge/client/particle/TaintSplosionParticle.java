package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

public final class TaintSplosionParticle extends BreakingItemParticle {
    private static final float FAINT_RED = 0.78F;
    private static final float FAINT_GREEN = 0.32F;
    private static final float FAINT_BLUE = 0.78F;
    private static final float FAINT_ALPHA = 0.5F;
    private static final float STRONG_RED = 0.32F;
    private static final float STRONG_GREEN = 0.08F;
    private static final float STRONG_BLUE = 0.45F;
    private static final float STRONG_ALPHA = 0.9F;
    private static final double SPEED_BASE = 0.12;
    private static final double SPEED_RANGE = 0.18;
    private static final double UP_KICK = 0.12;
    private static final double MIN_DIRECTION = 1.0E-4;
    private static final int LIFETIME_BASE = 40;
    private static final int LIFETIME_SPREAD = 560;
    private static final int LIFETIME_SKEW = 5;

    private final float startAlpha;
    private final SingleQuadParticle.Layer translucentLayer;

    private TaintSplosionParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);
        if (this.random.nextBoolean()) {
            setColor(FAINT_RED, FAINT_GREEN, FAINT_BLUE);
            this.startAlpha = FAINT_ALPHA;
        } else {
            setColor(STRONG_RED, STRONG_GREEN, STRONG_BLUE);
            this.startAlpha = STRONG_ALPHA;
        }
        this.alpha = this.startAlpha;
        launch(dx, dy, dz);
        this.lifetime = LIFETIME_BASE + (int) (LIFETIME_SPREAD * Math.pow(this.random.nextDouble(), LIFETIME_SKEW));
        this.translucentLayer = sprite.atlasLocation().equals(TextureAtlas.LOCATION_BLOCKS) ? SingleQuadParticle.Layer.TRANSLUCENT_TERRAIN : SingleQuadParticle.Layer.TRANSLUCENT_ITEMS;
    }

    private void launch(double dx, double dy, double dz) {
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double ux = length < MIN_DIRECTION ? 0.0 : dx / length;
        double uy = length < MIN_DIRECTION ? 1.0 : dy / length;
        double uz = length < MIN_DIRECTION ? 0.0 : dz / length;
        double speed = SPEED_BASE + this.random.nextDouble() * SPEED_RANGE;
        this.xd = ux * speed;
        this.yd = uy * speed + UP_KICK;
        this.zd = uz * speed;
    }

    @Override
    public void tick() {
        super.tick();
        this.alpha = this.startAlpha * (1.0F - Mth.clamp((float) this.age / this.lifetime, 0.0F, 1.0F));
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return this.translucentLayer;
    }

    public static final class Provider extends BreakingItemParticle.ItemParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new TaintSplosionParticle(level, x, y, z, vx, vy, vz, getSprite(new ItemStackTemplate(Items.SLIME_BALL), level, random));
        }
    }
}
