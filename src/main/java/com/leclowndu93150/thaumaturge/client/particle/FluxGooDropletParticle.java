package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.FluxGooDropletParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public final class FluxGooDropletParticle extends BreakingItemParticle {
    private final float baseAlpha;

    private FluxGooDropletParticle(ClientLevel level, double x, double y, double z, double xa, double ya, double za, TextureAtlasSprite sprite, int color, float alpha, int lifetime) {
        super(level, x, y, z, xa, ya, za, sprite);
        this.baseAlpha = alpha;
        this.alpha = alpha;
        this.lifetime = lifetime;
        this.rCol = ((color >> 16) & 0xFF) / 255.0F;
        this.gCol = ((color >> 8) & 0xFF) / 255.0F;
        this.bCol = (color & 0xFF) / 255.0F;
        this.gravity = 1.0F;
    }

    @Override
    public void tick() {
        super.tick();
        if (lifetime > 0) {
            float t = (float) age / (float) lifetime;
            this.alpha = baseAlpha * Math.max(0.0F, 1.0F - t);
        }
    }

    public static final class Provider extends BreakingItemParticle.ItemParticleProvider<FluxGooDropletParticleOptions> implements ParticleProvider<FluxGooDropletParticleOptions> {
        private static final float RECIPROCAL_NUMERATOR = 66.0F;
        private static final float RECIPROCAL_SPREAD = 0.9F;
        private static final float RECIPROCAL_FLOOR = 0.1F;

        @Override
        public @Nullable Particle createParticle(FluxGooDropletParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            int lifetime = options.lifetime() > 0 ? options.lifetime() : skewedLifetime(random);
            return new FluxGooDropletParticle(level, x, y, z, vx, vy, vz, getSprite(new ItemStackTemplate(Items.SLIME_BALL), level, random), options.color(), options.alpha(), lifetime);
        }

        private static int skewedLifetime(RandomSource random) {
            return (int) (RECIPROCAL_NUMERATOR / (random.nextFloat() * RECIPROCAL_SPREAD + RECIPROCAL_FLOOR));
        }
    }
}
