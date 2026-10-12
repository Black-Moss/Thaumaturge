package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.SlimyBubbleParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.RandomSource;

public final class SlimyBubbleParticle extends TTParticle {
    private static final int SWELL_END_AGE = 6;
    private static final int SWELL_FRAME_DIVISOR = 2;
    private static final int POP_UP_AGE = 5;
    private static final double POP_UP_HEIGHT = 0.1;
    private static final int FLOAT_FRAME_BASE = 3;
    private static final int FLOAT_CYCLE_TICKS = 4;
    private static final int FLOAT_FRAME_DIVISOR = 2;
    private static final double RISE_ACCELERATION = 0.005;
    private static final int POP_TICKS = 4;
    private static final int POP_FRAME_LAST = 6;
    private static final int POP_FRAME_DIVISOR = 2;
    private static final double POP_SLOWDOWN = 0.5;

    private SlimyBubbleParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SlimyBubbleParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.color());
        this.alpha = options.alpha();
        this.quadSize = options.scale();
        this.lifetime = options.age();
        frame(0);
    }

    @Override
    protected void update() {
        int remaining = this.lifetime - this.age;
        if (this.age <= SWELL_END_AGE) {
            swell();
        } else if (remaining > POP_TICKS) {
            floatUp();
        } else {
            pop(remaining);
        }
    }

    private void swell() {
        frame(this.age / SWELL_FRAME_DIVISOR);
        if (this.age == POP_UP_AGE) {
            setPos(this.x, this.y + POP_UP_HEIGHT, this.z);
        }
    }

    private void floatUp() {
        this.yd += RISE_ACCELERATION;
        frame(FLOAT_FRAME_BASE + this.age % FLOAT_CYCLE_TICKS / FLOAT_FRAME_DIVISOR);
    }

    private void pop(int remaining) {
        this.yd *= POP_SLOWDOWN;
        frame(POP_FRAME_LAST - remaining / POP_FRAME_DIVISOR);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TTParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<SlimyBubbleParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("slimy_bubble");

        @Override
        public Particle createParticle(SlimyBubbleParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new SlimyBubbleParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
