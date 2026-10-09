package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchCrab;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.content.particle.VentParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.EventHooks;

public final class BlockEntityEldritchCrabSpawner extends BlockEntity {
    private static final int WARNING_EVENT = 1;
    private static final int WARNING_EVENT_PARAM = 0;
    private static final int STAGGER_BOUND = 201;
    private static final int WARNING_TICKS = 15;
    private static final int SOUND_STEP = 5;
    private static final int IDLE_SOUND_ODDS = 20;
    private static final int HARD_DELAY = 120;
    private static final int NORMAL_DELAY = 150;
    private static final int EASY_DELAY = 200;
    private static final int DELAY_SPREAD = 61;
    private static final int BLOCKED_DELAY = 60;
    private static final int BLOCKED_SPREAD = 101;
    private static final double CRAB_COUNT_MARGIN = 16.0;
    private static final float SOUND_VOLUME = 0.5F;
    private static final float WARNING_PITCH = 1.1F;
    private static final float IDLE_PITCH = 0.65F;
    private static final float GORE_PITCH = 1.0F;
    private static final double CENTER = 0.5;
    private static final double UP_HEIGHT = 0.25;
    private static final double DOWN_TOP = 0.75;
    private static final double LAUNCH_SPEED = 0.2;
    private static final int HELM_ODDS = 100;
    private static final int CHAMPION_ODDS = 1000;
    private static final int STEAM_BURST_TICKS = 20;
    private static final int BURST_MIN_PUFFS = 2;
    private static final int BURST_EXTRA_PUFFS = 4;
    private static final int REST_PUFF_ODDS = 20;
    private static final double PLATE_THICKNESS = 0.25;
    private static final double PLATE_FAR = 0.75;
    private static final float PUFF_SPREAD_DIVISOR = 4.0F;
    private static final float PATCH_SPAN = 0.5F;
    private static final double PUFF_SPEED = 0.25;
    private static final int STEAM_COLOR = 0x9988AA;
    private static final float STEAM_SCALE = 2.0F;

    private int spawnTimer;
    private int steamBurst;

