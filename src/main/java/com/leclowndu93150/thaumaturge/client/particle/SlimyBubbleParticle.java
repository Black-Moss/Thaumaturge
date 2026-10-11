package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.SlimyBubbleParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.RandomSource;

public final class SlimyBubbleParticle extends TTParticle {
    private static final int SWELL_TICKS = 6;
    private static final int SWELL_FRAMES = 4;
    private static final int POP_UP_TICK = 3;
    private static final double POP_UP_HEIGHT = 0.06;
    private static final int FLOAT_FRAME_LOW = 3;
    private static final int FLOAT_FRAME_HIGH = 4;
    private static final int WOBBLE_TICKS = 2;
    private static final double RISE_ACCELERATION = 0.0008;
    private static final int POP_TICKS = 4;
    private static final int POP_FRAME_FIRST = 5;
    private static final int POP_FRAME_SECOND = 6;
    private static final double POP_SLOWDOWN = 0.5;
    private static final float SIZE_UNIT = 0.8F;
    private static final int MIN_LIFETIME = SWELL_TICKS + POP_TICKS + 1;

    private SlimyBubbleParticle(ClientLevel level, double x, double y, double z, SlimyBubbleParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        setColor(options.color());
        this.alpha = options.alpha();
        this.quadSize = options.scale() * SIZE_UNIT;
        this.lifetime = Math.max(MIN_LIFETIME, options.age());
        frame(0);
    }

    @Override
    protected void update() {
        int remaining = this.lifetime - this.age;
        if (this.age < SWELL_TICKS) {
            swell();
        } else if (remaining > POP_TICKS) {
            floatUp();
        } else {
            pop(remaining);
        }
    }

    private void swell() {
        frame(this.age * SWELL_FRAMES / SWELL_TICKS);
        if (this.age == POP_UP_TICK) {
            setPos(this.x, this.y + POP_UP_HEIGHT, this.z);
        }
    }

    private void floatUp() {
        this.yd += RISE_ACCELERATION;
        frame(this.age / WOBBLE_TICKS % 2 == 0 ? FLOAT_FRAME_LOW : FLOAT_FRAME_HIGH);
    }

    private void pop(int remaining) {
        this.yd *= POP_SLOWDOWN;
        frame(remaining > POP_TICKS / 2 ? POP_FRAME_FIRST : POP_FRAME_SECOND);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TTParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<SlimyBubbleParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("slimy_bubble");

        @Override
        public Particle createParticle(SlimyBubbleParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new SlimyBubbleParticle(level, x, y, z, options, SHEET);
        }
    }
}
