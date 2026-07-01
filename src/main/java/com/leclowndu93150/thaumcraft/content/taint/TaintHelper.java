package com.leclowndu93150.thaumcraft.content.taint;

import com.leclowndu93150.thaumcraft.api.aura.AuraHelper;
import com.leclowndu93150.thaumcraft.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumcraft.content.taint.spread.TaintSeedRegistry;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class TaintHelper {
    public static final double TAINT_SPREAD_AREA = 16.0;
    public static final double TAINT_SPREAD_AREA_SQ = TAINT_SPREAD_AREA * TAINT_SPREAD_AREA;
    public static final double TAINT_FRINGE = TAINT_SPREAD_AREA * 0.8;
    public static final double TAINT_FRINGE_SQ = TAINT_FRINGE * TAINT_FRINGE;
    public static final float TAINT_SPREAD_RATE = 1.0F;

    private TaintHelper() {}

    public static boolean isNearTaintSeed(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        return TaintSeedRegistry.get(serverLevel).isNear(pos, TAINT_SPREAD_AREA_SQ);
    }

    public static boolean isAtTaintSeedEdge(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        return TaintSeedRegistry.get(serverLevel).isAtEdge(pos, TAINT_FRINGE_SQ, TAINT_SPREAD_AREA_SQ);
    }

    public static boolean isAdjacentToSolidBlock(LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighbor);
            if (neighborState.isFaceSturdy(level, neighbor, direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    public static void spreadFibres(ServerLevel level, BlockPos pos, boolean force) {
        RandomSource random = level.getRandom();
        float saturation = AuraHelper.getFluxSaturation(level, pos);
        float mod = 0.001F + saturation * 2.0F;
        if (!force && random.nextFloat() > (TAINT_SPREAD_RATE / 100.0F) * mod) {
            return;
        }
        if (!isNearTaintSeed(level, pos)) {
            return;
        }
        BlockPos target = pos.offset(
                random.nextInt(3) - 1,
                random.nextInt(3) - 1,
                random.nextInt(3) - 1);
        if (target.equals(pos)) {
            return;
        }
        BlockState targetState = level.getBlockState(target);
        float hardness = targetState.getDestroySpeed(level, target);
        if (hardness < 0 || hardness > 10) {
            return;
        }

        boolean isLeaves = targetState.is(BlockTags.LEAVES);
        boolean isReplaceable = targetState.isAir() || targetState.canBeReplaced();

        if (!isLeaves && !targetState.liquid() && isReplaceable) {
            if (isAdjacentToSolidBlock(level, target)
                    && !BlockTaintFibre.isOnlyAdjacentToTaint(level, target)) {
                level.setBlock(target, TCBlocks.TAINT_FIBRE.get().defaultBlockState(), Block.UPDATE_ALL);
                AuraHelper.drainFlux(level, target, 0.01F, false);
            }
            return;
        }

        if (isLeaves) {
            if (random.nextFloat() < 0.6F) {
                Direction logFace = findAdjacentLog(level, target);
                if (logFace != null) {
                    level.setBlock(target,
                            TCBlocks.TAINT_FEATURE.get().defaultBlockState()
                                    .setValue(DirectionalBlock.FACING, logFace.getOpposite()),
                            Block.UPDATE_ALL);
                    AuraHelper.drainFlux(level, target, 0.01F, false);
                    return;
                }
            }
            level.setBlock(target, TCBlocks.TAINT_FIBRE.get().defaultBlockState(), Block.UPDATE_ALL);
            AuraHelper.drainFlux(level, target, 0.01F, false);
            return;
        }

        if (BlockTaintFibre.isHemmedByTaint(level, target) && hardness < 5.0F) {
            if (targetState.is(BlockTags.LOGS)) {
                Direction.Axis axis = Direction.Axis.Y;
                if (targetState.hasProperty(RotatedPillarBlock.AXIS)) {
                    axis = targetState.getValue(RotatedPillarBlock.AXIS);
                }
                level.setBlock(target,
                        TCBlocks.TAINT_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis),
                        Block.UPDATE_ALL);
                AuraHelper.drainFlux(level, target, 0.01F, false);
                return;
            }
            if (targetState.is(BlockTags.SAND) || targetState.is(BlockTags.DIRT)) {
                level.setBlock(target, TCBlocks.TAINT_SOIL.get().defaultBlockState(), Block.UPDATE_ALL);
                AuraHelper.drainFlux(level, target, 0.01F, false);
                return;
            }
            if (targetState.is(BlockTags.STONE_ORE_REPLACEABLES)
                    || targetState.is(BlockTags.STONE_BRICKS)
                    || targetState.is(BlockTags.BASE_STONE_OVERWORLD)) {
                level.setBlock(target, TCBlocks.TAINT_ROCK.get().defaultBlockState(), Block.UPDATE_ALL);
                AuraHelper.drainFlux(level, target, 0.01F, false);
                return;
            }
            level.setBlock(target, TCBlocks.TAINT_CRUST.get().defaultBlockState(), Block.UPDATE_ALL);
            AuraHelper.drainFlux(level, target, 0.01F, false);
        }
    }

    private static Direction findAdjacentLog(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockState neighborState = level.getBlockState(pos.relative(direction));
            if (neighborState.is(TCBlocks.TAINT_LOG.get())) {
                return direction;
            }
        }
        return null;
    }
}
