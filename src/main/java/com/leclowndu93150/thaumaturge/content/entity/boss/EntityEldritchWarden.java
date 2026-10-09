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
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityEldritchWarden extends EntityThaumaturgeBoss implements RangedAttackMob {
    private static final String TITLE_KEY = "warden_title";
    private static final String NAME_KEY = "entity.thaumaturge.eldritch_warden.name.custom";
    private static final String TITLE_SEPARATOR = ";";
    private static final String TITLE_ROSTER = "Aphoom-Zhah;Basatan;Chaugnar Faugn;Mnomquah;Nyogtha;Oorn;Shaikorth;Rhan-Tegoth;Rhogog;Shudde M'ell;Vulthoom;Yag-Kosha;Yibb-Tstll;Zathog;Zushakon";
    private static final BossTitles TITLES = new BossTitles(NAME_KEY, List.of(TITLE_ROSTER.split(TITLE_SEPARATOR)));
    private static final byte EVENT_EMERGENCE = 18;
    private static final int EMERGENCE_TICKS = 150;
    private static final int SHROUD_INTERVAL = 4;
    private static final int SHROUD_SPIRALS = 33;
    private static final int SHROUD_COLOR = 0x22112F;
    private static final float SHROUD_MIN_RADIUS = 1.0F;
    private static final int FULL_CIRCLE_DEGREES = 360;
    private static final double TRAIL_OFFSET = 0.25;
    private static final double MAX_HEALTH = 400.0;
    private static final double MOVEMENT_SPEED = 0.33;
    private static final double ATTACK_DAMAGE = 10.0;
    private static final double ARMOR = 4.0;
    private static final int AMBIENT_INTERVAL = 500;
    private static final int FLOAT_PRIORITY = 0;
    private static final int RANGED_PRIORITY = 1;
    private static final int MELEE_PRIORITY = 2;
    private static final int HOME_PRIORITY = 6;
    private static final int STROLL_PRIORITY = 7;
    private static final int LOOK_PRIORITY = 8;
    private static final int RETALIATE_PRIORITY = 1;
    private static final int PLAYER_TARGET_PRIORITY = 2;
    private static final double RANGED_SPEED = 1.0;
    private static final double RANGED_MIN_DISTANCE = 3.0;
    private static final int RANGED_MIN_INTERVAL = 20;
    private static final int RANGED_MAX_INTERVAL = 40;
    private static final float RANGED_RADIUS = 24.0F;
    private static final double MELEE_SPEED = 1.1;
    private static final double HOME_SPEED = 0.8;
    private static final double STROLL_SPEED = 1.0;
    private static final float LOOK_RANGE = 8.0F;
    private static final float ORB_CHANCE = 0.8F;
    private static final double ORB_DROP = 0.13;
    private static final float ORB_SPEED = 1.0F;
    private static final float ORB_INACCURACY = 2.0F;
    private static final float ORB_VOLUME = 2.0F;
    private static final float SCREECH_VOLUME = 4.0F;
    private static final float BASE_PITCH = 1.0F;
    private static final float PITCH_SPREAD = 0.1F;
    private static final double SCREECH_PUSH = 1.5;
    private static final double SCREECH_LIFT = 0.1;
    private static final int SCREECH_EFFECT_TICKS = 400;
    private static final int SCREECH_WARP_BASE = 3;
    private static final int SCREECH_WARP_SPREAD = 3;

    private record TrailCorner(int x, int z) {
    }

    private static final List<TrailCorner> TRAIL_CORNERS = List.of(new TrailCorner(-1, -1), new TrailCorner(-1, 1), new TrailCorner(1, -1), new TrailCorner(1, 1));

    private record GoalSlot(int priority, Function<EntityEldritchWarden, Goal> factory) {
    }

    private static final List<GoalSlot> BEHAVIOUR = List.of(new GoalSlot(FLOAT_PRIORITY, FloatGoal::new),
            new GoalSlot(RANGED_PRIORITY, warden -> new LongRangeAttackGoal(warden, RANGED_SPEED, RANGED_MIN_DISTANCE, RANGED_MIN_INTERVAL, RANGED_MAX_INTERVAL, RANGED_RADIUS)),
            new GoalSlot(MELEE_PRIORITY, EntityEldritchWarden::clawGoal), new GoalSlot(HOME_PRIORITY, EntityEldritchWarden::homeGoal), new GoalSlot(STROLL_PRIORITY, EntityEldritchWarden::wanderGoal),
            new GoalSlot(LOOK_PRIORITY, EntityEldritchWarden::gazeGoal));
    private static final List<Class<? extends LivingEntity>> PREY_KINDS = List.of(Player.class, EntityCultist.class);

    private final CastingArms arms = new CastingArms(this);
    private final WardenShield shield = new WardenShield(this);
    private final RingPhase ring = new RingPhase(this, arms);
    private final BlockPos.MutableBlockPos trailCursor = new BlockPos.MutableBlockPos();
    private int title;
    private boolean emergenceAnnounced;

    public EntityEldritchWarden(EntityType<? extends EntityEldritchWarden> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        AttributeSupplier.Builder builder = createBossAttributes();
        builder.add(Attributes.ARMOR, ARMOR);
        builder.add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE);
        builder.add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED);
        builder.add(Attributes.MAX_ABSORPTION, WardenShield.capacityFor(MAX_HEALTH));
        return builder.add(Attributes.MAX_HEALTH, MAX_HEALTH);
    }

    @Override
    protected void registerGoals() {
        for (GoalSlot slot : BEHAVIOUR) {
            goalSelector.addGoal(slot.priority(), slot.factory().apply(this));
        }
        goalSelector.addGoal(LOOK_PRIORITY, new RandomLookAroundGoal(this));
        targetSelector.addGoal(RETALIATE_PRIORITY, new HurtByTargetGoal(this));
        for (int rank = 0; rank < PREY_KINDS.size(); rank++) {
            targetSelector.addGoal(PLAYER_TARGET_PRIORITY + rank, preyGoal(PREY_KINDS.get(rank)));
        }
    }

    public CastingArms arms() {
        return arms;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        spawnTimer = EMERGENCE_TICKS;
        title = TITLES.roll(getRandom());
        shield.raise();
        ChampionHelper.makeChampion(this, true);
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    public void assignTitle() {
        Optional<Component> display = MobTraits.champion(this).map(MobTraitNames::of).map(label -> TITLES.name(title, label));
        display.ifPresent(this::setCustomName);
    }

    @Override
    public void tick() {
        announceEmergence();
        super.tick();
        if (level() instanceof ServerLevel server) {
            tickServerSide(server);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        lockAiDuringRing();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level() instanceof ServerLevel server) {
            if (isAlive()) {
                layTrail(server);
            }
        } else {
            arms.relax();
        }
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        boolean piercing = source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        return ring.active() && !piercing || super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (isRejectedDamage(source) || !super.hurtServer(level, source, damage)) {
            return false;
        }
        if (isAlive() && shield.broken()) {
            ring.ignite();
        }
        return true;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!(level() instanceof ServerLevel level) || spawnTimer > 0 || ring.active()) {
            return;
        }
        boolean orbRoll = random.nextFloat() < ORB_CHANCE;
        if (orbRoll) {
            fireOrb(level, target);
            return;
        }
        if (hasLineOfSight(target)) {
            screech(level, target);
        }
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == EVENT_EMERGENCE) {
            spawnTimer = EMERGENCE_TICKS;
        } else if (!arms.receive(event)) {
            super.handleEntityEvent(event);
        }
    }

    @Override
    public int getAmbientSoundInterval() {
        return AMBIENT_INTERVAL;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TTSounds.EGIDLE.value();
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        title = TITLES.clamp(input.getByteOr(TITLE_KEY, (byte) 0));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof EntityEldritchGuardian) && super.canAttack(target);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte(TITLE_KEY, (byte) title);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TTSounds.EGDEATH.value();
    }

    private <T extends LivingEntity> Goal preyGoal(Class<T> kind) {
        return new NearestAttackableTargetGoal<>(this, kind, true);
    }

    private static Goal clawGoal(EntityEldritchWarden warden) {
        return new MeleeAttackGoal(warden, MELEE_SPEED, false);
    }

    private static Goal homeGoal(EntityEldritchWarden warden) {
        return new MoveTowardsRestrictionGoal(warden, HOME_SPEED);
    }

    private static Goal wanderGoal(EntityEldritchWarden warden) {
        return new WaterAvoidingRandomStrollGoal(warden, STROLL_SPEED);
    }

    private static Goal gazeGoal(EntityEldritchWarden warden) {
        return new LookAtPlayerGoal(warden, Player.class, LOOK_RANGE);
    }

    private void announceEmergence() {
        if (emergenceAnnounced || spawnTimer <= 0 || !(level() instanceof ServerLevel server)) {
            return;
        }
        emergenceAnnounced = true;
        server.broadcastEntityEvent(this, EVENT_EMERGENCE);
    }

    private void tickServerSide(ServerLevel server) {
        boolean shroudDue = spawnTimer > 0 && spawnTimer % SHROUD_INTERVAL == 0;
        if (shroudDue) {
            emitShroud(server);
        }
        shield.tick();
        ring.tick(server);
    }

    private void lockAiDuringRing() {
        if (!ring.active()) {
            return;
        }
        targetSelector.setControlFlag(Goal.Flag.TARGET, false);
        goalSelector.setControlFlag(Goal.Flag.LOOK, false);
        goalSelector.setControlFlag(Goal.Flag.MOVE, false);
        getNavigation().stop();
    }

    private static boolean isRejectedDamage(DamageSource source) {
        return source.is(DamageTypes.WITHER) || source.is(DamageTypeTags.IS_DROWNING) || source.is(DamageTypeTags.WITHER_IMMUNE_TO);
    }

    private void fireOrb(ServerLevel level, LivingEntity target) {
        Vec3 hand = arms.castFromNextHand();
        EntityEldritchOrb orb = new EntityEldritchOrb(level, this);
        orb.setPos(getX() + hand.x, getEyeY() - ORB_DROP, getZ() + hand.z);
        Vec3 aim = LeadingAim.at(this, target);
        orb.shoot(aim.x, aim.y, aim.z, ORB_SPEED, ORB_INACCURACY);
        level.addFreshEntity(orb);
        level.playSound(null, getX(), getY(), getZ(), TTSounds.EGATTACK.get(), SoundSource.HOSTILE, ORB_VOLUME, BASE_PITCH + random.nextFloat() * PITCH_SPREAD);
    }

    private void screech(ServerLevel level, LivingEntity target) {
        Vec3 facing = Vec3.directionFromRotation(0.0F, getYRot());
        target.push(facing.x * SCREECH_PUSH, SCREECH_LIFT, facing.z * SCREECH_PUSH);
        target.hurtMarked = true;
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, SCREECH_EFFECT_TICKS, 0));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, SCREECH_EFFECT_TICKS, 0));
        if (target instanceof ServerPlayer player) {
            WarpHelper.addWarp(player, SCREECH_WARP_BASE + random.nextInt(SCREECH_WARP_SPREAD), WarpType.TEMPORARY);
        }
        level.playSound(null, getX(), getY(), getZ(), TTSounds.EGSCREECH.get(), SoundSource.HOSTILE, SCREECH_VOLUME, BASE_PITCH + random.nextFloat() * PITCH_SPREAD);
    }

    private void emitShroud(ServerLevel level) {
        float radius = Math.max(SHROUD_MIN_RADIUS, getBbHeight() * (EMERGENCE_TICKS - spawnTimer) / EMERGENCE_TICKS);
        for (int spiral = 0; spiral < SHROUD_SPIRALS; spiral++) {
            spawnSpiral(level, radius, spiral * FULL_CIRCLE_DEGREES / SHROUD_SPIRALS);
        }
    }

    private void spawnSpiral(ServerLevel level, float radius, int startDegrees) {
        Effects.spiralSmoke(level, position()).radius(radius).start(startDegrees).minY(Mth.floor(getY())).color(SHROUD_COLOR).send();
    }

    private void layTrail(ServerLevel level) {
        BlockState sap = TTBlocks.EFFECT_SAP.get().defaultBlockState();
        for (TrailCorner corner : TRAIL_CORNERS) {
            trailCursor.set(getX() + corner.x() * TRAIL_OFFSET, getY(), getZ() + corner.z() * TRAIL_OFFSET);
            if (level.hasChunkAt(trailCursor) && level.getBlockState(trailCursor).isAir()) {
                level.setBlock(trailCursor, sap, Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer viewer) {
        super.stopSeenByPlayer(viewer);
        shield.hide(viewer);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer viewer) {
        super.startSeenByPlayer(viewer);
        shield.show(viewer);
    }
}
