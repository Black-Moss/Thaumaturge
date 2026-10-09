package com.leclowndu93150.thaumaturge.content.taint.block;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.taint.ITaintBlock;
import com.leclowndu93150.thaumaturge.content.device.DeviceShapes;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintCrawler;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.flux.FluxGooFluid;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockTaintFeature extends DirectionalBlock implements ITaintBlock {
    public static final MapCodec<BlockTaintFeature> CODEC = simpleCodec(BlockTaintFeature::new);

    private static final int DECAY_ONE_IN = 10;
    private static final int POLLUTION_ONE_IN = 200;
    private static final float POLLUTION_AMOUNT = 1.0F;
    private static final float POLLUTION_FLUX_RATIO = 0.2F;
    private static final int GEYSER_ONE_IN = 100;
    private static final float CRAWLER_CHANCE = 0.333F;
    private static final double CELL_CENTRE = 0.5;
    private static final float FULL_TURN_DEGREES = 360.0F;

    private static final int[][] VARIANT_ZERO = {{3, 0, 3, 11, 1, 11}, {2, 1, 3, 12, 9, 11}, {3, 1, 2, 11, 9, 3}, {3, 1, 11, 11, 9, 12}, {3, 9, 3, 11, 10, 11}, {5, 10, 6, 8, 11, 9}};
    private static final int[][] VARIANT_ONE = {{3, 0, 4, 9, 1, 10}, {2, 1, 4, 10, 7, 10}, {3, 1, 3, 9, 7, 4}, {3, 1, 10, 9, 7, 11}, {3, 7, 4, 9, 8, 10}, {5, 8, 6, 7, 9, 8}, {9, 0, 8, 13, 1, 12},
            {10, 1, 7, 13, 5, 10}, {13, 1, 8, 14, 5, 11}, {9, 1, 10, 13, 6, 11}, {8, 1, 11, 14, 5, 12}, {9, 1, 12, 13, 5, 13}, {10, 5, 8, 13, 6, 10}, {9, 5, 11, 13, 6, 12}};
    private static final int[][] VARIANT_TWO = {{2, 0, 8, 6, 1, 12}, {1, 1, 8, 7, 5, 12}, {2, 1, 7, 6, 5, 8}, {2, 1, 12, 6, 5, 13}, {2, 5, 8, 6, 6, 12}, {9, 0, 2, 13, 1, 6}, {8, 1, 2, 14, 4, 6},
            {9, 1, 1, 13, 4, 2}, {9, 1, 6, 13, 4, 7}, {9, 4, 2, 13, 5, 6}, {10, 0, 10, 14, 1, 14}, {9, 1, 10, 15, 5, 14}, {10, 1, 9, 14, 5, 10}, {10, 1, 14, 14, 5, 15}, {10, 5, 10, 14, 6, 14},
            {11, 6, 11, 13, 7, 13}};
    private static final List<Map<Direction, VoxelShape>> VARIANT_SHAPES = buildVariants(VARIANT_ZERO, VARIANT_ONE, VARIANT_TWO);

    public BlockTaintFeature(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    public MapCodec<BlockTaintFeature> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int variant = RandomSource.create(state.getSeed(pos)).nextInt(VARIANT_SHAPES.size());
        return VARIANT_SHAPES.get(variant).get(state.getValue(FACING));
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        TaintHelper.trySpreadTaintedBiome(level, pos, random);
        if (!TaintHelper.isEcologicallySustained(level, pos)) {
            if (random.nextInt(DECAY_ONE_IN) == 0) {
                decay(level, pos, state);
                return;
            }
        } else if (random.nextInt(POLLUTION_ONE_IN) == 0 && pollutes(level, pos)) {
            return;
        }
        TaintHelper.attemptFibreGrowth(level, pos, false);
        BlockState below = level.getBlockState(pos.below());
        if (below.is(TTBlocks.TAINT_LOG) && below.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y && random.nextInt(GEYSER_ONE_IN) == 0) {
            level.setBlock(pos, TTBlocks.TAINT_GEYSER.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        RandomSource random = level.getRandom();
        EntityTaintCrawler crawler = random.nextFloat() < CRAWLER_CHANCE ? TTEntities.TAINT_CRAWLER.get().create(level, EntitySpawnReason.NATURAL) : null;
        if (crawler == null) {
            AuraHelper.polluteAura(level, pos, POLLUTION_AMOUNT, true);
            return;
        }
        crawler.snapTo(pos.getX() + CELL_CENTRE, pos.getY(), pos.getZ() + CELL_CENTRE, random.nextFloat() * FULL_TURN_DEGREES, 0.0F);
        level.addFreshEntity(crawler);
    }

    @Override
    public void decay(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, FluxGooFluid.gooBlockState(PhysicalFlux.MAX_QUANTA), Block.UPDATE_ALL);
    }

    private static boolean pollutes(ServerLevel level, BlockPos pos) {
        int base = AuraHelper.getAuraBase(level, pos);
        if (base <= 0 || AuraHelper.getFlux(level, pos) > POLLUTION_FLUX_RATIO * base) {
            return false;
        }
        AuraHelper.polluteAura(level, pos, POLLUTION_AMOUNT, true);
        return true;
    }

    private static List<Map<Direction, VoxelShape>> buildVariants(int[][]... variants) {
        List<Map<Direction, VoxelShape>> shapes = new ArrayList<>();
        for (int[][] boxes : variants) {
            VoxelShape shape = Shapes.empty();
            for (int[] box : boxes) {
                shape = Shapes.or(shape, Block.box(box[0], box[1], box[2], box[3], box[4], box[5]));
            }
            shapes.add(DeviceShapes.facingShapesFromUp(shape));
        }
        return shapes;
    }
}
