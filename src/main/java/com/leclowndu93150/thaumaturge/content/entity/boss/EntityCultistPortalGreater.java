package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.content.decor.banner.BannerStandingBlock;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultist;
import com.leclowndu93150.thaumaturge.content.entity.portal.CultistPortals;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityCultistPortalGreater extends EntityThaumaturgeBoss {
    private static final String STAGE_KEY = "stage";
    private static final double MAX_HEALTH = 500.0;
    private static final double ATTACK_DAMAGE = 0.0;
    private static final double ARMOR = 5.0;
    private static final double KNOCKBACK_RESISTANCE = 1.0;
    private static final int EXPERIENCE = 30;
    private static final float TOUCH_DAMAGE = 8.0F;
    private static final float DEATH_BLAST_POWER = 2.0F;
    private static final int OPENING_TICKS = 200;
    private static final int BANNER_COUNTDOWN = 190;
    private static final int CRATE_FIRST_COUNTDOWN = 120;
    private static final int CRATE_LAST_COUNTDOWN = 20;
    private static final int CRATE_INTERVAL = 13;
    private static final double PLAYER_RANGE = 48.0;
    private static final int RECHECK_MIN_TICKS = 30;
    private static final int RECHECK_SPREAD_TICKS = 30;
    private static final int[] EARLY_DELAY_MIN = {15, 14, 13, 12, 11};
    private static final int[] EARLY_DELAY_MAX = {24, 22, 20, 18, 16};
    private static final int POPULATION_STAGE = EARLY_DELAY_MIN.length;
    private static final int LEADER_STAGE = 12;
    private static final double POPULATION_RANGE = 32.0;
    private static final int POPULATION_DELAY_PER_CULTIST = 20;
    private static final int POPULATION_EXTRA_SPREAD = 5;
    private static final int POPULATION_DIVISOR = 3;
    private static final int LEADER_DELAY_BASE = 50;
    private static final int LEADER_POPULATION_FACTOR = 2;
    private static final int LEADER_DELAY_SPREAD = 50;
    private static final int DRAIN_MIN = 5;
    private static final int DRAIN_SPREAD = 5;
    private static final float HEAL_PER_TICK = 1.0F;
    private static final int HOME_RADIUS = 32;
    private static final int BANNER_DISTANCE = 6;
    private static final int CRATE_RADIUS = 4;
    private static final float RARE_CHANCE = 0.05F;
    private static final float UNCOMMON_CHANCE = 0.15F;
    private static final int BOLT_COLOR = 0xBB2222;
    private static final float CHIME_VOLUME = 1.0F;
    private static final float CHIME_PITCH = 1.0F;
    private static final int AMBIENT_SOUND_INTERVAL = CultistPortals.AMBIENT_INTERVAL;
    private static final boolean PUSHABLE = false;
    private static final Direction[] CARDINALS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    public int pulse;
    private int stage;
    private int countdown = OPENING_TICKS;
    private boolean opening = true;

    public EntityCultistPortalGreater(EntityType<? extends EntityCultistPortalGreater> type, Level level) {
        super(type, level);
        this.xpReward = EXPERIENCE;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE).add(Attributes.ARMOR, ARMOR).add(Attributes.KNOCKBACK_RESISTANCE,
                KNOCKBACK_RESISTANCE);
    }

    @Override
    protected void registerGoals() {}

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putInt(STAGE_KEY, stage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        stage = in.getIntOr(STAGE_KEY, 0);
    }

    @Override
    public boolean isPushable() {
        return PUSHABLE;
    }

    @Override
    public void move(MoverType kind, Vec3 delta) {}

    @Override
    public void aiStep() {
        pulse = Math.max(0, pulse - 1);
    }

    @Override
    public void tick() {
        super.tick();
        if (!isAlive() || !(level() instanceof ServerLevel server)) {
            return;
        }
        if (stage < LEADER_STAGE) {
            heal(HEAL_PER_TICK);
        }
        tickWaves(server);
    }

    private void tickWaves(ServerLevel level) {
        if (countdown > 0) {
            if (opening && stage == 0) {
                openingEvents(level);
            }
            countdown--;
            return;
        }
        opening = false;
        if (level.getNearestPlayer(this, PLAYER_RANGE) == null) {
            countdown = RECHECK_MIN_TICKS + random.nextInt(RECHECK_SPREAD_TICKS);
            return;
        }
        pulse(level);
        runStage(level);
        stage++;
    }

    private void openingEvents(ServerLevel level) {
        if (countdown == BANNER_COUNTDOWN) {
            placeBanners(level);
        } else if (countdown <= CRATE_FIRST_COUNTDOWN && countdown >= CRATE_LAST_COUNTDOWN && (CRATE_FIRST_COUNTDOWN - countdown) % CRATE_INTERVAL == 0) {
            placeCrate(level);
        }
    }

    private void runStage(ServerLevel level) {
        if (stage < POPULATION_STAGE) {
            spawnMinion(level);
            countdown = EARLY_DELAY_MIN[stage] + random.nextInt(EARLY_DELAY_MAX[stage] - EARLY_DELAY_MIN[stage] + 1);
        } else if (stage == LEADER_STAGE) {
            spawnLeader(level);
            countdown = LEADER_DELAY_BASE + LEADER_POPULATION_FACTOR * populationDelay() + random.nextInt(LEADER_DELAY_SPREAD);
        } else {
            spawnMinion(level);
            int population = populationDelay();
            countdown = population + random.nextInt(POPULATION_EXTRA_SPREAD) + population / POPULATION_DIVISOR - 1;
            if (stage > LEADER_STAGE) {
                hurtServer(level, damageSources().fellOutOfWorld(), DRAIN_MIN + random.nextInt(DRAIN_SPREAD));
            }
        }
    }

    private int populationDelay() {
        return POPULATION_DELAY_PER_CULTIST * CultistPortals.cultistsNear(this, POPULATION_RANGE);
    }

    private void spawnMinion(ServerLevel level) {
        EntityCultist minion = CultistPortals.rollMinion(level, random);
        if (minion != null) {
            arrive(level, minion);
        }
    }

    private void spawnLeader(ServerLevel level) {
        EntityCultistLeader leader = TTEntities.CULTIST_LEADER.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (leader != null) {
            arrive(level, leader);
        }
    }

    private void arrive(ServerLevel level, Mob arrival) {
        arrival.setHomeTo(blockPosition(), HOME_RADIUS);
        CultistPortals.summon(this, level, arrival);
        arrival.setHomeTo(blockPosition(), HOME_RADIUS);
        chime(level);
    }

    private void placeBanners(ServerLevel level) {
        BlockPos base = blockPosition();
        for (Direction side : CARDINALS) {
            BlockPos pos = base.relative(side, BANNER_DISTANCE);
            if (!level.hasChunkAt(pos)) {
                continue;
            }
            float facingYaw = side.getOpposite().toYRot();
            BlockState banner = TTBlocks.BANNER_CRIMSON_CULT.get().defaultBlockState().setValue(BannerStandingBlock.ROTATION, RotationSegment.convertToSegment(facingYaw));
            level.setBlock(pos, banner, Block.UPDATE_ALL);
            bolt(level, Vec3.atCenterOf(pos));
        }
        chime(level);
        pulse(level);
    }

    private void placeCrate(ServerLevel level) {
        int dx = random.nextInt(CRATE_RADIUS * 2 + 1) - CRATE_RADIUS;
        int dz = random.nextInt(CRATE_RADIUS * 2 + 1) - CRATE_RADIUS;
        BlockPos pos = blockPosition().offset(dx, 0, dz);
        if (dx == 0 || dz == 0 || !level.hasChunkAt(pos) || !level.isEmptyBlock(pos)) {
            return;
        }
        level.setBlock(pos, crateState(), Block.UPDATE_ALL);
        bolt(level, Vec3.atCenterOf(pos));
        chime(level);
        pulse(level);
    }

    private BlockState crateState() {
        float roll = random.nextFloat();
        if (roll < RARE_CHANCE) {
            return TTBlocks.LOOT_CRATE_RARE.get().defaultBlockState();
        }
        return roll < RARE_CHANCE + UNCOMMON_CHANCE ? TTBlocks.LOOT_CRATE_UNCOMMON.get().defaultBlockState() : TTBlocks.LOOT_CRATE_COMMON.get().defaultBlockState();
    }

    private void bolt(ServerLevel level, Vec3 target) {
        Effects.boltStrike(level, getBoundingBox().getCenter()).to(target).color(BOLT_COLOR).send();
    }

    private void chime(ServerLevel level) {
        level.playSound(null, getX(), getY(), getZ(), TTSounds.WANDFAIL.get(), SoundSource.HOSTILE, CHIME_VOLUME, CHIME_PITCH);
    }

    private void pulse(ServerLevel level) {
        level.broadcastEntityEvent(this, CultistPortals.PULSE_EVENT);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event != CultistPortals.PULSE_EVENT) {
            super.handleEntityEvent(event);
            return;
        }
        pulse = CultistPortals.PULSE_TICKS;
    }

    @Override
    public boolean addEffect(MobEffectInstance effect, @Nullable Entity origin) {
        return false;
    }

    @Override
    public void playerTouch(Player player) {
        CultistPortals.touch(this, player, TOUCH_DAMAGE);
    }

    @Override
    public void die(DamageSource cause) {
        CultistPortals.collapse(this, DEATH_BLAST_POWER);
        super.die(cause);
    }

    @Override
    public int getAmbientSoundInterval() {
        return AMBIENT_SOUND_INTERVAL;
    }

    @Override
    protected float getSoundVolume() {
        return CultistPortals.SOUND_VOLUME;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TTSounds.MONOLITH.value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource cause) {
        return TTSounds.ZAP.value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TTSounds.SHOCK.value();
    }
}
