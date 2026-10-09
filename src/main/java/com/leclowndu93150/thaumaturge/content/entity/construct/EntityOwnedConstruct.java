package com.leclowndu93150.thaumaturge.content.entity.construct;

import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.Optional;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.Nullable;

public abstract class EntityOwnedConstruct extends PathfinderMob implements OwnableEntity {
    private static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> OWNER = SynchedEntityData.defineId(EntityOwnedConstruct.class,
            EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);
    private static final String OWNER_KEY = "Owner";
    private static final String COMMISSIONED_KEY = "commissioned";
    private static final String LEGACY_COMMISSIONED_KEY = "v";
    private static final String NOT_OWNED_KEY = "message.thaumaturge.construct.not_owned";
    private static final int AMBIENT_INTERVAL = 240;
    private static final float JOLT_YAW_SCALE = 45.0F;
    private static final float JOLT_PITCH_SCALE = 20.0F;
    private static final double MAX_RISE = 0.1;
    private static final float DROP_HEIGHT = 0.5F;
    private static final float DISMANTLE_VOLUME = 1.0F;
    private static final float DISMANTLE_PITCH = 1.0F;

    private boolean commissioned;

    protected EntityOwnedConstruct(EntityType<? extends EntityOwnedConstruct> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder data) {
        data.define(OWNER, Optional.empty());
        super.defineSynchedData(data);
    }

    @Override
    public boolean removeWhenFarAway(double farDistance) {
        return false;
    }

    public boolean isOwned() {
        return getOwnerReference() != null;
    }

    @Override
    public @Nullable EntityReference<LivingEntity> getOwnerReference() {
        Optional<EntityReference<LivingEntity>> stored = entityData.get(OWNER);
        return stored.isPresent() ? stored.get() : null;
    }

    public void setOwner(LivingEntity owner) {
        entityData.set(OWNER, Optional.of(EntityReference.of(owner)));
    }

    public void setValidSpawn() {
        this.commissioned = true;
    }

    public boolean isOwner(LivingEntity entity) {
        EntityReference<LivingEntity> stored = getOwnerReference();
        return stored != null && stored.matches(entity);
    }

    @Override
    public void tick() {
        super.tick();
        releaseAlliedTarget();
        discardUnlessCommissioned();
    }

    private void discardUnlessCommissioned() {
        if (level().isClientSide() || commissioned) {
            return;
        }
        discard();
    }

    private void releaseAlliedTarget() {
        LivingEntity target = getTarget();
        if (target != null && isAlliedTo(target)) {
            setTarget(null);
        }
    }

    @Override
    public @Nullable PlayerTeam getTeam() {
        LivingEntity owner = getOwner();
        if (owner == null) {
            return super.getTeam();
        }
        return owner.getTeam();
    }

    @Override
    protected boolean considersEntityAsAlly(Entity other) {
        LivingEntity owner = getOwner();
        if (owner != null) {
            return other == owner || owner.isAlliedTo(other);
        }
        return super.considersEntityAsAlly(other);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isInteractionIgnored(player)) {
            return InteractionResult.PASS;
        }
        boolean permitted = isOwner(player) || level().isClientSide();
        if (permitted) {
            return super.mobInteract(player, hand);
        }
        TTActionBar.sendPurple(player, NOT_OWNED_KEY);
        return InteractionResult.SUCCESS;
    }

    private boolean isInteractionIgnored(Player player) {
        if (isRemoved()) {
            return true;
        }
        return player.isShiftKeyDown() || player.getMainHandItem().is(Items.NAME_TAG);
    }

    @Override
    public void die(DamageSource source) {
        announceDeathToOwner();
        super.die(source);
    }

    private void announceDeathToOwner() {
        if (!hasCustomName() || !(level() instanceof ServerLevel server)) {
            return;
        }
        boolean shown = server.getGameRules().get(GameRules.SHOW_DEATH_MESSAGES);
        if (shown && getOwner() instanceof ServerPlayer owner) {
            owner.sendSystemMessage(getCombatTracker().getDeathMessage());
        }
    }

    @Override
    public int getAmbientSoundInterval() {
        return AMBIENT_INTERVAL;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TTSounds.TOOL.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource cause) {
        return clatter();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return clatter();
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    private static SoundEvent clatter() {
        return TTSounds.CLACK.get();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        EntityReference.store(getOwnerReference(), out, OWNER_KEY);
        out.putBoolean(COMMISSIONED_KEY, commissioned);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        boolean legacyCommissioned = in.getBooleanOr(LEGACY_COMMISSIONED_KEY, true);
        commissioned = in.getBooleanOr(COMMISSIONED_KEY, legacyCommissioned);
        entityData.set(OWNER, Optional.ofNullable(EntityReference.readWithOldOwnerConversion(in, OWNER_KEY, level())));
    }

    static Vec3 dampHorizontal(Vec3 delta, double divisor) {
        return delta.with(Axis.X, delta.x / divisor).with(Axis.Z, delta.z / divisor);
    }

    private float perturb(float angle, float scale) {
        return angle + (float) random.nextGaussian() * scale;
    }

    final void jolt() {
        float newYaw = perturb(getYRot(), JOLT_YAW_SCALE);
        float newPitch = perturb(getXRot(), JOLT_PITCH_SCALE);
        setXRot(newPitch);
        setYRot(newYaw);
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return getAirSupply();
    }

    final void capRise() {
        Vec3 motion = getDeltaMovement();
        if (motion.y <= MAX_RISE) {
            return;
        }
        setDeltaMovement(motion.x, MAX_RISE, motion.z);
    }

    private void releaseStack(ServerLevel server, ItemStack stack) {
        spawnAtLocation(server, stack, DROP_HEIGHT);
    }

    final void dropHeld() {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        ItemStack held = getMainHandItem();
        if (held.isEmpty()) {
            return;
        }
        setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        releaseStack(server, held);
    }

    final void dismantle(Player player, InteractionHand hand, ItemStack placer) {
        if (level() instanceof ServerLevel server) {
            server.playSound(null, getX(), getY(), getZ(), TTSounds.ZAP.get(), getSoundSource(), DISMANTLE_VOLUME, DISMANTLE_PITCH);
            releaseStack(server, placer);
            discard();
            player.swing(hand);
        }
    }
}
