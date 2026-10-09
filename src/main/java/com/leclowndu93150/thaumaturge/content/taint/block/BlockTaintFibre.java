package com.leclowndu93150.thaumaturge.content.taint.block;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.taint.ITaintBlock;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.content.taint.effect.FluxTaintExposure;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockTaintFibre extends Block implements ITaintBlock {
    public static final MapCodec<BlockTaintFibre> CODEC = simpleCodec(BlockTaintFibre::new);
    private static final String GROWTH_PREFIX = "growth";
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty GROWTH1 = growthFlag(1);
    public static final BooleanProperty GROWTH2 = growthFlag(2);
    public static final BooleanProperty GROWTH3 = growthFlag(3);
    public static final BooleanProperty GROWTH4 = growthFlag(4);

    private static final BooleanProperty[] ALL_FLAGS = Stream.concat(Stream.of(NORTH, EAST, SOUTH, WEST, UP, DOWN), Stream.of(GROWTH1, GROWTH2, GROWTH3, GROWTH4)).toArray(BooleanProperty[]::new);
    private static final Direction[] DIRECTIONS = Direction.values();

    private static final int GROWTH_ROLL_RANGE = 50;
    private static final int GROWTH1_ROLL_END = 4;
    private static final int GROWTH2_ROLL_END = 6;
    private static final int GROWTH3_ROLL = 6;
    private static final int GROWTH4_MIN_ROLL = 48;
    private static final int STEP_INFECTION_ONE_IN = 750;
    private static final int GROWTH3_POLLUTION_BASE = 3;
    private static final int GROWTH3_POLLUTION_RANGE = 3;
    private static final float STALK_PRESSURE = 0.02F;

    private static final double SLAB_THICKNESS = 0.05;
    private static final double[][] GROWTH3_BOXES = {{6.0, 5.0, 7.0, 8.0, 6.0, 9.0}, {5.0, 4.0, 5.0, 11.0, 5.0, 11.0}, {5.0, 1.0, 11.0, 11.0, 4.0, 12.0}, {5.0, 1.0, 4.0, 11.0, 4.0, 5.0},
            {5.0, 0.0, 5.0, 11.0, 1.0, 11.0}, {4.0, 1.0, 5.0, 12.0, 4.0, 11.0}};
    private static final VoxelShape GROWTH3_SHAPE = unionOfBoxes(GROWTH3_BOXES);
    private static final Map<Direction, VoxelShape> SLABS = buildSlabs();
    private static final Map<BooleanProperty, VoxelShape> GROWTH_SHAPES = buildGrowthShapes();

    private final Function<BlockState, VoxelShape> shapes;

    public BlockTaintFibre(Properties properties) {
        super(properties);
        BlockState initial = stateDefinition.any();
        for (BooleanProperty flag : ALL_FLAGS) {
            initial = initial.setValue(flag, false);
        }
        registerDefaultState(initial);
        shapes = getShapeForEachState(BlockTaintFibre::buildShape);
    }

    public static BooleanProperty propertyFor(Direction direction) {
        return switch (direction.getAxis()) {
            case X -> direction == Direction.EAST ? EAST : WEST;
            case Z -> direction == Direction.SOUTH ? SOUTH : NORTH;
            case Y -> direction == Direction.UP ? UP : DOWN;
        };
    }

    @Override
    public MapCodec<BlockTaintFibre> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ALL_FLAGS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.apply(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return stateForWorld(context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        return hasSolidAttachment(level, pos) ? stateForWorld(level, pos) : Blocks.AIR.defaultBlockState();
    }

    public static BlockState stateForWorld(LevelReader level, BlockPos pos) {
        BlockState state = TTBlocks.TAINT_FIBRE.get().defaultBlockState();
        for (Direction direction : DIRECTIONS) {
            state = state.setValue(propertyFor(direction), isAttachable(level, pos, direction));
        }
        int roll = RandomSource.create(Mth.getSeed(pos)).nextInt(GROWTH_ROLL_RANGE);
        boolean grounded = state.getValue(DOWN);
        boolean roofed = state.getValue(UP);
        return state.setValue(GROWTH1, grounded && roll < GROWTH1_ROLL_END).setValue(GROWTH2, grounded && roll >= GROWTH1_ROLL_END && roll < GROWTH2_ROLL_END)
                .setValue(GROWTH3, grounded && roll == GROWTH3_ROLL).setValue(GROWTH4, roofed && roll >= GROWTH4_MIN_ROLL);
    }

    public static boolean hasSolidAttachment(LevelReader level, BlockPos pos) {
        return Arrays.stream(DIRECTIONS).anyMatch(direction -> isAttachable(level, pos, direction));
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return hasSolidAttachment(level, pos);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        TaintHelper.trySpreadTaintedBiome(level, pos, random);
        if (shouldWither(state, level, pos)) {
            decay(level, pos, state);
            return;
        }
        boolean becameStalk = state.getValue(GROWTH3) && tryBecomeStalk(level, pos);
        if (!becameStalk) {
            TaintHelper.attemptFibreGrowth(level, pos, false);
        }
    }

    private static boolean hasAnyGrowth(BlockState state) {
        for (BooleanProperty growth : GROWTH_SHAPES.keySet()) {
            if (state.getValue(growth)) {
                return true;
            }
        }
        return false;
    }

    private static boolean shouldWither(BlockState state, ServerLevel level, BlockPos pos) {
        return (!hasAnyGrowth(state) && isOnlyAdjacentToTaint(level, pos)) || !TaintHelper.isEcologicallySustained(level, pos);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && state.getValue(GROWTH3)) {
            AuraHelper.polluteAura(level, pos, GROWTH3_POLLUTION_BASE + level.getRandom().nextInt(GROWTH3_POLLUTION_RANGE), true);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void decay(Level level, BlockPos pos, BlockState state) {
        level.removeBlock(pos, false);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        FluxTaintExposure.onStep(level, entity, STEP_INFECTION_ONE_IN);
    }

    public static boolean isOnlyAdjacentToTaint(LevelAccessor level, BlockPos pos) {
        return Arrays.stream(DIRECTIONS).noneMatch(direction -> isSolidNonTaintNeighbour(level, pos, direction));
    }

    private static boolean isSolidNonTaintNeighbour(LevelAccessor level, BlockPos pos, Direction direction) {
        BlockPos neighbour = pos.relative(direction);
        BlockState neighbourState = level.getBlockState(neighbour);
        boolean passable = neighbourState.isAir() || neighbourState.getBlock() instanceof ITaintBlock;
        return !passable && neighbourState.isFaceSturdy(level, neighbour, direction.getOpposite());
    }

    public static boolean isHemmedByTaint(LevelAccessor level, BlockPos pos) {
        return Arrays.stream(DIRECTIONS).allMatch(direction -> level.getBlockState(pos.relative(direction)).getBlock() instanceof ITaintBlock);
    }

    private static boolean isAttachable(LevelReader level, BlockPos pos, Direction direction) {
        BlockPos neighbour = pos.relative(direction);
        return level.getBlockState(neighbour).isFaceSturdy(level, neighbour, direction.getOpposite());
    }

    private static boolean tryBecomeStalk(ServerLevel level, BlockPos pos) {
        BlockState stalk = TTBlocks.TAINT_SPORE_STALK.get().defaultBlockState();
        if (ThaumaturgeCommonConfig.WUSS_MODE.get() || TaintBlooms.isProtected(level, pos) || !TaintEcology.isTainted(level, pos) || !stalk.canSurvive(level, pos)) {
            return false;
        }
        level.setBlock(pos, stalk, Block.UPDATE_ALL);
        TaintEcology.addPressure(level, pos, STALK_PRESSURE);
        return true;
    }

    private static VoxelShape buildShape(BlockState state) {
        VoxelShape shape = Shapes.empty();
        for (Direction direction : DIRECTIONS) {
            if (state.getValue(propertyFor(direction))) {
                shape = Shapes.or(shape, SLABS.get(direction));
            }
        }
        for (Map.Entry<BooleanProperty, VoxelShape> growth : GROWTH_SHAPES.entrySet()) {
            if (state.getValue(growth.getKey())) {
                shape = Shapes.or(shape, growth.getValue());
            }
        }
        return shape;
    }

    private static Map<Direction, VoxelShape> buildSlabs() {
        Map<Direction, VoxelShape> slabs = new EnumMap<>(Direction.class);
        for (Direction direction : DIRECTIONS) {
            boolean positive = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE;
            double from = positive ? 1.0 - SLAB_THICKNESS : 0.0;
            double to = positive ? 1.0 : SLAB_THICKNESS;
            Direction.Axis axis = direction.getAxis();
            slabs.put(direction, Shapes.box(axis == Direction.Axis.X ? from : 0.0, axis == Direction.Axis.Y ? from : 0.0, axis == Direction.Axis.Z ? from : 0.0, axis == Direction.Axis.X ? to : 1.0,
                    axis == Direction.Axis.Y ? to : 1.0, axis == Direction.Axis.Z ? to : 1.0));
        }
        return slabs;
    }

    private static Map<BooleanProperty, VoxelShape> buildGrowthShapes() {
        Map<BooleanProperty, VoxelShape> shapes = new LinkedHashMap<>();
        shapes.put(GROWTH1, growthColumn(0.1, 0.9, 0.0, 0.4));
        shapes.put(GROWTH2, growthColumn(0.2, 0.8, 0.0, 1.0));
        shapes.put(GROWTH3, GROWTH3_SHAPE);
        shapes.put(GROWTH4, growthColumn(0.1, 0.9, 0.3, 1.0));
        return shapes;
    }

    private static VoxelShape growthColumn(double horizontalMin, double horizontalMax, double bottom, double top) {
        return Shapes.box(horizontalMin, bottom, horizontalMin, horizontalMax, top, horizontalMax);
    }

    private static BooleanProperty growthFlag(int index) {
        return BooleanProperty.create(GROWTH_PREFIX + index);
    }

    private static VoxelShape unionOfBoxes(double[][] boxes) {
        VoxelShape union = Shapes.empty();
        for (double[] b : boxes) {
            union = Shapes.or(union, Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
        }
        return union;
    }
}
