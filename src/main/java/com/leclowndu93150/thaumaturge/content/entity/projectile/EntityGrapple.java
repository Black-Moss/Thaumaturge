package com.leclowndu93150.thaumaturge.content.entity.projectile;

import com.leclowndu93150.thaumaturge.mixin.server.network.ServerGamePacketListenerImplAccessor;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class EntityGrapple extends ThrowableProjectile {
    private static final EntityDataAccessor<Boolean> PULLING = SynchedEntityData.defineId(EntityGrapple.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> MAIN_HAND = SynchedEntityData.defineId(EntityGrapple.class, EntityDataSerializers.BOOLEAN);
    private static final double FLIGHT_GRAVITY = 0.03;
    private static final double PULLING_GRAVITY = 0.0;
    private static final int MAX_FLIGHT_TICKS = 30;
    private static final int NO_GRAPPLE = -1;
    private static final double START_HEIGHT_DROP = 0.1;
    private static final double NEAR_RANGE = 8.0;
    private static final double MIN_DIVISOR = 1.0E-9;
    private static final double PULL_SCALE = 5.0;
    private static final double MAX_PULL = 0.25;
    private static final double LIFT = 0.033;
    private static final double BOOST = 0.4;
    private static final float AMPLITUDE_GROWTH = 0.02F;
    private static final float AMPLITUDE_DECAY = 0.66F;

    public float ampl;

    private boolean claimed;
    private boolean boosted;

    public EntityGrapple(EntityType<? extends EntityGrapple> type, Level level) {
        super(type, level);
    }

    public EntityGrapple(EntityType<? extends EntityGrapple> type, Level level, LivingEntity thrower, InteractionHand hand) {
        super(type, level);
        setOwner(thrower);
        setPos(thrower.getX(), thrower.getEyeY() - START_HEIGHT_DROP, thrower.getZ());
        entityData.set(MAIN_HAND, hand == InteractionHand.MAIN_HAND);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(PULLING, false);
        builder.define(MAIN_HAND, true);
    }

    @Override
    protected double getDefaultGravity() {
        return isPulling() ? PULLING_GRAVITY : FLIGHT_GRAVITY;
    }

    public boolean isPulling() {
        return entityData.get(PULLING);
    }

    public InteractionHand getHand() {
        return entityData.get(MAIN_HAND) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    @Override
    public void tick() {
        super.tick();
        Entity owner = getOwner();
        if (!isPulling() && !isRemoved() && (tickCount > MAX_FLIGHT_TICKS || owner == null)) {
            remove(Entity.RemovalReason.DISCARDED);
            return;
        }
        if (owner == null) {
            return;
        }
        if (level().isClientSide()) {
            ampl = isPulling() ? ampl * AMPLITUDE_DECAY : ampl + AMPLITUDE_GROWTH;
        } else if (!claim(owner)) {
            return;
        }
        if (isPulling() && isAlive() && owner instanceof LivingEntity living) {
            pull(living);
        }
    }

    private boolean claim(Entity owner) {
        int stored = owner.getData(TTAttachments.GRAPPLE_ID);
        if (!claimed) {
            if (stored >= 0 && stored != getId() && level().getEntity(stored) instanceof EntityGrapple previous) {
                previous.remove(Entity.RemovalReason.DISCARDED);
            }
            owner.setData(TTAttachments.GRAPPLE_ID, getId());
            claimed = true;
            return true;
        }
        if (stored != getId()) {
            remove(Entity.RemovalReason.DISCARDED);
            return false;
        }
        return true;
    }

    private void pull(LivingEntity owner) {
        if (owner.isShiftKeyDown()) {
            if (!level().isClientSide()) {
                remove(Entity.RemovalReason.DISCARDED);
            }
            return;
        }
        if (owner instanceof ServerPlayer serverPlayer) {
            ((ServerGamePacketListenerImplAccessor) serverPlayer.connection).setAboveGroundTickCount(0);
        }
        owner.resetFallDistance();
        if (level().isClientSide() && !(owner instanceof Player player && player.isLocalPlayer())) {
            return;
        }
        Vec3 toHook = position().subtract(owner.position());
        double distance = toHook.length();
        double divisor = Math.max(distance < NEAR_RANGE ? distance * (NEAR_RANGE - distance) : distance, MIN_DIVISOR);
        Vec3 pull = toHook.scale(1.0 / (divisor * PULL_SCALE));
        double length = pull.length();
        if (length > MAX_PULL) {
            pull = pull.scale(MAX_PULL / length);
        }
        double lift = LIFT;
        if (!boosted) {
            boosted = true;
            lift += BOOST;
        }
        owner.setDeltaMovement(owner.getDeltaMovement().add(pull.x, pull.y + lift, pull.z));
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!level().isClientSide()) {
            Entity owner = getOwner();
            if (owner != null && owner.hasData(TTAttachments.GRAPPLE_ID) && owner.getData(TTAttachments.GRAPPLE_ID) == getId()) {
                owner.setData(TTAttachments.GRAPPLE_ID, NO_GRAPPLE);
            }
        }
        super.remove(reason);
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (!level().isClientSide() && !isPulling()) {
            entityData.set(PULLING, true);
            setDeltaMovement(Vec3.ZERO);
            setPos(hitResult.getLocation());
        }
    }
}
