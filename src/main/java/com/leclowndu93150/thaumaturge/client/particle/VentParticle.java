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
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class VentParticle extends TTParticle {
    private static final int FIRST_SMOKE_FRAME = 1;
    private static final int SAFETY_LIFETIME = 80;
    private static final float SIZE_UNIT = 0.3F;
    private static final float START_FRACTION_BASE = 0.1F;
    private static final float START_FRACTION_RANGE = 0.2F;
    private static final float GROWTH = 1.12F;
    private static final float VARIANT_GROWTH = 1.18F;
    private static final float START_ALPHA = 0.55F;
    private static final double LAUNCH_SPEED = 0.25;
    private static final double SCATTER = 0.12;
    private static final float DRAG = 0.8F;
    private static final float VARIANT_DRAG = 0.9F;
    private static final double RISE = 0.004;
    private static final double VARIANT_DRIFT = 0.003;
    private static final float VARIANT_SHADE = 0.05F;
    private static final double MIN_DIRECTION = 1.0E-4;
    private static final float HITBOX = 0.05F;

    private final float fullSize;
    private final float growth;
    private final double verticalDrift;

    private VentParticle(ClientLevel level, double x, double y, double z, VentParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        this.fullSize = Math.max(HITBOX, options.scale() * SIZE_UNIT);
        this.quadSize = this.fullSize * (START_FRACTION_BASE + this.random.nextFloat() * START_FRACTION_RANGE);
        this.lifetime = SAFETY_LIFETIME;
        this.hasPhysics = true;
        setSize(HITBOX, HITBOX);
        int color = options.color();
        if (options.variant()) {
            this.growth = VARIANT_GROWTH;
            this.friction = VARIANT_DRAG;
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
        return Mth.clamp(channel / 255.0F + (this.random.nextFloat() * 2.0F - 1.0F) * VARIANT_SHADE, 0.0F, 1.0F);
    }

    private void launch(double dx, double dy, double dz) {
        Vec3 direction = new Vec3(dx, dy, dz);
        Vec3 unit = direction.length() < MIN_DIRECTION ? new Vec3(0.0, 1.0, 0.0) : direction.normalize();
        Vec3 scattered = unit.add(this.random.nextGaussian() * SCATTER, this.random.nextGaussian() * SCATTER, this.random.nextGaussian() * SCATTER);
        Vec3 velocity = scattered.normalize().scale(LAUNCH_SPEED);
        this.xd = velocity.x;
        this.yd = velocity.y;
        this.zd = velocity.z;
    }

    @Override
    protected void update() {
        this.yd += this.verticalDrift;
        this.quadSize *= this.growth;
        if (this.quadSize >= this.fullSize) {
            remove();
            return;
        }
        refresh();
    }

    private void refresh() {
        float grown = Mth.clamp(this.quadSize / this.fullSize, 0.0F, 1.0F);
        this.alpha = START_ALPHA * (1.0F - grown);
        int smokeFrames = sheetFrames() - FIRST_SMOKE_FRAME;
        frame(FIRST_SMOKE_FRAME + (int) (grown * smokeFrames));
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
