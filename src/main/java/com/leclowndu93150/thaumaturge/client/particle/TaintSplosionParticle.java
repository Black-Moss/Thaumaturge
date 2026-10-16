package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

public final class TaintSplosionParticle extends BreakingItemParticle {
    private static final float FAINT_RED = 0.6F;
    private static final float FAINT_GREEN = 0.0F;
    private static final float FAINT_BLUE = 0.3F;
    private static final float FAINT_ALPHA = 0.4F;
    private static final float STRONG_RED = 0.3F;
    private static final float STRONG_GREEN = 0.0F;
    private static final float STRONG_BLUE = 0.3F;
    private static final float STRONG_ALPHA = 0.6F;
    private static final double SPEED_UNIT = 0.15;
    private static final double SPEED_FACTOR = 0.964;
    private static final double UP_KICK = 0.1;
    private static final double MIN_DIRECTION = 1.0E-4;
    private static final double LIFETIME_NUMERATOR = 66.0;
    private static final double LIFETIME_DIVISOR_MIN = 0.1;
    private static final double LIFETIME_DIVISOR_RANGE = 0.9;

    private final float startAlpha;

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
        this.lifetime = (int) (LIFETIME_NUMERATOR / (LIFETIME_DIVISOR_MIN + this.random.nextDouble() * LIFETIME_DIVISOR_RANGE));
    }

    private void launch(double dx, double dy, double dz) {
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double ux = length < MIN_DIRECTION ? 0.0 : dx / length;
        double uy = length < MIN_DIRECTION ? 0.0 : dy / length;
        double uz = length < MIN_DIRECTION ? 0.0 : dz / length;
        double speed = (this.random.nextDouble() + this.random.nextDouble() + 1.0) * SPEED_UNIT * SPEED_FACTOR;
        this.xd = ux * speed;
        this.yd = uy * speed + UP_KICK;
        this.zd = uz * speed;
    }

    @Override
    public void tick() {
        super.tick();
        this.alpha = this.startAlpha * (1.0F - Mth.clamp((float) this.age / this.lifetime, 0.0F, 1.0F));
    }

    public static final class Provider extends BreakingItemParticle.ItemParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new TaintSplosionParticle(level, x, y, z, vx, vy, vz, getSprite(new ItemStackTemplate(Items.SLIME_BALL), level, random));
        }
    }
}
