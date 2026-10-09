package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class TTParticle extends SingleQuadParticle {
    private static final double FULL_TURN_RADIANS = Math.PI * 2.0;
    private static final double SPIN_UNIT_DEGREES_PER_TICK = 360.0 * Math.PI / 180.0;
    private static final float SPIN_UNIT_RADIANS_PER_TICK = (float) Math.toRadians(SPIN_UNIT_DEGREES_PER_TICK);
    private static final int MOON_PHASE_COUNT = 8;
    private static final double MOON_PHASE_STEP_RADIANS = Math.PI / 4.0;
    private static final float[] MOON_PHASE_HEADINGS = buildMoonPhaseHeadings();
    private static final float WIND_STRENGTH = 0.1F;
    private static final float HEADING_JITTER_RADIANS = 0.17F;

    protected final @Nullable ParticleSheet sheet;
    protected int frame;
    private int delay;
    private float rollStepRadians;
    private double moonPushX;
    private double moonPushZ;

    protected TTParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, ParticleSheet sheet) {
        this(level, x, y, z, vx, vy, vz, sheet, null);
    }

    protected TTParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, TextureAtlasSprite sprite) {
        this(level, x, y, z, vx, vy, vz, null, sprite);
    }

    private TTParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, @Nullable ParticleSheet sheet, @Nullable TextureAtlasSprite sprite) {
        super(level, x, y, z, vx, vy, vz, sprite);
        this.sheet = sheet;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.hasPhysics = false;
        this.friction = 1.0F;
        this.gravity = 0.0F;
        this.quadSize = 0.1F;
        this.setSize(0.1F, 0.1F);
    }

    @Override
    public void tick() {
        recordPreviousPosition();
        if (consumeDelayTick()) {
            return;
        }
        advanceRoll();
        applyMoonPush();
        super.tick();
        if (!this.removed) {
            update();
        }
    }

    protected abstract void update();

    private static float[] buildMoonPhaseHeadings() {
        float[] headings = new float[MOON_PHASE_COUNT];
        for (int phase = 0; phase < MOON_PHASE_COUNT; phase++) {
            headings[phase] = (float) (phase * MOON_PHASE_STEP_RADIANS);
        }
        return headings;
    }

    private void recordPreviousPosition() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    private boolean consumeDelayTick() {
        if (this.delay <= 0) {
            return false;
        }
        this.delay--;
        return true;
    }

    private void advanceRoll() {
        this.oRoll = this.roll;
        this.roll += this.rollStepRadians;
    }

    private void applyMoonPush() {
        this.xd += this.moonPushX;
        this.zd += this.moonPushZ;
    }

    protected float progress() {
        return Mth.clamp((float) this.age / this.lifetime, 0.0F, 1.0F);
    }

    protected void setDelay(int delay) {
        this.delay = Math.max(0, delay);
        if (this.delay > 0) {
            this.alpha = 0.0F;
        }
    }

    protected boolean delayed() {
        return this.delay > 0;
    }

    protected void setSpin(float startTurns, float speed) {
        this.roll = (float) (startTurns * FULL_TURN_RADIANS);
        this.oRoll = this.roll;
        this.rollStepRadians = speed * SPIN_UNIT_RADIANS_PER_TICK;
    }

    protected void setMoonWind(double scale) {
        int phase = this.level.environmentAttributes().getValue(EnvironmentAttributes.MOON_PHASE, new Vec3(this.x, this.y, this.z)).index();
        float heading = MOON_PHASE_HEADINGS[Math.floorMod(phase, MOON_PHASE_COUNT)] + this.random.nextFloat() * HEADING_JITTER_RADIANS;
        this.moonPushX = Mth.cos(heading) * WIND_STRENGTH * scale;
        this.moonPushZ = Mth.sin(heading) * WIND_STRENGTH * scale;
    }

    protected void drift(float strengthX, float strengthY, float strengthZ) {
        if (strengthX != 0.0F)
            this.xd += this.random.nextGaussian() * strengthX;
        if (strengthY != 0.0F)
            this.yd += this.random.nextGaussian() * strengthY;
        if (strengthZ != 0.0F)
            this.zd += this.random.nextGaussian() * strengthZ;
    }

    protected void jitterVelocity(double deviation) {
        this.xd += this.random.nextGaussian() * deviation;
        this.yd += this.random.nextGaussian() * deviation;
        this.zd += this.random.nextGaussian() * deviation;
    }

    protected void frame(int index) {
        this.frame = Mth.clamp(index, 0, this.sheet == null ? 0 : this.sheet.frames() - 1);
    }

    protected void frameByProgress() {
        if (this.sheet != null) {
            frame((int) (progress() * this.sheet.frames()));
        }
    }

    protected int sheetFrames() {
        return this.sheet == null ? 1 : this.sheet.frames();
    }

    protected void setColor(int color) {
        this.rCol = ARGB.red(color) / 255.0F;
        this.gCol = ARGB.green(color) / 255.0F;
        this.bCol = ARGB.blue(color) / 255.0F;
    }

    protected void lerpColor(float t, float r0, float g0, float b0, float r1, float g1, float b1) {
        this.rCol = Mth.lerp(t, r0, r1);
        this.gCol = Mth.lerp(t, g0, g1);
        this.bCol = Mth.lerp(t, b0, b1);
    }

    @Override
    protected float getU0() {
        return this.sheet != null ? this.sheet.u0(this.frame) : super.getU0();
    }

    @Override
    protected float getU1() {
        return this.sheet != null ? this.sheet.u1(this.frame) : super.getU1();
    }

    @Override
    protected float getV0() {
        return this.sheet != null ? 0.0F : super.getV0();
    }

    @Override
    protected float getV1() {
        return this.sheet != null ? 1.0F : super.getV1();
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return this.sheet != null ? TTParticleLayers.additive(this.sheet) : Layer.bySprite(this.sprite);
    }
}
