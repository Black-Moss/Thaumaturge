package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.client.particle.support.ParticleCulling;
import com.leclowndu93150.thaumaturge.content.particle.VentParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

public final class VentParticle extends TTParticle {
    private static final int FIRST_SMOKE_FRAME = 1;
    private static final int SAFETY_LIFETIME = 80;
    private static final float SIZE_UNIT = 0.3F;
    private static final float START_FRACTION_BASE = 0.05F;
    private static final float START_FRACTION_RANGE = 0.1F;
    private static final float GROWTH = 1.15F;
    private static final float VARIANT_GROWTH = 1.2F;
    private static final float START_ALPHA = 0.4F;
    private static final int FRAME_SPAN = 4;
    private static final double LAUNCH_SPEED = 0.125;
    private static final double SCATTER = 0.0375;
    private static final float DRAG = 0.85F;
    private static final double RISE = 0.0025;
    private static final double VARIANT_DRIFT = 0.0075;
    private static final float VARIANT_SHADE = 0.05F;
    private static final double MIN_DIRECTION = 1.0E-6;
    private static final float HITBOX = 0.02F;
    private static final float MIN_FULL_SIZE = 0.05F;

    private final float fullSize;
    private final float growth;
    private final double verticalDrift;
    private float size;

    private VentParticle(ClientLevel level, double x, double y, double z, VentParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        this.fullSize = Math.max(MIN_FULL_SIZE, options.scale() * SIZE_UNIT);
        this.size = this.fullSize * (START_FRACTION_BASE + this.random.nextFloat() * START_FRACTION_RANGE);
        this.quadSize = this.size;
        this.lifetime = SAFETY_LIFETIME;
        this.hasPhysics = true;
        setSize(HITBOX, HITBOX);
        int color = options.color();
        if (options.variant()) {
            this.growth = VARIANT_GROWTH;
            this.friction = DRAG;
            this.verticalDrift = this.random.nextGaussian() * VARIANT_DRIFT;
            setColor(shade(ARGB.red(color)), shade(ARGB.green(color)), shade(ARGB.blue(color)));
            this.xd = options.vx();
            this.yd = options.vy();
            this.zd = options.vz();
        } else {
            this.growth = GROWTH;
            this.friction = DRAG;
            this.verticalDrift = RISE;
            setColor(color);
            launch(options.vx(), options.vy(), options.vz());
        }
        refresh();
    }

    private float shade(int channel) {
        return Mth.clamp(channel / 255.0F + (float) this.random.nextGaussian() * VARIANT_SHADE, 0.0F, 1.0F);
    }

    private void launch(double dx, double dy, double dz) {
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < MIN_DIRECTION) {
            this.xd = dx;
            this.yd = dy;
            this.zd = dz;
            return;
        }
        this.xd = (dx / length + this.random.nextGaussian() * SCATTER) * LAUNCH_SPEED;
        this.yd = (dy / length + this.random.nextGaussian() * SCATTER) * LAUNCH_SPEED;
        this.zd = (dz / length + this.random.nextGaussian() * SCATTER) * LAUNCH_SPEED;
    }

    @Override
    public void tick() {
        this.yd += this.verticalDrift;
        super.tick();
    }

    @Override
    protected void update() {
        this.quadSize = this.size;
        refresh();
        this.size *= this.growth;
        if (this.size >= this.fullSize) {
            remove();
        }
    }

    private void refresh() {
        float grown = Mth.clamp(this.quadSize / this.fullSize, 0.0F, 1.0F);
        this.alpha = START_ALPHA * (1.0F - grown);
        frame(FIRST_SMOKE_FRAME + (int) (grown * FRAME_SPAN));
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TTParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<VentParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("vent");

        @Override
        public @Nullable Particle createParticle(VentParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            if (ParticleCulling.beyondSpawnRange(x, y, z)) {
                return null;
            }
            return new VentParticle(level, x, y, z, options, SHEET);
        }
    }
}
