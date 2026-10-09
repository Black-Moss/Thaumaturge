package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.SlimyBubbleParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class SlimyBubbleParticle extends TTParticle {
    private static final int[] GROW_FRAMES = {0, 0, 1, 1, 2, 2, 3};
    private static final int RISE_START_AGE = GROW_FRAMES.length;
    private static final int LIFT_AGE = 5;
    private static final double LIFT_AMOUNT = 0.1;
    private static final int POP_TICKS = 4;
    private static final int POP_FIRST_FRAME = 5;
    private static final int POP_SECOND_FRAME = 6;
    private static final int POP_FRAME_STEP = 2;
    private static final int WOBBLE_PERIOD = 4;
    private static final int WOBBLE_SPLIT = 2;
    private static final int WOBBLE_LOW_FRAME = 3;
    private static final int WOBBLE_HIGH_FRAME = 4;
    private static final double RISE_ACCELERATION = 0.005;
    private static final double POP_DAMPING = 0.5;

    private SlimyBubbleParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SlimyBubbleParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.color());
        this.alpha = options.alpha();
        this.quadSize = options.scale();
        this.lifetime = options.age();
    }

    @Override
    protected void update() {
        if (this.age == LIFT_AGE) {
            setPos(this.x, this.y + LIFT_AMOUNT, this.z);
        }
        int remaining = this.lifetime - this.age;
        if (remaining < POP_TICKS) {
            int step = POP_TICKS - 1 - remaining;
            frame(step < POP_FRAME_STEP ? POP_FIRST_FRAME : POP_SECOND_FRAME);
            this.yd *= POP_DAMPING;
            return;
        }
        if (this.age < RISE_START_AGE) {
            frame(GROW_FRAMES[this.age]);
        } else {
            frame(this.age % WOBBLE_PERIOD < WOBBLE_SPLIT ? WOBBLE_LOW_FRAME : WOBBLE_HIGH_FRAME);
            this.yd += RISE_ACCELERATION;
        }
    }

    @Override
    public Layer getLayer() {
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
