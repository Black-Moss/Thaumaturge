package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.content.entity.ai.AltarFocusGoal;
import com.leclowndu93150.thaumaturge.content.entity.ai.LongRangeAttackGoal;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

public class EntityCultistCleric extends EntityCultist implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> RITUALIST = SynchedEntityData.defineId(EntityCultistCleric.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<BlockPos> ANCHOR = SynchedEntityData.defineId(EntityCultistCleric.class, EntityDataSerializers.BLOCK_POS);
    private static final String RITUALIST_KEY = "ritualist";
    private static final double MAX_HEALTH = 24.0;
    private static final double MOVE_SPEED = 1.0;
    private static final double MIN_ATTACK_DISTANCE = 2.0;
    private static final int MIN_ATTACK_INTERVAL = 20;
    private static final int MAX_ATTACK_INTERVAL = 40;
    private static final float ATTACK_RADIUS = 24.0F;
    private static final int ALTAR_PRIORITY = 1;
    private static final int RANGED_PRIORITY = 2;
    private static final int MELEE_PRIORITY = 3;
    private static final int AMBIENT_INTERVAL = 500;
    private static final float ORB_SOUND_VOLUME = 1.0F;
    private static final float ORB_SOUND_PITCH_BASE = 1.0F;
    private static final float ORB_SOUND_PITCH_SPREAD = 0.1F;
    private static final int VOLLEY_SIZE = 3;
    private static final float ORB_WEIGHT = 1.0F;
    private static final float VOLLEY_WEIGHT = 2.0F;
    private static final double ORB_LEAD_TICKS = 10.0;
    private static final float ORB_LAUNCH_SPEED = 0.3F;
    private static final float ORB_LAUNCH_SCATTER = 8.0F;
    private static final double TARGET_BODY_MIDDLE = 0.5;
    private static final double FIREBALL_CHEST_HEIGHT = 0.6;
    private static final double FIREBALL_LIFT = 0.4;
    private static final double FIREBALL_FAN_WIDTH = 2.5;
    private static final float FIREBALL_SOUND_VOLUME = 1.0F;
    private static final float FIREBALL_SOUND_PITCH_BASE = 1.0F;
    private static final float FIREBALL_SOUND_PITCH_SPREAD = 0.2F;
    private static final double ALTAR_FOCUS_CENTER = 0.5;
    private static final double ALTAR_FOCUS_HEIGHT = 1.5;
    private static final float RITUAL_HEAD_TURN = 10.0F;
    private static final float RITUAL_PITCH_TURN = 10.0F;

    private static final List<WeightedAttack> ATTACKS = List.of(new WeightedAttack(ORB_WEIGHT, new CrimsonOrbSpell()), new WeightedAttack(VOLLEY_WEIGHT, new FireballFan()));
    private static final float TOTAL_ATTACK_WEIGHT = ORB_WEIGHT + VOLLEY_WEIGHT;

    public EntityCultistCleric(EntityType<? extends EntityCultistCleric> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createCultistAttributes(MAX_HEALTH);
    }

    @Override
    protected void registerGoals() {
        LongRangeAttackGoal ranged = new LongRangeAttackGoal(this, MOVE_SPEED, MIN_ATTACK_DISTANCE, MIN_ATTACK_INTERVAL, MAX_ATTACK_INTERVAL, ATTACK_RADIUS);
        this.goalSelector.addGoal(MELEE_PRIORITY, new MeleeAttackGoal(this, MOVE_SPEED, false));
        this.goalSelector.addGoal(RANGED_PRIORITY, ranged);
        this.goalSelector.addGoal(ALTAR_PRIORITY, new AltarFocusGoal(this));
        this.addIdleGoals();
        this.addTargetGoals();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANCHOR, BlockPos.ZERO);
        builder.define(RITUALIST, false);
    }

    public boolean isRitualist() {
        return this.entityData.get(RITUALIST);
    }

    public void setRitualist(boolean ritualist) {
        this.entityData.set(RITUALIST, ritualist);
        this.syncAnchor();
    }

    public BlockPos ritualAnchor() {
        return this.entityData.get(ANCHOR);
    }

    private void syncAnchor() {
        if (this.isRitualist() && this.hasHome() && !this.getHomePosition().equals(this.ritualAnchor())) {
            this.entityData.set(ANCHOR, this.getHomePosition().immutable());
        }
    }

    public Vec3 ritualFocus() {
        BlockPos anchor = this.ritualAnchor();
        return new Vec3(anchor.getX() + ALTAR_FOCUS_CENTER, anchor.getY() + ALTAR_FOCUS_HEIGHT, anchor.getZ() + ALTAR_FOCUS_CENTER);
    }

    @Override
    protected ResourceKey<LootTable> equipmentTable() {
        return TTLootTables.EQUIPMENT_CULTIST_CLERIC;
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        if (this.isRitualist()) {
            return false;
        }
        return super.removeWhenFarAway(distanceSqr);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (!this.isInvulnerableTo(level, source)) {
            this.setRitualist(false);
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!this.isRitualist() || !this.hasHome()) {
            return;
        }
        this.syncAnchor();
        this.getNavigation().stop();
        Vec3 focus = this.ritualFocus();
        this.getLookControl().setLookAt(focus.x, focus.y, focus.z, RITUAL_HEAD_TURN, RITUAL_PITCH_TURN);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!(this.level() instanceof ServerLevel server) || !this.hasLineOfSight(target)) {
            return;
        }
        this.swing(InteractionHand.MAIN_HAND);
        this.pickAttack(this.random).fire(this, server, target, distanceFactor);
    }

    private AttackStrategy pickAttack(RandomSource random) {
        float roll = random.nextFloat() * TOTAL_ATTACK_WEIGHT;
        for (WeightedAttack attack : ATTACKS) {
            roll -= attack.weight();
            if (roll < 0.0F) {
                return attack.strategy();
            }
        }
        return ATTACKS.getLast().strategy();
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        boolean stored = input.getBooleanOr(RITUALIST_KEY, false);
        super.readAdditionalSaveData(input);
        this.setRitualist(stored);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putBoolean(RITUALIST_KEY, this.isRitualist());
        super.addAdditionalSaveData(output);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TTSounds.CHANT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return AMBIENT_INTERVAL;
    }

    private interface AttackStrategy {
        void fire(EntityCultistCleric cleric, ServerLevel level, LivingEntity target, float distanceFactor);
    }

    private record WeightedAttack(float weight, AttackStrategy strategy) {
    }

    private static final class CrimsonOrbSpell implements AttackStrategy {
        @Override
        public void fire(EntityCultistCleric cleric, ServerLevel level, LivingEntity target, float distanceFactor) {
            EntityGolemOrb orb = new EntityGolemOrb(level, cleric, target, true);
            Vec3 predicted = target.position().add(0.0, target.getBbHeight() * TARGET_BODY_MIDDLE, 0.0).add(target.getDeltaMovement().scale(ORB_LEAD_TICKS));
            Vec3 heading = predicted.subtract(orb.position());
            orb.shoot(heading.x, heading.y, heading.z, ORB_LAUNCH_SPEED, ORB_LAUNCH_SCATTER);
            level.addFreshEntity(orb);
            RandomSource random = cleric.getRandom();
            level.playSound(null, cleric.getX(), cleric.getY(), cleric.getZ(), TTSounds.EGATTACK.get(), SoundSource.HOSTILE, ORB_SOUND_VOLUME,
                    ORB_SOUND_PITCH_BASE + random.nextFloat() * ORB_SOUND_PITCH_SPREAD);
        }
    }

    private static final class FireballFan implements AttackStrategy {
        @Override
        public void fire(EntityCultistCleric cleric, ServerLevel level, LivingEntity target, float distanceFactor) {
            RandomSource random = cleric.getRandom();
            level.playSound(null, cleric.getX(), cleric.getY(), cleric.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, FIREBALL_SOUND_VOLUME,
                    FIREBALL_SOUND_PITCH_BASE + (random.nextFloat() - random.nextFloat()) * FIREBALL_SOUND_PITCH_SPREAD);
            double startY = cleric.getY(FIREBALL_CHEST_HEIGHT) + FIREBALL_LIFT;
            double towardX = target.getX() - cleric.getX();
            double towardY = target.getY(TARGET_BODY_MIDDLE) - startY;
            double towardZ = target.getZ() - cleric.getZ();
            double fanWidth = Math.sqrt(distanceFactor) * FIREBALL_FAN_WIDTH;
            for (int shot = 0; shot < VOLLEY_SIZE; shot++) {
                Vec3 heading = new Vec3(towardX + random.nextGaussian() * fanWidth, towardY, towardZ + random.nextGaussian() * fanWidth);
                SmallFireball fireball = new SmallFireball(level, cleric, heading.normalize());
                fireball.setPos(fireball.getX(), startY, fireball.getZ());
                level.addFreshEntity(fireball);
            }
        }
    }
}
