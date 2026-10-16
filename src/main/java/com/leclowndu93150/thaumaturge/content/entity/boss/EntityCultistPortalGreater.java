package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.content.decor.banner.BannerStandingBlock;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.portal.CultistPortals;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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

public class EntityCultistPortalGreater extends EntityThaumaturgeBoss {
    private static final String STAGE_KEY = "stage";
    private static final double MAX_HEALTH = 500.0;
    private static final double ARMOR = 5.0;
    private static final double KNOCKBACK_RESISTANCE = 1.0;
    private static final int EXPERIENCE = 30;
    private static final float TOUCH_DAMAGE = 8.0F;
    private static final float DEATH_BLAST_POWER = 2.0F;
    private static final float REGEN_PER_TICK = 1.0F;
    private static final int LAST_HEALING_WAVE = 12;
    private static final int PRAETOR_WAVE = 13;
    private static final int RUSH_WAVES = 5;
    private static final int OPENING_TICKS = 200;
    private static final int BANNER_TICK = 40;
    private static final int BANNER_DISTANCE = 6;
    private static final int CRATE_FIRST_TICK = 57;
    private static final int CRATE_LAST_TICK = 174;
    private static final int CRATE_INTERVAL = 13;
    private static final int CRATE_REACH = 4;
    private static final float RARE_CRATE_CHANCE = 0.05F;
    private static final float UNCOMMON_CRATE_CHANCE = 0.15F;
    private static final int ARC_COLOR = 0xBB2222;
    private static final double BLOCK_CENTER = 0.5;
    private static final double PORTAL_CORE = 0.5;
    private static final double WAVE_PLAYER_RANGE = 48.0;
    private static final int IDLE_RECHECK_MIN = 30;
    private static final int IDLE_RECHECK_SPREAD = 30;
    private static final double CROWD_RANGE = 32.0;
    private static final int ARRIVAL_HOME_RADIUS = 32;
    private static final int TICKS_PER_SECOND = 20;
    private static final float CONJURE_VOLUME = 1.0F;
    private static final float CONJURE_PITCH = 1.0F;
    private static final int RUSH_MIN_START = 15;
    private static final int RUSH_MAX_START = 24;
    private static final int RUSH_MIN_STEP = 1;
    private static final int RUSH_MAX_STEP = 2;
    private static final int CROWD_EXTRA_BASE = 5;
    private static final int CROWD_EXTRA_DIVISOR = 3;
    private static final int PRAETOR_PAUSE_BASE = 50;
    private static final int PRAETOR_CROWD_FACTOR = 2;
    private static final int PRAETOR_PAUSE_SPREAD = 50;
    private static final int BURN_DAMAGE_BASE = 5;
    private static final int BURN_DAMAGE_SPREAD = 5;

    public int pulse;

    private int stage;
    private int clock;
    private int waveCountdown;

