package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.ThaumaturgeEntityTypeTags;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthHelper;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityThaumaturgeBoss extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ANGER = SynchedEntityData.defineId(EntityThaumaturgeBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_EMERGENCE = SynchedEntityData.defineId(EntityThaumaturgeBoss.class, EntityDataSerializers.INT);
    private static final int CALM = 0;
    private static final int SETTLED = 0;
    private static final int FIRST_EXTRA_PLAYER = 1;
    private static final boolean DESPAWNS_WHEN_FAR = false;
    private static final boolean PICKS_UP_LOOT = false;
    private static final int KILL_EXPERIENCE = 50;
    private static final double KNOCKBACK_RESISTANCE = 0.95;
    private static final double FOLLOW_RANGE = 40.0;
    private static final int HOME_RADIUS = 24;
    private static final int PASSIVE_HEAL_INTERVAL = 30;
    private static final float DEFAULT_PASSIVE_HEALING = 1.0F;
    private static final int AGGRO_REVIEW_INTERVAL = 20;
    private static final double AGGRO_RANGE = 128.0;
    private static final double AGGRO_RANGE_SQR = AGGRO_RANGE * AGGRO_RANGE;
    private static final int AGGRO_LEAD = 25;
    private static final double AGGRO_RATIO = 1.1;
    private static final int MAX_SCALING_SLOTS = 5;
    private static final double HEALTH_PER_SLOT = 50.0;
    private static final double DAMAGE_PER_SLOT = 0.5;
    private static final String HEALTH_BUFF_PREFIX = "boss_hp_buff_";
    private static final String DAMAGE_BUFF_PREFIX = "boss_dmg_buff_";
    private static final Identifier[] HEALTH_BUFFS = buffIds(HEALTH_BUFF_PREFIX);
    private static final Identifier[] DAMAGE_BUFFS = buffIds(DAMAGE_BUFF_PREFIX);

    protected final BossBar bossBar = new BossBar(this);
    protected int spawnTimer;
    private final BossRage rage = new BossRage(this, DATA_ANGER);
    private final Int2IntOpenHashMap aggro = new Int2IntOpenHashMap();

    public EntityThaumaturgeBoss(EntityType<? extends EntityThaumaturgeBoss> type, Level level) {
        super(type, level);
        this.xpReward = KILL_EXPERIENCE;
    }

    public static AttributeSupplier.Builder createBossAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE).add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE);
    }

    @Override
    public boolean considersEntityAsAlly(Entity other) {
        if (other.getType().builtInRegistryHolder().is(ThaumaturgeEntityTypeTags.ELDRITCH)) {
            return true;
        }
        return super.considersEntityAsAlly(other);
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return DESPAWNS_WHEN_FAR;
    }

    @Override
    public boolean isPushable() {
        if (isEmerging()) {
            return false;
        }
        return super.isPushable();
    }

    @Override
    public boolean canPickUpLoot() {
        return PICKS_UP_LOOT;
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 multiplier) {
        if (!state.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(state, multiplier);
        }
    }

    private static Identifier[] buffIds(String prefix) {
        Identifier[] ids = new Identifier[MAX_SCALING_SLOTS];
        for (int slot = 0; slot < MAX_SCALING_SLOTS; slot++) {
            ids[slot] = TTIds.rl(prefix + slot);
        }
        return ids;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        setHomeTo(blockPosition(), HOME_RADIUS);
        assignTitle();
        bossBar.rename();
        return groupData;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ANGER, CALM);
        builder.define(DATA_EMERGENCE, SETTLED);
    }

    public int rageTicks() {
        return rage.anger();
    }

    public void setRageTicks(int ticks) {
        rage.setAnger(ticks);
    }

    public int emergenceTicks() {
        return level().isClientSide() ? entityData.get(DATA_EMERGENCE) : spawnTimer;
    }

    protected boolean holdsStill() {
        return isEmerging();
    }

    protected boolean usesLegacyEnrage() {
        return true;
    }

    protected float passiveHealing() {
        return DEFAULT_PASSIVE_HEALING;
    }

    public void assignTitle() {}

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossBar.show(player);
    }

    private void dropDeadTarget() {
        LivingEntity target = getTarget();
        if (target != null && !target.isAlive()) {
            setTarget(null);
        }
    }

    private void setAiGate(boolean open) {
        goalSelector.setControlFlag(Goal.Flag.MOVE, open);
        goalSelector.setControlFlag(Goal.Flag.LOOK, open);
        targetSelector.setControlFlag(Goal.Flag.TARGET, open);
        if (!open) {
            getNavigation().stop();
        }
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        bossBar.rename();
    }

    private boolean isEmerging() {
        return emergenceTicks() > 0;
    }

    private boolean isDue(int interval) {
        return tickCount % interval == 0;
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        boolean shielded = isEmerging() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        return shielded || super.isInvulnerableTo(level, source);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.hide(player);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        bossBar.update();
        dropDeadTarget();
        setAiGate(!holdsStill());
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel level) {
            runServerCadence(level);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        float dealt = usesLegacyEnrage() ? rage.absorb(source, damage) : damage;
        boolean hurt = super.hurtServer(level, source, dealt);
        if (hurt) {
            recordHit(source, damage);
        }
        return hurt;
    }

    private void runServerCadence(ServerLevel level) {
        rage.tick();
        if (spawnTimer > 0) {
            spawnTimer--;
        }
        entityData.set(DATA_EMERGENCE, Math.max(SETTLED, spawnTimer));
        float healing = passiveHealing();
        if (healing > 0.0F && isAlive() && isDue(PASSIVE_HEAL_INTERVAL)) {
            heal(healing);
        }
        if (isDue(AGGRO_REVIEW_INTERVAL)) {
            reviewAggro(level);
            if (getTarget() != null && !LabyrinthHelper.isLabyrinthBound(this)) {
                scaleForPlayers(level);
            }
        }
    }

    private void recordHit(DamageSource source, float damage) {
        if (source.getEntity() instanceof LivingEntity attacker && attacker != this) {
            aggro.addTo(attacker.getId(), (int) damage);
        }
    }

    private void reviewAggro(ServerLevel level) {
        aggro.int2IntEntrySet().removeIf(entry -> isStaleAttacker(level.getEntity(entry.getIntKey())));
        LivingEntity target = getTarget();
        if (target == null || aggro.isEmpty()) {
            return;
        }
        int leaderId = target.getId();
        int leaderThreat = aggro.get(leaderId);
        for (Int2IntMap.Entry entry : aggro.int2IntEntrySet()) {
            if (entry.getIntValue() > leaderThreat) {
                leaderId = entry.getIntKey();
                leaderThreat = entry.getIntValue();
            }
        }
        if (leaderId == target.getId()) {
            return;
        }
        int currentThreat = aggro.get(target.getId());
        boolean clearLead = leaderThreat > currentThreat + AGGRO_LEAD && leaderThreat > currentThreat * AGGRO_RATIO;
        if (clearLead && level.getEntity(leaderId) instanceof LivingEntity challenger && canAttack(challenger)) {
            setTarget(challenger);
        }
    }

    private boolean isStaleAttacker(@Nullable Entity attacker) {
        if (!(attacker instanceof LivingEntity living) || !living.isAlive() || living.isRemoved() || living.level() != level()) {
            return true;
        }
        if (living instanceof Player player && (player.isSpectator() || player.isCreative())) {
            return true;
        }
        return distanceToSqr(living) > AGGRO_RANGE_SQR;
    }

    private void scaleForPlayers(ServerLevel level) {
        int players = 0;
        for (int attackerId : aggro.keySet()) {
            if (level.getEntity(attackerId) instanceof Player) {
                players++;
            }
        }
        applyScalingSlots(Mth.clamp(players - FIRST_EXTRA_PLAYER, 0, MAX_SCALING_SLOTS));
    }

    private void applyScalingSlots(int slots) {
        float fraction = getMaxHealth() > 0.0F ? getHealth() / getMaxHealth() : 1.0F;
        boolean healthChanged = applySlots(Attributes.MAX_HEALTH, HEALTH_BUFFS, HEALTH_PER_SLOT, slots);
        applySlots(Attributes.ATTACK_DAMAGE, DAMAGE_BUFFS, DAMAGE_PER_SLOT, slots);
        if (healthChanged) {
            rebalanceHealth(fraction);
        }
    }

    private boolean applySlots(Holder<Attribute> attribute, Identifier[] ids, double amount, int slots) {
        AttributeInstance instance = getAttribute(attribute);
        if (instance == null) {
            return false;
        }
        boolean changed = false;
        for (int slot = 0; slot < MAX_SCALING_SLOTS; slot++) {
            boolean wanted = slot < slots;
            changed |= instance.hasModifier(ids[slot]) != wanted;
            syncModifier(instance, ids[slot], amount, wanted);
        }
        return changed;
    }

    private void rebalanceHealth(float fraction) {
        if (isAlive()) {
            setHealth(fraction * getMaxHealth());
        }
    }

    private static void syncModifier(AttributeInstance instance, Identifier id, double amount, boolean wanted) {
        if (instance.hasModifier(id) == wanted) {
            return;
        }
        if (wanted) {
            instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        } else {
            instance.removeModifier(id);
        }
    }
}
