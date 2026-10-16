package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultist;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchGuardian;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchOrb;
import com.leclowndu93150.thaumaturge.content.entity.ai.LongRangeAttackGoal;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.content.entity.eldritch.CastingArms;
import com.leclowndu93150.thaumaturge.content.entity.eldritch.LeadingAim;
import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitNames;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityEldritchWarden extends EntityThaumaturgeBoss implements RangedAttackMob {
    private static final String NAME_KEY = "entity.thaumaturge.eldritch_warden.name.custom";
    private static final List<String> TITLE_NAMES = List.of("Aphoom-Zhah", "Basatan", "Chaugnar Faugn", "Mnomquah", "Nyogtha", "Oorn", "Shaikorth", "Rhan-Tegoth", "Rhogog", "Shudde M'ell", "Vulthoom",
            "Yag-Kosha", "Yibb-Tstll", "Zathog", "Zushakon");
    private static final BossTitles TITLES = new BossTitles(NAME_KEY, TITLE_NAMES);
    private static final String TITLE_KEY = "warden_title";
    private static final String FRENZY_KEY = "frenzy_spent";
    private static final double MAX_HEALTH = 400.0;
    private static final double ATTACK_DAMAGE = 10.0;
    private static final double MOVEMENT_SPEED = 0.33;
    private static final double ARMOR = 4.0;
    private static final int EMERGENCE_TICKS = 150;
    private static final int FLOAT_PRIORITY = 0;
    private static final int RANGED_PRIORITY = 2;
    private static final int MELEE_PRIORITY = 3;
    private static final int HOME_PRIORITY = 5;
    private static final int STROLL_PRIORITY = 7;
    private static final int LOOK_PRIORITY = 8;
    private static final int RETALIATE_PRIORITY = 1;
    private static final int PLAYER_PRIORITY = 2;
    private static final int CULTIST_PRIORITY = 3;
    private static final double CAST_SPEED = 1.0;
    private static final double CAST_MIN_DISTANCE = 3.0;
    private static final int CAST_MIN_INTERVAL = 20;
    private static final int CAST_MAX_INTERVAL = 40;
    private static final float CAST_RADIUS = 24.0F;
    private static final double MELEE_SPEED = 1.1;
    private static final double HOME_SPEED = 0.8;
    private static final double STROLL_SPEED = 1.0;
    private static final float LOOK_DISTANCE = 8.0F;
    private static final float ORB_CHANCE = 0.8F;
    private static final float ORB_SPEED = 1.0F;
    private static final float ORB_INACCURACY = 2.0F;
    private static final float ORB_VOLUME = 2.0F;
    private static final double ORB_DROP = 0.13;
    private static final float SCREECH_VOLUME = 4.0F;
    private static final float PITCH_BASE = 1.0F;
    private static final float PITCH_SPREAD = 0.1F;
    private static final double SCREECH_SHOVE = 1.5;
    private static final double SCREECH_LIFT = 0.1;
    private static final int SCREECH_TICKS = 400;
    private static final int SCREECH_AMPLIFIER = 0;
    private static final int SCREECH_WARP_BASE = 3;
    private static final int SCREECH_WARP_SPREAD = 3;
    private static final int AMBIENT_INTERVAL = 500;
    private static final double TRAIL_CORNER = 0.25;
    private static final double[] TRAIL_OFFSETS = {-TRAIL_CORNER, TRAIL_CORNER};
    private static final int SMOKE_INTERVAL = 4;
    private static final int SMOKE_COUNT = 33;
    private static final int SMOKE_COLOR = 0x22112F;
    private static final double SMOKE_MIN_HEIGHT = 1.0;
    private static final int SMOKE_FLOOR_MARGIN = 1;
    private static final int FULL_TURN_DEGREES = 360;

    private final CastingArms arms = new CastingArms(this);
    private final WardenShield shield = new WardenShield(this);
    private final RingPhase rings = new RingPhase(this, arms);
    private int title;
    private boolean frenzySpent;

    public EntityEldritchWarden(EntityType<? extends EntityEldritchWarden> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE).add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED).add(Attributes.ARMOR, ARMOR)
                .add(Attributes.MAX_ABSORPTION, WardenShield.capacityFor(MAX_HEALTH));
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(FLOAT_PRIORITY, new FloatGoal(this));
        goalSelector.addGoal(RANGED_PRIORITY, new LongRangeAttackGoal(this, CAST_SPEED, CAST_MIN_DISTANCE, CAST_MIN_INTERVAL, CAST_MAX_INTERVAL, CAST_RADIUS));
        goalSelector.addGoal(MELEE_PRIORITY, new MeleeAttackGoal(this, MELEE_SPEED, false));
        goalSelector.addGoal(HOME_PRIORITY, new MoveTowardsRestrictionGoal(this, HOME_SPEED));
        goalSelector.addGoal(STROLL_PRIORITY, new RandomStrollGoal(this, STROLL_SPEED));
        goalSelector.addGoal(LOOK_PRIORITY, new LookAtPlayerGoal(this, Player.class, LOOK_DISTANCE));
        goalSelector.addGoal(LOOK_PRIORITY, new RandomLookAroundGoal(this));
        targetSelector.addGoal(RETALIATE_PRIORITY, new HurtByTargetGoal(this));
        targetSelector.addGoal(PLAYER_PRIORITY, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(CULTIST_PRIORITY, new NearestAttackableTargetGoal<>(this, EntityCultist.class, true));
    }

    public CastingArms arms() {
        return arms;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        ChampionHelper.makeChampion(this, true);
        title = TITLES.roll(getRandom());
        shield.raise();
        spawnTimer = EMERGENCE_TICKS;
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    public void assignTitle() {
        MobTraits.champion(this).ifPresent(trait -> setCustomName(TITLES.name(title, MobTraitNames.of(trait))));
    }

    @Override
    protected boolean holdsStill() {
        return super.holdsStill() || rings.active();
    }

    @Override
    public void tick() {
        super.tick();
        arms.relax();
        if (!(level() instanceof ServerLevel level) || !isAlive()) {
            return;
        }
        shield.tick();
        leaveSappingTrail(level);
        if (emergenceTicks() > 0) {
            riseFromSmoke(level);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        rings.tick(level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (rings.active()) {
            setDeltaMovement(0.0, getDeltaMovement().y, 0.0);
        }
    }

    private void leaveSappingTrail(ServerLevel level) {
        BlockState field = TTBlocks.EFFECT_SAP.get().defaultBlockState();
        for (double dx : TRAIL_OFFSETS) {
            for (double dz : TRAIL_OFFSETS) {
                BlockPos spot = BlockPos.containing(getX() + dx, getY(), getZ() + dz);
                if (level.isEmptyBlock(spot)) {
                    level.setBlockAndUpdate(spot, field);
                }
            }
        }
    }

    private void riseFromSmoke(ServerLevel level) {
        if (tickCount % SMOKE_INTERVAL != 0) {
            return;
        }
        double height = Math.max(SMOKE_MIN_HEIGHT, getBbHeight() * (EMERGENCE_TICKS - emergenceTicks()) / EMERGENCE_TICKS);
        Vec3 centre = position().add(0.0, height / 2.0, 0.0);
        int floor = Mth.floor(getBoundingBox().minY) - SMOKE_FLOOR_MARGIN;
        RandomSource random = getRandom();
        for (int puff = 0; puff < SMOKE_COUNT; puff++) {
            Effects.spiralSmoke(level, centre).radius((float) height).start(random.nextInt(FULL_TURN_DEGREES)).minY(floor).color(SMOKE_COLOR).send();
        }
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypes.DROWN) || source.is(DamageTypes.WITHER) || source.is(DamageTypeTags.WITHER_IMMUNE_TO)) {
            return true;
        }
        if (rings.active() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean shielded = !shield.broken();
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && shielded && shield.broken() && !frenzySpent && isAlive()) {
            frenzySpent = true;
            rings.begin(level);
        }
        return hurt;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (holdsStill() || !(level() instanceof ServerLevel level)) {
            return;
        }
        if (getRandom().nextFloat() < ORB_CHANCE) {
            castOrb(level, target);
        } else if (hasLineOfSight(target)) {
            screech(level, target);
        }
    }

    private void castOrb(ServerLevel level, LivingEntity target) {
        Vec3 hand = arms.castFromNextHand();
        EntityEldritchOrb orb = new EntityEldritchOrb(level, this);
        orb.setPos(orb.getX() + hand.x, orb.getY() - ORB_DROP, orb.getZ() + hand.z);
        Vec3 aim = LeadingAim.at(orb, target);
        orb.shoot(aim.x, aim.y, aim.z, ORB_SPEED, ORB_INACCURACY);
        level.addFreshEntity(orb);
        playLoud(level, TTSounds.EGATTACK.get(), ORB_VOLUME);
    }

    private void screech(ServerLevel level, LivingEntity target) {
        float yaw = getYRot() * Mth.DEG_TO_RAD;
        target.setDeltaMovement(target.getDeltaMovement().add(-Mth.sin(yaw) * SCREECH_SHOVE, SCREECH_LIFT, Mth.cos(yaw) * SCREECH_SHOVE));
        target.hurtMarked = true;
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, SCREECH_TICKS, SCREECH_AMPLIFIER), this);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, SCREECH_TICKS, SCREECH_AMPLIFIER), this);
        if (target instanceof ServerPlayer player) {
            WarpHelper.addWarp(player, SCREECH_WARP_BASE + getRandom().nextInt(SCREECH_WARP_SPREAD), WarpType.TEMPORARY);
        }
        playLoud(level, TTSounds.EGSCREECH.get(), SCREECH_VOLUME);
    }

    private void playLoud(ServerLevel level, SoundEvent sound, float volume) {
        if (isSilent()) {
            return;
        }
        level.playSound(null, getX(), getY(), getZ(), sound, SoundSource.HOSTILE, volume, PITCH_BASE + getRandom().nextFloat() * PITCH_SPREAD);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (!arms.receive(id)) {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof EntityEldritchGuardian) && super.canAttack(target);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TTSounds.EGIDLE.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return AMBIENT_INTERVAL;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TTSounds.EGDEATH.get();
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        shield.show(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        shield.hide(player);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte(TITLE_KEY, (byte) title);
        output.putBoolean(FRENZY_KEY, frenzySpent);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        title = input.getString(TITLE_KEY).map(TITLE_NAMES::indexOf).filter(index -> index >= 0).orElseGet(() -> TITLES.clamp(input.getIntOr(TITLE_KEY, 0)));
        frenzySpent = input.getBooleanOr(FRENZY_KEY, false);
    }
}
