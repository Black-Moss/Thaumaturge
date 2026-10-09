package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public abstract class SeekerParticle extends TTParticle {
    public static final int NO_ENTITY = -1;

    private static final double SPAWN_VELOCITY_NOISE = 0.01;
    private static final double TICKS_PER_BLOCK = 10.0;
    private static final double MIN_TRAVEL_TICKS = 1.0;
    private static final double LIFETIME_MIN_FACTOR = 0.5;
    private static final double HORIZONTAL_DAMPING = 0.985;
    private static final double VERTICAL_DAMPING = 0.95;
    private static final double MAX_PULL = 0.25;
    private static final double PULL_DISTANCE_DIVISOR = 15.0;
    private static final double SHRINK_DISTANCE = 2.0;
    private static final float SHRINK_FACTOR = 0.9F;
    private static final double MIN_DISTANCE = 1.0E-6;

    private final int targetEntityId;
    private final float driftStrength;
    private Vec3 goal = Vec3.ZERO;

    protected SeekerParticle(ClientLevel level, double x, double y, double z, ParticleSheet sheet, int targetEntityId, Vec3 target, Vec3 velocity, float driftStrength) {
        super(level, x, y, z, velocity.x, velocity.y, velocity.z, sheet);
        this.targetEntityId = targetEntityId;
        this.driftStrength = driftStrength;
        launch(target);
    }

    protected SeekerParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite, int targetEntityId, Vec3 target, Vec3 velocity, float driftStrength) {
        super(level, x, y, z, velocity.x, velocity.y, velocity.z, sprite);
        this.targetEntityId = targetEntityId;
        this.driftStrength = driftStrength;
        launch(target);
    }

    private void launch(Vec3 target) {
        this.goal = target;
        jitterVelocity(SPAWN_VELOCITY_NOISE);
        double flightTicks = Math.max(MIN_TRAVEL_TICKS, offsetToGoal().length() * TICKS_PER_BLOCK);
        double lifeScale = LIFETIME_MIN_FACTOR + this.random.nextDouble();
        this.lifetime = Math.max(1, (int) (flightTicks * lifeScale));
    }

    @Override
    public void tick() {
        rememberPosition();
        if (this.age++ >= this.lifetime) {
            remove();
            return;
        }
        followTargetEntity();
        glide();
        steer();
        if (insideGoalCell()) {
            remove();
        } else {
            update();
        }
    }

    private void rememberPosition() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    private void followTargetEntity() {
        Entity entity = this.targetEntityId == NO_ENTITY ? null : this.level.getEntity(this.targetEntityId);
        if (entity != null) {
            this.goal = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
        }
    }

    private void glide() {
        move(this.xd, this.yd, this.zd);
        this.xd *= HORIZONTAL_DAMPING;
        this.zd *= HORIZONTAL_DAMPING;
        this.yd *= VERTICAL_DAMPING;
    }

    private void steer() {
        Vec3 offset = offsetToGoal();
        double distance = offset.length();
        if (distance > MIN_DISTANCE) {
            double limit = Math.min(MAX_PULL, distance / PULL_DISTANCE_DIVISOR);
            Vec3 pull = offset.scale(limit / distance);
            this.xd = clampSpeed(this.xd + pull.x, limit);
            this.yd = clampSpeed(this.yd + pull.y, limit);
            this.zd = clampSpeed(this.zd + pull.z, limit);
        }
        drift(this.driftStrength, this.driftStrength, this.driftStrength);
        if (distance < SHRINK_DISTANCE) {
            this.quadSize *= SHRINK_FACTOR;
        }
    }

    private static double clampSpeed(double speed, double limit) {
        return Mth.clamp(speed, -limit, limit);
    }

    private Vec3 offsetToGoal() {
        return this.goal.subtract(this.x, this.y, this.z);
    }

    private boolean insideGoalCell() {
        return sameCell(this.x, this.goal.x) && sameCell(this.y, this.goal.y) && sameCell(this.z, this.goal.z);
    }

    private static boolean sameCell(double a, double b) {
        return Mth.floor(a) == Mth.floor(b);
    }
}