    public BlockEntityEldritchCrabSpawner(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ELDRITCH_CRAB_SPAWNER.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel serverLevel) {
            serverTick(serverLevel, pos, state.getValue(BlockEldritchCrabSpawner.FACING));
        } else {
            clientTick(level, pos, state.getValue(BlockEldritchCrabSpawner.FACING));
        }
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == WARNING_EVENT) {
            steamBurst = STEAM_BURST_TICKS;
            return true;
        }
        return super.triggerEvent(id, param);
    }

    private void serverTick(ServerLevel level, BlockPos pos, Direction facing) {
        RandomSource random = level.getRandom();
        if (spawnTimer == 0) {
            spawnTimer = random.nextInt(STAGGER_BOUND);
            return;
        }
        spawnTimer--;
        if (spawnTimer == 0) {
            if (canSpawn(level, pos)) {
                spawnCrab(level, pos, facing);
                spawnTimer = spawnDelay(level.getDifficulty()) + random.nextInt(DELAY_SPREAD);
            } else {
                spawnTimer = BLOCKED_DELAY + random.nextInt(BLOCKED_SPREAD);
            }
        } else if (spawnTimer == WARNING_TICKS && canSpawn(level, pos)) {
            level.blockEvent(pos, getBlockState().getBlock(), WARNING_EVENT, WARNING_EVENT_PARAM);
            level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, SOUND_VOLUME, WARNING_PITCH);
        } else if (spawnTimer % SOUND_STEP == 0 && random.nextInt(IDLE_SOUND_ODDS) == 0) {
            level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, SOUND_VOLUME, IDLE_PITCH);
        }
    }

    private static int spawnDelay(Difficulty difficulty) {
        return switch (difficulty) {
            case HARD -> HARD_DELAY;
            case NORMAL -> NORMAL_DELAY;
            default -> EASY_DELAY;
        };
    }

    private static boolean canSpawn(ServerLevel level, BlockPos pos) {
        double range = ThaumaturgeServerConfig.LABYRINTH.crabVentActivationRange.get();
        if (level.getNearestPlayer(pos.getX() + CENTER, pos.getY() + CENTER, pos.getZ() + CENTER, range, EntitySelector.NO_SPECTATORS) == null) {
            return false;
        }
        int crabs = level.getEntitiesOfClass(EntityEldritchCrab.class, new AABB(pos).inflate(CRAB_COUNT_MARGIN)).size();
        return crabs < ThaumaturgeServerConfig.LABYRINTH.crabVentMaxCrabs.get();
    }

    private static void spawnCrab(ServerLevel level, BlockPos pos, Direction facing) {
        EntityEldritchCrab crab = TTEntities.ELDRITCH_CRAB.get().create(level, EntitySpawnReason.SPAWNER);
        if (crab == null) {
            return;
        }
        float width = crab.getBbWidth();
        float height = crab.getBbHeight();
        double x = CENTER;
        double y = CENTER;
        double z = CENTER;
        switch (facing.getAxis()) {
            case X -> {
                x = width / 2.0;
                y = CENTER - height / 2.0;
            }
            case Y -> y = facing == Direction.UP ? UP_HEIGHT : DOWN_TOP - height;
            case Z -> {
                y = CENTER - height / 2.0;
                z = width / 2.0;
            }
        }
        float yaw = facing.getAxis().isHorizontal() ? facing.toYRot() : 0.0F;
        crab.snapTo(pos.getX() + x, pos.getY() + y, pos.getZ() + z, yaw, 0.0F);
        crab.setDeltaMovement(facing.getStepX() * LAUNCH_SPEED, facing.getStepY() * LAUNCH_SPEED, facing.getStepZ() * LAUNCH_SPEED);
        DifficultyInstance difficulty = level.getCurrentDifficultyAt(pos);
        EventHooks.finalizeMobSpawn(crab, level, difficulty, EntitySpawnReason.SPAWNER, null);
        int scale = Math.max(1, (int) (level.getDifficulty().getId() + difficulty.getEffectiveDifficulty()));
        RandomSource random = level.getRandom();
        crab.setHelm(random.nextInt(Math.max(HELM_ODDS / scale, 1)) == 0);
        if (random.nextInt(Math.max(CHAMPION_ODDS / scale, 1)) == 0) {
            ChampionHelper.makeChampion(crab, false);
        }
        if (level.addFreshEntity(crab)) {
            SoundEvent gore = TTSounds.GORE.get();
            level.playSound(null, pos, gore, SoundSource.BLOCKS, SOUND_VOLUME, GORE_PITCH);
        }
    }

    private void clientTick(Level level, BlockPos pos, Direction facing) {
        RandomSource random = level.getRandom();
        int puffs = 0;
        if (steamBurst > 0) {
            steamBurst--;
            puffs = BURST_MIN_PUFFS + random.nextInt(BURST_EXTRA_PUFFS);
        } else if (random.nextInt(REST_PUFF_ODDS) == 0) {
            puffs = 1;
        }
        for (int i = 0; i < puffs; i++) {
            emitPuff(level, pos, facing, random);
        }
    }

    private static void emitPuff(Level level, BlockPos pos, Direction facing, RandomSource random) {
        VentParticleOptions options = new VentParticleOptions(facing.getStepX() * PUFF_SPEED, facing.getStepY() * PUFF_SPEED, facing.getStepZ() * PUFF_SPEED, STEAM_COLOR, STEAM_SCALE, false);
        level.addParticle(options, pos.getX() + puffCoordinate(Direction.Axis.X, facing, random), pos.getY() + puffCoordinate(Direction.Axis.Y, facing, random),
                pos.getZ() + puffCoordinate(Direction.Axis.Z, facing, random), 0.0, 0.0, 0.0);
    }

    private static double puffCoordinate(Direction.Axis axis, Direction facing, RandomSource random) {
        if (facing.getAxis() == axis) {
            double surface = facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? PLATE_THICKNESS : PLATE_FAR;
            return surface + (random.nextFloat() - random.nextFloat()) / PUFF_SPREAD_DIVISOR;
        }
        return PLATE_THICKNESS + random.nextFloat() * PATCH_SPAN;
    }
}
