package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IEntityAspectSource;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.effect.WispEffects;
import com.leclowndu93150.thaumaturge.content.entity.ai.FlyingWanderGoal;
import com.leclowndu93150.thaumaturge.content.entity.ai.WispZapGoal;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class WispEntity extends Monster implements IEntityAspectSource {
    private static final EntityDataAccessor<String> ASPECT = SynchedEntityData.defineId(WispEntity.class, EntityDataSerializers.STRING);
    private static final String ASPECT_SAVE_KEY = "Type";
    private static final BooleanSupplier NEVER_STOP = () -> false;
    private static final BooleanSupplier ALWAYS_WANDER = () -> true;
    private static final double MAX_HEALTH = 22.0;
    private static final double ATTACK_DAMAGE = 3.0;
    private static final double FOLLOW_RANGE = 16.0;
    private static final double FLYING_SPEED = 0.1;
    private static final int EXPERIENCE = 5;
    private static final int MAX_CLUSTER = 2;
    private static final float SOUND_VOLUME = 0.25F;
    private static final float TRAVEL_FRICTION = 0.02F;
    private static final double VERTICAL_DAMPING = 0.6;
    private static final int WANDER_PRIORITY = 5;
    private static final int LOOK_PRIORITY = 7;
    private static final int ZAP_PRIORITY = 7;
    private static final int RETALIATE_PRIORITY = 1;
    private static final int PLAYER_TARGET_PRIORITY = 2;
    private static final int PLAYER_TARGET_INTERVAL = 1000;
    private static final int EXOTIC_ONE_IN = 10;
    private static final int SCAN_AMOUNT = 5;
    private static final int SPAWN_CAP = 8;
    private static final double SPAWN_CAP_RADIUS = 16.0;
    private static final float BIRTH_BURST_SIZE = 10.0F;
    private static final float DEATH_BURST_SIZE = 1.0F;
    private static final double DEATH_BURST_LIFT = 0.45;
    private static final int BIRTH_TICKS = 1;
    private static final int DEATH_BURST_TICK = 1;
    private static final int MOTE_ONE_IN = 2;
    private static final double MOTE_SPREAD = 0.7;

    public WispEntity(EntityType<? extends WispEntity> type, Level level) {
        super(type, level);
        this.xpReward = EXPERIENCE;
        this.moveControl = new Ghast.GhastMoveControl(this, false, NEVER_STOP);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE).add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE)
                .add(Attributes.FLYING_SPEED, FLYING_SPEED);
    }

    public static boolean checkWispSpawnRules(EntityType<WispEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return !isCrowded(level, pos) && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    private static boolean isCrowded(ServerLevelAccessor level, BlockPos pos) {
        return level.getEntitiesOfClass(WispEntity.class, new AABB(pos).inflate(SPAWN_CAP_RADIUS)).size() >= SPAWN_CAP;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(WANDER_PRIORITY, new FlyingWanderGoal(this, true, ALWAYS_WANDER));
        goalSelector.addGoal(LOOK_PRIORITY, new Ghast.GhastLookGoal(this));
        goalSelector.addGoal(ZAP_PRIORITY, new WispZapGoal(this));
        targetSelector.addGoal(RETALIATE_PRIORITY, new HurtByTargetGoal(this));
        targetSelector.addGoal(PLAYER_TARGET_PRIORITY, new NearestAttackableTargetGoal<>(this, Player.class, PLAYER_TARGET_INTERVAL, true, false, null));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ASPECT, "");
    }

    public @Nullable Holder<IAspect> aspect() {
        String raw = entityData.get(ASPECT);
        Identifier id = raw.isEmpty() ? null : Identifier.tryParse(raw);
        return id == null ? null : Aspects.resolve(level(), ResourceKey.create(IAspect.REGISTRY_KEY, id));
    }

    public void setAspect(Identifier id) {
        entityData.set(ASPECT, id.toString());
    }

    @Override
    public AspectList entityAspects() {
        Level world = level();
        List<@Nullable Holder<IAspect>> scanned = Arrays.asList(Aspects.resolve(world, TTAspects.AURAM), Aspects.resolve(world, TTAspects.VOLATUS), aspect());
        AspectList total = AspectList.EMPTY;
        for (Holder<IAspect> entry : scanned) {
            total = entry == null ? total : total.add(entry, SCAN_AMOUNT);
        }
        return total;
    }

    @Override
    protected float getSoundVolume() {
        return SOUND_VOLUME;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TTSounds.WISPLIVE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TTSounds.WISPDEAD.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.LAVA_EXTINGUISH;
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(getDeltaMovement().multiply(1.0, VERTICAL_DAMPING, 1.0));
        if (!level().isClientSide()) {
            return;
        }
        spawnClientEffects();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (aspect() != null) {
            return;
        }
        if (level() instanceof ServerLevel server) {
            pickAspect(server);
        }
    }

    private void pickAspect(ServerLevel level) {
        boolean exotic = random.nextInt(EXOTIC_ONE_IN) == 0;
        List<Holder.Reference<IAspect>> pool = candidates(level, exotic);
        if (pool.isEmpty()) {
            return;
        }
        Holder.Reference<IAspect> chosen = pool.get(random.nextInt(pool.size()));
        setAspect(chosen.key().identifier());
    }

    private static List<Holder.Reference<IAspect>> candidates(ServerLevel level, boolean exotic) {
        HolderLookup.RegistryLookup<IAspect> registry = level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY);
        return registry.listElements().filter(candidate -> candidate.value().isPrimal() != exotic).toList();
    }

    @Override
    public void travel(Vec3 input) {
        travelFlying(input, TRAVEL_FRICTION);
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    protected void checkFallDamage(double yDelta, boolean onGround, BlockState state, BlockPos pos) {}

    private void spawnClientEffects() {
        if (tickCount <= BIRTH_TICKS) {
            level().addParticle(WispEffects.burst(BIRTH_BURST_SIZE), getX(), getY(), getZ(), 0.0, 0.0, 0.0);
        }
        if (isDeadOrDying() && deathTime == DEATH_BURST_TICK) {
            level().addParticle(WispEffects.burst(DEATH_BURST_SIZE), getX(), getY() + DEATH_BURST_LIFT, getZ(), 0.0, 0.0, 0.0);
        }
        Holder<IAspect> aspect = aspect();
        if (aspect == null || random.nextInt(MOTE_ONE_IN) != 0) {
            return;
        }
        double moteX = getX() + moteOffset();
        double moteY = getY() + getBbHeight() / 2.0 + moteOffset();
        double moteZ = getZ() + moteOffset();
        level().addParticle(WispEffects.mote(random, aspect.value().color()), moteX, moteY, moteZ, 0.0, 0.0, 0.0);
    }

    private double moteOffset() {
        return (random.nextDouble() * 2.0 - 1.0) * MOTE_SPREAD;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        Holder<IAspect> aspect = aspect();
        if (aspect != null) {
            spawnAtLocation(level, EssentiaCrystalFactory.of(aspect));
        }
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return MAX_CLUSTER;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        String stored = entityData.get(ASPECT);
        output.putString(ASPECT_SAVE_KEY, stored);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String stored = input.getStringOr(ASPECT_SAVE_KEY, "");
        entityData.set(ASPECT, stored);
    }
}
