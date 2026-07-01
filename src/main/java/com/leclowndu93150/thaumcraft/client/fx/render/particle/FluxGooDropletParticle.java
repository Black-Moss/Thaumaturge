package com.leclowndu93150.thaumcraft.client.fx.render.particle;

import com.leclowndu93150.thaumcraft.content.fx.data.FluxGooDropletData;
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

    private FluxGooDropletParticle(ClientLevel level, double x, double y, double z, double xa, double ya, double za,
                                   TextureAtlasSprite sprite, int color, float alpha, int lifetime) {
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

    public static final class Provider extends BreakingItemParticle.ItemParticleProvider<FluxGooDropletData>
            implements ParticleProvider<FluxGooDropletData> {
        @Nullable
        @Override
        public Particle createParticle(FluxGooDropletData options, ClientLevel level,
                                       double x, double y, double z,
                                       double xa, double ya, double za,
                                       RandomSource random) {
            TextureAtlasSprite sprite = this.getSprite(
                    new ItemStackTemplate(Items.SLIME_BALL), level, random);
            int lifetime = options.lifetime() > 0
                    ? options.lifetime()
                    : (int) (66.0F / (random.nextFloat() * 0.9F + 0.1F));
            return new FluxGooDropletParticle(level, x, y, z, xa, ya, za,
                    sprite, options.color(), options.alpha(), lifetime);
        }
    }
}
