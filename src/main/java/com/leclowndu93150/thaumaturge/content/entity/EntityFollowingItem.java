package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jspecify.annotations.Nullable;

public final class EntityFollowingItem extends ItemEntity implements IEntityWithComplexSpawn {
    private static final int NO_COLLECTOR = -1;
    private static final int IDLE = -1;
    private static final float FULL_TURN_DEGREES = 360.0F;
    private static final double ARRIVAL_DISTANCE = 0.5;
    private static final double ARRIVAL_DAMPING = 0.1;
    private static final int MAX_PURSUIT = 19;
    private static final int PURSUIT_DIVISOR_BASE = 20;
    private static final double HALF = 0.5;
    private static final int TRAIL_COLOR = 0xFF6F86FF;
    private static final float TRAIL_ALPHA = 1.0F;
    private static final float TRAIL_SCALE_BASE = 0.35F;
    private static final float TRAIL_SCALE_SPREAD = 0.25F;
    private static final int TRAIL_AGE_BASE = 14;
    private static final int TRAIL_AGE_SPREAD = 8;
    private static final float TRAIL_BUOYANCY = -0.002F;
    private static final double TRAIL_JITTER = 0.12;

    private @Nullable Entity collector;
    private int pursuit = IDLE;

    public EntityFollowingItem(EntityType<? extends EntityFollowingItem> type, Level level) {
        super(type, level);
    }

    public EntityFollowingItem(Level level, double x, double y, double z, ItemStack stack, Entity collector) {
        this(TTEntities.FOLLOWING_ITEM.get(), level);
        this.setPos(x, y, z);
        this.setItem(stack);
        this.setYRot(this.random.nextFloat() * FULL_TURN_DEGREES);
        engage(collector);
    }

    private boolean isPursuing() {
        return this.pursuit != IDLE;
    }

    private void engage(Entity target) {
        this.collector = target;
        this.pursuit = 0;
        this.setNoGravity(true);
    }

    private void disengage() {
        this.pursuit = IDLE;
        this.setNoGravity(false);
    }

    @Override
    public void tick() {
        if (isPursuing()) {
            stepTowardCollector();
        }
        super.tick();
    }

    private void stepTowardCollector() {
        Entity target = this.collector;
        if (target == null || target.isRemoved()) {
            disengage();
            return;
        }
        Vec3 gap = target.position().add(0.0, target.getBbHeight() * HALF, 0.0).subtract(this.position());
        if (gap.length() <= ARRIVAL_DISTANCE) {
            this.setDeltaMovement(this.getDeltaMovement().scale(ARRIVAL_DAMPING));
            disengage();
            return;
        }
        this.pursuit = Math.min(MAX_PURSUIT, this.pursuit + 1);
        this.setDeltaMovement(gap.normalize().scale(1.0 / (PURSUIT_DIVISOR_BASE - this.pursuit)));
        if (this.level().isClientSide()) {
            emitTrailBubble();
        }
    }

    private double jitter() {
        return this.random.triangle(0.0, TRAIL_JITTER);
    }

    private void emitTrailBubble() {
        float scale = TRAIL_SCALE_BASE + this.random.nextFloat() * TRAIL_SCALE_SPREAD;
        int age = TRAIL_AGE_BASE + this.random.nextInt(TRAIL_AGE_SPREAD);
        double px = this.xo + jitter();
        double py = this.yo + this.getBbHeight() * HALF + jitter();
        double pz = this.zo + jitter();
        this.level().addParticle(new BubbleParticleOptions(TRAIL_COLOR, TRAIL_ALPHA, scale, age, TRAIL_BUOYANCY, false), px, py, pz, 0.0, 0.0, 0.0);
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        Entity target = isPursuing() ? this.collector : null;
        buffer.writeVarInt(target == null ? NO_COLLECTOR : target.getId());
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        int id = buffer.readVarInt();
        if (id == NO_COLLECTOR) {
            return;
        }
        Entity found = this.level().getEntity(id);
        if (found != null) {
            engage(found);
        }
    }
}