    public EntityCultistPortalGreater(EntityType<? extends EntityCultistPortalGreater> type, Level level) {
        super(type, level);
        this.xpReward = EXPERIENCE;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.ARMOR, ARMOR).add(Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE);
    }

    @Override
    protected void registerGoals() {}

    @Override
    public void tick() {
        super.tick();
        if (this.pulse > 0) {
            this.pulse--;
        }
        if (this.level() instanceof ServerLevel server && this.isAlive()) {
            this.runEncounter(server);
        }
    }

    private void runEncounter(ServerLevel server) {
        if (this.stage < LAST_HEALING_WAVE && this.getHealth() < this.getMaxHealth()) {
            this.heal(REGEN_PER_TICK);
        }
        if (this.clock < OPENING_TICKS) {
            this.clock++;
            if (this.stage == 0) {
                this.stageOpening(server);
            }
            return;
        }
        if (this.waveCountdown > 0) {
            this.waveCountdown--;
            return;
        }
        if (server.getNearestPlayer(this, WAVE_PLAYER_RANGE) == null) {
            this.waveCountdown = IDLE_RECHECK_MIN + this.random.nextInt(IDLE_RECHECK_SPREAD);
            return;
        }
        this.runWave(server);
    }

    private void stageOpening(ServerLevel server) {
        if (this.clock == BANNER_TICK) {
            this.raiseBanners(server);
        }
        if (this.clock >= CRATE_FIRST_TICK && this.clock <= CRATE_LAST_TICK && (this.clock - CRATE_FIRST_TICK) % CRATE_INTERVAL == 0) {
            this.tryConjureCrate(server);
        }
    }

    private void raiseBanners(ServerLevel server) {
        BlockPos center = this.blockPosition();
        for (Direction outward : Direction.Plane.HORIZONTAL) {
            BlockPos spot = center.relative(outward, BANNER_DISTANCE);
            BlockState banner = TTBlocks.BANNER_CRIMSON_CULT.get().defaultBlockState().setValue(BannerStandingBlock.ROTATION, RotationSegment.convertToSegment(outward));
            server.setBlock(spot, banner, Block.UPDATE_ALL);
            this.arcTo(server, spot);
            this.playConjure();
        }
        CultistPortals.pulse(server, this);
    }

    private void tryConjureCrate(ServerLevel server) {
        int offsetX = this.random.nextInt(CRATE_REACH + 1) - this.random.nextInt(CRATE_REACH + 1);
        int offsetZ = this.random.nextInt(CRATE_REACH + 1) - this.random.nextInt(CRATE_REACH + 1);
        if (offsetX == 0 || offsetZ == 0) {
            return;
        }
        BlockPos spot = this.blockPosition().offset(offsetX, 0, offsetZ);
        if (!server.isEmptyBlock(spot)) {
            return;
        }
        server.setBlock(spot, this.rollCrate().defaultBlockState(), Block.UPDATE_ALL);
        this.arcTo(server, spot);
        this.playConjure();
        CultistPortals.pulse(server, this);
    }

    private Block rollCrate() {
        float roll = this.random.nextFloat();
        if (roll < RARE_CRATE_CHANCE) {
            return TTBlocks.LOOT_CRATE_RARE.get();
        }
        if (roll < RARE_CRATE_CHANCE + UNCOMMON_CRATE_CHANCE) {
            return TTBlocks.LOOT_CRATE_UNCOMMON.get();
        }
        return TTBlocks.LOOT_CRATE_COMMON.get();
    }

    private void arcTo(ServerLevel server, BlockPos spot) {
        Vec3 core = new Vec3(this.getX(), this.getY(PORTAL_CORE), this.getZ());
        Vec3 end = new Vec3(spot.getX() + BLOCK_CENTER, spot.getY() + BLOCK_CENTER, spot.getZ() + BLOCK_CENTER);
        Effects.boltStrike(server, core).to(end).color(ARC_COLOR).send();
    }

    private void playConjure() {
        this.playSound(TTSounds.WANDFAIL.get(), CONJURE_VOLUME, CONJURE_PITCH);
    }

    private void runWave(ServerLevel server) {
        this.stage++;
        boolean praetorWave = this.stage == PRAETOR_WAVE;
        Mob arrival = praetorWave ? TTEntities.CULTIST_LEADER.get().create(server, EntitySpawnReason.MOB_SUMMONED) : CultistPortals.rollMinion(server, this.random);
        int crowd = CultistPortals.cultistsNear(this, CROWD_RANGE) * TICKS_PER_SECOND;
        CultistPortals.pulse(server, this);
        if (arrival != null) {
            CultistPortals.summon(this, server, arrival);
            arrival.setHomeTo(this.blockPosition(), ARRIVAL_HOME_RADIUS);
            if (this.stage > PRAETOR_WAVE) {
                this.hurtServer(server, server.damageSources().magic(), BURN_DAMAGE_BASE + this.random.nextInt(BURN_DAMAGE_SPREAD));
            }
        }
        this.waveCountdown = this.pauseAfter(this.stage, crowd);
    }

    private int pauseAfter(int wave, int crowdTicks) {
        if (wave <= RUSH_WAVES) {
            int step = wave - 1;
            int shortest = RUSH_MIN_START - step * RUSH_MIN_STEP;
            int longest = RUSH_MAX_START - step * RUSH_MAX_STEP;
            return shortest + this.random.nextInt(longest - shortest + 1);
        }
        if (wave == PRAETOR_WAVE) {
            return PRAETOR_PAUSE_BASE + PRAETOR_CROWD_FACTOR * crowdTicks + this.random.nextInt(PRAETOR_PAUSE_SPREAD);
        }
        return crowdTicks + this.random.nextInt(CROWD_EXTRA_BASE + crowdTicks / CROWD_EXTRA_DIVISOR);
    }

    @Override
    public void move(MoverType type, Vec3 movement) {}

    @Override
    public boolean isPushable() {
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
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(STAGE_KEY, this.stage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.stage = input.getIntOr(STAGE_KEY, 0);
    }
}
