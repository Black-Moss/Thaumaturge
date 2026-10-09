package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.content.entity.portal.CultistPortals;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class EntityCultistPortalLesser extends Monster {
    private static final EntityDataAccessor<Boolean> ACTIVE = SynchedEntityData.defineId(EntityCultistPortalLesser.class, EntityDataSerializers.BOOLEAN);
    private static final String ACTIVE_KEY = "active";
    private static final double MAX_HEALTH = 100.0;
    private static final double ARMOR = 4.0;
    private static final int EXPERIENCE = 10;
    private static final double PLAYER_RANGE = 32.0;
    private static final double CULTIST_RANGE = 32.0;
    private static final int ACTIVATION_INTERVAL = 10;
    private static final int FIRST_ATTEMPT_DELAY = 100;
    private static final int ATTEMPT_DELAY_BASE = 51;
    private static final int ATTEMPT_DELAY_SPREAD = 50;
    private static final int HARD_CULTIST_CAP = 6;
    private static final int NORMAL_CULTIST_CAP = 4;
    private static final int LOW_CULTIST_CAP = 2;
    private static final int SPAWN_DAMAGE_BASE = 5;
    private static final int SPAWN_DAMAGE_SPREAD = 5;
    private static final float ACTIVATION_SOUND_VOLUME = 1.0F;
    private static final float ACTIVATION_SOUND_PITCH = 1.0F;
    private static final double TOUCH_DISTANCE_SQR = 3.0;
    private static final float TOUCH_DAMAGE = 4.0F;
    private static final float DEATH_BLAST_POWER = 1.5F;

    public int activeCounter;
    public int pulse;

    private int spawnCountdown = FIRST_ATTEMPT_DELAY;

    public EntityCultistPortalLesser(EntityType<? extends EntityCultistPortalLesser> type, Level level) {
        super(type, level);
        this.xpReward = EXPERIENCE;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.ARMOR, ARMOR).add(Attributes.ATTACK_DAMAGE, 0.0).add(Attributes.MAX_HEALTH, MAX_HEALTH);
    }

    @Override
    protected void registerGoals() {}

    @Override
    public void tick() {
        super.tick();
        boolean awake = this.isActive();
        this.activeCounter += awake ? 1 : 0;
        if (this.level() instanceof ServerLevel level) {
            if (awake) {
                runSpawnCycle(level);
            } else {
                awakenIfWatched(level);
            }
        }
    }

    @Override
    public void aiStep() {
        this.pulse = this.pulse > 0 ? this.pulse - 1 : 0;
    }

    @Override
    public void move(MoverType type, Vec3 movement) {}

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return false;
    }

    @Override
    public boolean addEffect(MobEffectInstance effect, Entity source) {
        return false;
    }

    @Override
    public boolean causeFallDamage(double distance, float damageMultiplier, DamageSource source) {
        return false;
    }

    @Override
    public void playerTouch(Player player) {
        CultistPortals.touch(this, player, TOUCH_DAMAGE);
    }

    @Override
    public void die(DamageSource source) {
        CultistPortals.collapse(this, DEATH_BLAST_POWER);
        super.die(source);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == CultistPortals.PULSE_EVENT) {
            this.pulse = CultistPortals.PULSE_TICKS;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TTSounds.MONOLITH.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TTSounds.ZAP.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TTSounds.SHOCK.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return CultistPortals.AMBIENT_INTERVAL;
    }

    @Override
    protected float getSoundVolume() {
        return CultistPortals.SOUND_VOLUME;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTIVE, false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean(ACTIVE_KEY, this.entityData.get(ACTIVE));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setActive(input.getBooleanOr(ACTIVE_KEY, false));
    }

    public boolean isActive() {
        Boolean flag = this.entityData.get(ACTIVE);
        return flag;
    }

    public void setActive(boolean awake) {
        if (awake != this.isActive()) {
            this.entityData.set(ACTIVE, awake);
        }
    }

    private void awakenIfWatched(ServerLevel level) {
        boolean due = this.tickCount % ACTIVATION_INTERVAL == 0;
        Player viewer = due ? level.getNearestPlayer(this, PLAYER_RANGE) : null;
        if (viewer == null) {
            return;
        }
        this.setActive(true);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), TTSounds.CRAFTSTART.get(), SoundSource.HOSTILE, ACTIVATION_SOUND_VOLUME, ACTIVATION_SOUND_PITCH);
    }

    private void runSpawnCycle(ServerLevel level) {
        if (--this.spawnCountdown > 0) {
            return;
        }
        this.spawnCountdown = ATTEMPT_DELAY_BASE + this.random.nextInt(ATTEMPT_DELAY_SPREAD);
        Player witness = level.getNearestPlayer(this, PLAYER_RANGE);
        boolean watched = witness != null && this.hasLineOfSight(witness);
        if (!watched || CultistPortals.cultistsNear(this, CULTIST_RANGE) >= cultistCap(level)) {
            return;
        }
        level.broadcastEntityEvent(this, CultistPortals.PULSE_EVENT);
        EntityCultist minion = CultistPortals.rollMinion(level, this.random);
        if (minion == null) {
            return;
        }
        CultistPortals.summon(this, level, minion);
        this.hurtServer(level, level.damageSources().fellOutOfWorld(), SPAWN_DAMAGE_BASE + this.random.nextInt(SPAWN_DAMAGE_SPREAD));
    }

    private static int cultistCap(ServerLevel level) {
        return switch (level.getDifficulty()) {
            case HARD -> HARD_CULTIST_CAP;
            case NORMAL -> NORMAL_CULTIST_CAP;
            default -> LOW_CULTIST_CAP;
        };
    }
}
