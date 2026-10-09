package com.leclowndu93150.thaumaturge.content.taint.block;

import com.leclowndu93150.thaumaturge.content.entity.EntityFallingTaint;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.entity.EntityTaintSporeSwarmer;
import com.leclowndu93150.thaumaturge.content.taint.flux.BlockFluxGoo;
import com.leclowndu93150.thaumaturge.content.taint.flux.FluxGooFluid;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

public final class BlockTaintCrust extends AbstractTaintBlock {
    public static final MapCodec<BlockTaintCrust> CODEC = simpleCodec(BlockTaintCrust::new);

    private static final int BIOME_DECAY_ONE_IN = 20;
    private static final int CREEP_DEPTH = 3;
    private static final int SWARMER_ONE_IN = 200;
    private static final double SWARMER_SPACING = 16.0;
    private static final int LOG_CLEARANCE = 1;
    private static final double HALF = 0.5;
    private static final int BLOCKING_GOO_AMOUNT = 4;
    private static final Direction[] ENCLOSING_SIDES = {Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    public BlockTaintCrust(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BlockTaintCrust> codec() {
        return CODEC;
    }

    @Override
    public void decay(Level level, BlockPos pos, BlockState state) {
        level.setBlockAndUpdate(pos, FluxGooFluid.gooBlockState(PhysicalFlux.MAX_QUANTA));
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        TaintHelper.trySpreadTaintedBiome(level, pos, random);
        if (TaintBiomeManager.isTainted(level, pos)) {
            if (!shed(level, pos, state, random)) {
                subRandomTick(state, level, pos, random);
            }
        } else if (random.nextInt(BIOME_DECAY_ONE_IN) == 0) {
            decay(level, pos, state);
        } else {
            shed(level, pos, state, random);
        }
    }

    @Override
    protected void subRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        TaintHelper.attemptFibreGrowth(level, pos, false);
        if (rollSwarmer(level, pos, random) || hasOpenSide(level, pos)) {
            return;
        }
        decay(level, pos, state);
    }

    public static boolean canFallBelow(Level level, BlockPos pos) {
        boolean nearLog = BlockPos.betweenClosedStream(pos.getX() - LOG_CLEARANCE, pos.getY() - LOG_CLEARANCE, pos.getZ() - LOG_CLEARANCE, pos.getX() + LOG_CLEARANCE, pos.getY() + LOG_CLEARANCE,
                pos.getZ() + LOG_CLEARANCE).anyMatch(near -> level.getBlockState(near).is(BlockTags.LOGS));
        if (nearLog) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BlockFluxGoo goo && goo.fluxAmount(state) >= BLOCKING_GOO_AMOUNT) {
            return false;
        }
        FluidState fluid = state.getFluidState();
        boolean liquid = fluid.is(FluidTags.WATER) || fluid.is(FluidTags.LAVA);
        return liquid || state.isAir() || state.is(BlockTags.FIRE) || state.is(TTBlocks.TAINT_FIBRE) || state.canBeReplaced();
    }

    private static boolean rollSwarmer(ServerLevel level, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.above()).isAir() && random.nextInt(SWARMER_ONE_IN) == 0 && trySpawnSwarmer(level, pos);
    }

    private static boolean hasOpenSide(Level level, BlockPos pos) {
        return Arrays.stream(ENCLOSING_SIDES).anyMatch(side -> !isCrust(level, pos.relative(side)));
    }

    private static boolean isCrust(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(TTBlocks.TAINT_CRUST);
    }

    private static boolean shed(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        return release(level, pos, pos, state) || tryCreep(level, pos, state, random);
    }

    private static boolean canRelease(ServerLevel level, BlockPos cell, BlockPos origin) {
        return BlockTaintFibre.isOnlyAdjacentToTaint(level, origin) && cell.getY() >= level.getMinY() && canFallBelow(level, cell.below());
    }

    private static boolean release(ServerLevel level, BlockPos cell, BlockPos origin, BlockState state) {
        if (!canRelease(level, cell, origin)) {
            return false;
        }
        double x = cell.getX() + HALF;
        double y = cell.getY() + HALF;
        double z = cell.getZ() + HALF;
        level.addFreshEntity(new EntityFallingTaint(level, x, y, z, state, origin));
        return true;
    }

    private static boolean tryCreep(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (!level.getBlockState(pos.above()).isAir()) {
            return false;
        }
        BlockPos side = pos.relative(Direction.Plane.HORIZONTAL.getRandomDirection(random));
        return hasCreepRoom(level, pos, side) && release(level, side, pos, state);
    }

    private static boolean hasCreepRoom(ServerLevel level, BlockPos pos, BlockPos side) {
        return IntStream.rangeClosed(1, CREEP_DEPTH).allMatch(depth -> level.getBlockState(side.below(depth)).isAir() && isCrust(level, pos.below(depth)));
    }

    private static boolean trySpawnSwarmer(ServerLevel level, BlockPos pos) {
        if (!level.getEntitiesOfClass(EntityTaintSporeSwarmer.class, new AABB(pos).inflate(SWARMER_SPACING)).isEmpty()) {
            return false;
        }
        EntityTaintSporeSwarmer swarmer = TTEntities.TAINT_SPORE_SWARMER.get().create(level, EntitySpawnReason.NATURAL);
        if (swarmer == null) {
            return false;
        }
        replaceWithSwarmer(level, pos, swarmer);
        return true;
    }

    private static void replaceWithSwarmer(ServerLevel level, BlockPos pos, EntityTaintSporeSwarmer swarmer) {
        swarmer.snapTo(pos.getX() + HALF, pos.getY(), pos.getZ() + HALF, 0.0F, 0.0F);
        level.removeBlock(pos, false);
        level.addFreshEntity(swarmer);
    }
}
