package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.client.particle.support.ParticleCulling;
import com.leclowndu93150.thaumaturge.client.particle.support.TargetEntityResolver;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class SeekerParticle extends TTParticle {
    protected static final int NO_ENTITY = -1;

    private static final double CULL_RANGE_FANCY = 64.0;
    private static final double CULL_RANGE_FAST = 32.0;
    private static final double SPAWN_KICK = 0.02;
    private static final double HORIZONTAL_DRAG = 0.9;
    private static final double VERTICAL_DRAG = 0.8;
    private static final double PULL_PER_BLOCK = 0.03;
    private static final double MIN_PULL = 0.04;
    private static final double MAX_PULL = 0.3;
    private static final double SWALLOW_DISTANCE = 2.0;
    private static final float SWALLOW_SHRINK = 0.95F;
    private static final double MIN_DISTANCE = 1.0E-4;
    private static final int LIFE_BASE_TICKS = 10;
    private static final double LIFE_TICKS_PER_BLOCK = 6.0;
    private static final double LIFE_SPREAD_MIN = 0.6;
    private static final double LIFE_SPREAD_RANGE = 0.8;

    private final @Nullable TargetEntityResolver targetResolver;
    private final float driftStrength;
    private double targetX;
    private double targetY;
    private double targetZ;

    protected SeekerParticle(ClientLevel level, double x, double y, double z, ParticleSheet sheet, int targetEntityId, Vec3 target, Vec3 velocity, float driftStrength) {
        super(level, x, y, z, velocity.x, velocity.y, velocity.z, sheet);
        this.targetResolver = targetEntityId == NO_ENTITY ? null : new TargetEntityResolver(targetEntityId);
        this.driftStrength = driftStrength;
        setUp(target);
    }

    protected SeekerParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite, int targetEntityId, Vec3 target, Vec3 velocity, float driftStrength) {
        super(level, x, y, z, velocity.x, velocity.y, velocity.z, sprite);
        this.targetResolver = targetEntityId == NO_ENTITY ? null : new TargetEntityResolver(targetEntityId);
        this.driftStrength = driftStrength;
        setUp(target);
    }

    private void setUp(Vec3 target) {
        this.targetX = target.x;
        this.targetY = target.y;
        this.targetZ = target.z;
        followTargetEntity();
        jitterVelocity(SPAWN_KICK);
        double distance = Math.sqrt(Mth.square(this.targetX - this.x) + Mth.square(this.targetY - this.y) + Mth.square(this.targetZ - this.z));
        double spread = LIFE_SPREAD_MIN + this.random.nextDouble() * LIFE_SPREAD_RANGE;
        this.lifetime = LIFE_BASE_TICKS + (int) (distance * LIFE_TICKS_PER_BLOCK * spread);
        if (ParticleCulling.beyondSpawnRange(this.x, this.y, this.z, CULL_RANGE_FANCY, CULL_RANGE_FAST)) {
            remove();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed || delayed()) {
            return;
        }
        followTargetEntity();
        if (insideTargetCell()) {
            remove();
            return;
        }
        double dx = this.targetX - this.x;
        double dy = this.targetY - this.y;
        double dz = this.targetZ - this.z;
        double distance = Math.max(MIN_DISTANCE, Math.sqrt(dx * dx + dy * dy + dz * dz));
        this.xd *= HORIZONTAL_DRAG;
        this.yd *= VERTICAL_DRAG;
        this.zd *= HORIZONTAL_DRAG;
        double pull = Mth.clamp(distance * PULL_PER_BLOCK, MIN_PULL, MAX_PULL);
        this.xd = Mth.clamp(this.xd + dx / distance * pull, -pull, pull);
        this.yd = Mth.clamp(this.yd + dy / distance * pull, -pull, pull);
        this.zd = Mth.clamp(this.zd + dz / distance * pull, -pull, pull);
        drift(this.driftStrength, this.driftStrength, this.driftStrength);
        if (distance < SWALLOW_DISTANCE) {
            this.quadSize *= SWALLOW_SHRINK;
        }
    }

    private void followTargetEntity() {
        if (this.targetResolver == null) {
            return;
        }
        Entity entity = this.targetResolver.resolve(this.level);
        if (entity == null || entity.isRemoved()) {
            return;
        }
        this.targetX = entity.getX();
        this.targetY = entity.getEyeY();
        this.targetZ = entity.getZ();
    }

    private boolean insideTargetCell() {
        return Mth.floor(this.x) == Mth.floor(this.targetX) && Mth.floor(this.y) == Mth.floor(this.targetY) && Mth.floor(this.z) == Mth.floor(this.targetZ);
    }
}
