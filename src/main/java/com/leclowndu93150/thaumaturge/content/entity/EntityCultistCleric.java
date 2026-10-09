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
    private static final int RAGE_LIMIT = 5;
    private static final int AMBIENT_INTERVAL = 500;
    private static final float ANCHOR_CENTER = 0.5F;
    private static final double ANCHOR_HEIGHT = 1.5;
    private static final float PITCH_STEP = 10.0F;
    private static final float HEAD_STEP = 40.0F;
    private static final float ORB_CHANCE = 0.34F;
    private static final float VOLLEY_CHANCE = 1.0F - ORB_CHANCE;
    private static final double LEAD_TICKS = 10.0;
    private static final float ORB_SPEED = 0.66F;
    private static final float ORB_INACCURACY = 3.0F;
    private static final float ORB_SOUND_VOLUME = 1.0F;
    private static final float ORB_SOUND_PITCH_SPREAD = 0.1F;
    private static final int VOLLEY_EVENT = 1009;
    private static final int VOLLEY_SIZE = 3;
    private static final float VOLLEY_SPREAD = 0.5F;
    private static final double VOLLEY_LIFT = 0.5;

    private static final List<WeightedAttack> ATTACKS = List.of(new WeightedAttack(ORB_CHANCE, new OrbAttack()), new WeightedAttack(VOLLEY_CHANCE, new VolleyAttack()));

    public int rage;

    private final RitualState ritual = new RitualState();

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
        return this.ritual.isActive();
    }

    public void setRitualist(boolean ritualist) {
        this.ritual.set(ritualist);
    }

    public BlockPos ritualAnchor() {
        return this.ritual.anchor();
    }

    @Override
    protected ResourceKey<LootTable> equipmentTable() {
        return TTLootTables.EQUIPMENT_CULTIST_CLERIC;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        this.swing(InteractionHand.MAIN_HAND);
        selectAttack(this.getRandom()).fire(this, level, target, distanceFactor);
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
        boolean immune = this.isInvulnerableTo(level, source);
        if (!immune) {
            this.ritual.clear();
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void tick() {
        super.tick();
        this.ritual.tick();
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

    private static AttackStrategy selectAttack(RandomSource random) {
        float roll = random.nextFloat();
        float threshold = 0.0F;
        for (WeightedAttack attack : ATTACKS) {
            threshold += attack.weight();
            if (roll < threshold) {
                return attack.strategy();
            }
        }
        return ATTACKS.get(ATTACKS.size() - 1).strategy();
    }

    private interface AttackStrategy {
        void fire(EntityCultistCleric cleric, ServerLevel level, LivingEntity target, float distanceFactor);
    }

    private record WeightedAttack(float weight, AttackStrategy strategy) {
    }

    private static final class OrbAttack implements AttackStrategy {
        @Override
        public void fire(EntityCultistCleric cleric, ServerLevel level, LivingEntity target, float distanceFactor) {
            RandomSource random = cleric.getRandom();
            Vec3 aim = Aiming.launchVector(cleric.position(), target.position(), target.getKnownMovement(), LEAD_TICKS);
            EntityGolemOrb orb = new EntityGolemOrb(level, cleric, target, true);
            orb.setPos(orb.getX() + aim.x, orb.getY() + aim.y, orb.getZ() + aim.z);
            orb.shoot(aim.x, aim.y, aim.z, ORB_SPEED, ORB_INACCURACY);
            level.addFreshEntity(orb);
            level.playSound(null, cleric.getX(), cleric.getY(), cleric.getZ(), TTSounds.EGATTACK.get(), SoundSource.HOSTILE, ORB_SOUND_VOLUME, 1.0F + random.nextFloat() * ORB_SOUND_PITCH_SPREAD);
        }
    }

    private static final class VolleyAttack implements AttackStrategy {
        @Override
        public void fire(EntityCultistCleric cleric, ServerLevel level, LivingEntity target, float distanceFactor) {
            RandomSource random = cleric.getRandom();
            level.levelEvent(null, VOLLEY_EVENT, cleric.blockPosition(), 0);
            double spread = VOLLEY_SPREAD * Math.sqrt(distanceFactor);
            double originY = cleric.getY() + cleric.getBbHeight() / 2.0;
            Vec3 origin = new Vec3(cleric.getX(), originY, cleric.getZ());
            Vec3 aimPoint = new Vec3(target.getX(), target.getBoundingBox().minY + target.getBbHeight() / 2.0, target.getZ());
            for (int i = 0; i < VOLLEY_SIZE; i++) {
                double offsetX = random.nextGaussian() * spread;
                double offsetZ = random.nextGaussian() * spread;
                Vec3 direction = Aiming.launchVector(origin, aimPoint, Vec3.ZERO, 0.0, offsetX, offsetZ);
                SmallFireball fireball = new SmallFireball(level, cleric, direction);
                fireball.setPos(fireball.getX(), originY + VOLLEY_LIFT, fireball.getZ());
                level.addFreshEntity(fireball);
            }
        }
    }

    private final class RitualState {
        boolean isActive() {
            return EntityCultistCleric.this.entityData.get(RITUALIST);
        }

        BlockPos anchor() {
            return EntityCultistCleric.this.entityData.get(ANCHOR);
        }

        void set(boolean ritualist) {
            EntityCultistCleric.this.entityData.set(RITUALIST, ritualist);
            if (!ritualist || !EntityCultistCleric.this.hasHome()) {
                return;
            }
            EntityCultistCleric.this.entityData.set(ANCHOR, EntityCultistCleric.this.getHomePosition());
        }

        void clear() {
            set(false);
        }

        void tick() {
            if (!isActive()) {
                return;
            }
            if (EntityCultistCleric.this.level().isClientSide()) {
                focusOnAnchor();
            } else if (EntityCultistCleric.this.rage >= RAGE_LIMIT) {
                clear();
            }
        }

        private void focusOnAnchor() {
            BlockPos anchor = anchor();
            Vec3 focus = new Vec3(anchor.getX() + ANCHOR_CENTER, anchor.getY() + ANCHOR_HEIGHT, anchor.getZ() + ANCHOR_CENTER);
            EntityCultistCleric.this.lookAt(focus, HEAD_STEP, PITCH_STEP);
        }
    }
}
