package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.entity.ThaumaturgeEntityTypeTags;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.entity.ai.LongRangeAttackGoal;
import com.leclowndu93150.thaumaturge.content.entity.eldritch.CastingArms;
import com.leclowndu93150.thaumaturge.network.ClientboundWarpFXPayload;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class EntityEldritchGuardian extends Monster implements RangedAttackMob {
    private static final double MAX_HEALTH = 50.0;
    private static final double FOLLOW_RANGE = 40.0;
    private static final double MOVEMENT_SPEED = 0.28;
    private static final double ATTACK_DAMAGE = 7.0;
    private static final double ARMOR = 4.0;
    private static final int EXPERIENCE = 20;
    private static final double SPAWN_EXCLUSION_HORIZONTAL = 32.0;
    private static final double SPAWN_EXCLUSION_VERTICAL = 16.0;
    private static final double CAST_SPEED = 1.0;
    private static final double CAST_MIN_DISTANCE = 8.0;
    private static final int CAST_MIN_INTERVAL = 20;
    private static final int CAST_MAX_INTERVAL = 40;
    private static final float CAST_RADIUS = 24.0F;
    private static final double MELEE_SPEED = 1.0;
    private static final double HOME_SPEED = 0.8;
    private static final double STROLL_SPEED = 1.0;
    private static final float LOOK_DISTANCE = 8.0F;
    private static final int CAST_PRIORITY = 2;
    private static final int MELEE_PRIORITY = 3;
    private static final int HOME_PRIORITY = 4;
    private static final int STROLL_PRIORITY = 5;
    private static final int LOOK_PLAYER_PRIORITY = 6;
    private static final int LOOK_AROUND_PRIORITY = 7;
    private static final int RETALIATE_PRIORITY = 1;
    private static final int PLAYER_PRIORITY = 2;
    private static final int CULTIST_PRIORITY = 3;
    private static final float SOUND_VOLUME = 1.5F;
    private static final int AMBIENT_INTERVAL = 500;
    private static final float WARDED_DAMAGE_FACTOR = 0.5F;
    private static final int FOG_INTERVAL = 100;
    private static final double HARD_FOG_RANGE_SQR = 576.0;
    private static final double FOG_RANGE_SQR = 256.0;
    private static final float IGNITE_CHANCE_PER_DIFFICULTY = 0.3F;
    private static final int IGNITE_SECONDS_PER_DIFFICULTY = 2;
    private static final float ORB_CHANCE = 0.85F;
    private static final double LEAD_TICKS = 10.0;
    private static final float ORB_SPEED = 1.1F;
    private static final float ORB_INACCURACY = 2.0F;
    private static final float ORB_SOUND_VOLUME = 2.0F;
    private static final float SCREECH_SOUND_VOLUME = 3.0F;
    private static final float SOUND_PITCH_SPREAD = 0.1F;
    private static final int WITHER_TICKS = 400;
    private static final int SCREECH_WARP_BASE = 1;
    private static final int SCREECH_WARP_SPREAD = 3;

    private static final boolean LOOTS_GROUND_ITEMS = false;

    private final CastingArms arms = new CastingArms(this);

    public EntityEldritchGuardian(EntityType<? extends EntityEldritchGuardian> type, Level level) {
        super(type, level);
        this.xpReward = EXPERIENCE;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE).add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE).add(Attributes.ARMOR, ARMOR);
    }

    public static boolean checkGuardianSpawnRules(EntityType<EntityEldritchGuardian> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        boolean crowded = !level.getEntitiesOfClass(EntityEldritchGuardian.class, exclusionZone(pos)).isEmpty();
        return !crowded && Monster.checkAnyLightMonsterSpawnRules(type, level, reason, pos, random);
    }

    private static AABB exclusionZone(BlockPos pos) {
        return new AABB(pos.getX() - SPAWN_EXCLUSION_HORIZONTAL, pos.getY() - SPAWN_EXCLUSION_VERTICAL, pos.getZ() - SPAWN_EXCLUSION_HORIZONTAL, pos.getX() + 1 + SPAWN_EXCLUSION_HORIZONTAL,
                pos.getY() + 1 + SPAWN_EXCLUSION_VERTICAL, pos.getZ() + 1 + SPAWN_EXCLUSION_HORIZONTAL);
    }

    @Override
    protected void registerGoals() {
        registerTargetGoals();
        registerCombatGoals();
        registerIdleGoals();
    }

    private void registerTargetGoals() {
        this.targetSelector.addGoal(RETALIATE_PRIORITY, new HurtByTargetGoal(this));
        hunt(PLAYER_PRIORITY, Player.class);
        hunt(CULTIST_PRIORITY, EntityCultist.class);
    }

    private <T extends LivingEntity> void hunt(int priority, Class<T> quarry) {
        this.targetSelector.addGoal(priority, new NearestAttackableTargetGoal<>(this, quarry, true));
    }

    private void registerCombatGoals() {
        Goal caster = new LongRangeAttackGoal(this, CAST_SPEED, CAST_MIN_DISTANCE, CAST_MIN_INTERVAL, CAST_MAX_INTERVAL, CAST_RADIUS);
        Goal brawler = new MeleeAttackGoal(this, MELEE_SPEED, false);
        behave(CAST_PRIORITY, caster);
        behave(MELEE_PRIORITY, brawler);
    }

    private void registerIdleGoals() {
        behave(HOME_PRIORITY, new MoveTowardsRestrictionGoal(this, HOME_SPEED));
        behave(STROLL_PRIORITY, new WaterAvoidingRandomStrollGoal(this, STROLL_SPEED));
        behave(LOOK_PLAYER_PRIORITY, new LookAtPlayerGoal(this, Player.class, LOOK_DISTANCE));
        behave(LOOK_AROUND_PRIORITY, new RandomLookAroundGoal(this));
    }

    private void behave(int priority, Goal goal) {
        this.goalSelector.addGoal(priority, goal);
    }

    @Override
    public boolean canPickUpLoot() {
        return LOOTS_GROUND_ITEMS;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean warded = source.is(DamageTypeTags.WITCH_RESISTANT_TO);
        return super.hurtServer(level, source, warded ? amount * WARDED_DAMAGE_FACTOR : amount);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level) {
            if (this.tickCount % FOG_INTERVAL == 0 && level.getDifficulty() != Difficulty.EASY) {
                fogNearbyPlayers(level);
            }
        } else {
            this.arms.relax();
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (!super.doHurtTarget(level, target)) {
            return false;
        }
        if (this.isOnFire() && this.getMainHandItem().isEmpty()) {
            spreadFlames(level, target);
        }
        return true;
    }

    private void spreadFlames(ServerLevel level, Entity target) {
        int severity = level.getDifficulty().getId();
        float roll = this.getRandom().nextFloat();
        if (roll < IGNITE_CHANCE_PER_DIFFICULTY * severity) {
            target.igniteForSeconds(IGNITE_SECONDS_PER_DIFFICULTY * severity);
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (this.level() instanceof ServerLevel level) {
            boolean castsOrb = this.getRandom().nextFloat() < ORB_CHANCE;
            if (castsOrb) {
                castOrb(level, target);
                return;
            }
            screech(level, target);
        }
    }

    public CastingArms arms() {
        return this.arms;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (!this.arms.receive(id)) {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return !this.hasHome() && super.removeWhenFarAway(distanceSqr);
    }

    @Override
    protected boolean considersEntityAsAlly(Entity entity) {
        return entity.is(ThaumaturgeEntityTypeTags.ELDRITCH) || super.considersEntityAsAlly(entity);
    }

    @Override
    protected float getSoundVolume() {
        return SOUND_VOLUME;
    }

    @Override
    public int getAmbientSoundInterval() {
        return AMBIENT_INTERVAL;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TTSounds.EGIDLE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TTSounds.EGDEATH.get();
    }

    private void fogNearbyPlayers(ServerLevel level) {
        double range = level.getDifficulty() == Difficulty.HARD ? HARD_FOG_RANGE_SQR : FOG_RANGE_SQR;
        for (ServerPlayer player : level.players()) {
            if (player.isAlive() && player.distanceToSqr(this) < range) {
                PacketDistributor.sendToPlayer(player, ClientboundWarpFXPayload.mistShort());
            }
        }
    }

    private void castOrb(ServerLevel level, LivingEntity target) {
        Vec3 hand = this.arms.castFromNextHand();
        Vec3 aim = leadAim(target);
        EntityEldritchOrb orb = new EntityEldritchOrb(level, this);
        orb.setPos(orb.position().add(hand.x, 0.0, hand.z));
        orb.shoot(aim.x, aim.y, aim.z, ORB_SPEED, ORB_INACCURACY);
        level.addFreshEntity(orb);
        playSpread(level, TTSounds.EGATTACK.get(), ORB_SOUND_VOLUME);
    }

    private Vec3 leadAim(LivingEntity target) {
        Vec3 predicted = target.position().add(target.getKnownMovement().scale(LEAD_TICKS));
        return predicted.subtract(this.position()).normalize();
    }

    private void playSpread(ServerLevel level, SoundEvent sound, float volume) {
        float pitch = 1.0F + this.getRandom().nextFloat() * SOUND_PITCH_SPREAD;
        level.playSound(null, this.getX(), this.getY(), this.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    private void screech(ServerLevel level, LivingEntity target) {
        if (!this.hasLineOfSight(target)) {
            return;
        }
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHER_TICKS, 0));
        if (target instanceof ServerPlayer player) {
            WarpHelper.addWarp(player, SCREECH_WARP_BASE + this.getRandom().nextInt(SCREECH_WARP_SPREAD), WarpType.TEMPORARY);
        }
        playSpread(level, TTSounds.EGSCREECH.get(), SCREECH_SOUND_VOLUME);
    }
}
