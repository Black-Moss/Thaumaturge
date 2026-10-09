package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class BlockMistParticle extends TTParticle {
    private static final int LIFETIME_BASE = 50;
    private static final int LIFETIME_RANGE = 25;
    private static final float GRAVITY = 0.1F;
    private static final float START_SIZE = 0.5F;
    private static final float END_SIZE = 0.1F;
    private static final double WIND_SCALE = 0.001;
    private static final float PEAK_ALPHA = 0.5F;
    private static final float[] FADE_CURVE = {0.0F, PEAK_ALPHA, 0.4F, 0.3F, 0.2F, 0.1F, 0.0F};

    private BlockMistParticle(ParticleSheet sheet, ClientLevel level, Vec3 origin, Vec3 motion, ColorParticleOption tint) {
        super(level, origin.x, origin.y, origin.z, motion.x, motion.y, motion.z, sheet);
        setColor(tint.getRed(), tint.getGreen(), tint.getBlue());
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.gravity = GRAVITY;
        this.quadSize = START_SIZE;
        this.alpha = 0.0F;
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? 1.0F : -1.0F);
        setMoonWind(WIND_SCALE);
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = Keyframes.sample(t, FADE_CURVE);
        this.quadSize = Mth.lerp(t, START_SIZE, END_SIZE);
    }

    public static final class Provider implements ParticleProvider<ColorParticleOption> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("block_mist");

        @Override
        public Particle createParticle(ColorParticleOption options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new BlockMistParticle(SHEET, level, new Vec3(x, y, z), new Vec3(vx, vy, vz), options);
        }
    }
}
