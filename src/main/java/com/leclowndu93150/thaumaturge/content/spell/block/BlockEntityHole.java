package com.leclowndu93150.thaumaturge.content.spell.block;

import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class BlockEntityHole extends AbstractSyncedBlockEntity {
    private static final String OLD_BLOCK_KEY = "oldblock";
    private static final String COUNTDOWN_KEY = "countdown";
    private static final String COUNTDOWN_MAX_KEY = "countdownmax";
    private static final String DEPTH_KEY = "count";
    private static final String DIRECTION_KEY = "direction";
    private static final int DEFAULT_TICKS = 120;
    private static final int SAVE_INTERVAL = 20;
    private static final int NO_HEADING = -1;
    private static final int MIN_SPREAD_DEPTH = 1;
    private static final int RING_REACH = 1;
    private static final int RING_DEPTH = 1;
    private static final int SPARKLE_ATTEMPTS = 2;
    private static final int SPARKLE_SKIP_ODDS = 3;
    private static final int EDGES_PER_AXIS = 4;
    private static final int EDGE_COUNT = 12;
    private static final int AXIS_COUNT = 3;
    private static final int FIRST_SIDE_BIT = 1;
    private static final int SECOND_SIDE_BIT = 2;
    private static final float SPARKLE_RED = 0.25F;
    private static final float SPARKLE_GREEN = 0.25F;
    private static final float SPARKLE_BLUE = 1.0F;
    private static final float SPARKLE_ALPHA = 1.0F;
    private static final float SPARKLE_BASE_SCALE = 0.6F;
    private static final float SPARKLE_SCALE_RANGE = 0.2F;
    private static final int SPARKLE_DELAY = 0;
    private static final float SPARKLE_DECAY = 1.0F;
    private static final float SPARKLE_GRAVITY = 0.0F;
    private static final int SPARKLE_BASE_AGE = 2;
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Direction.Axis[] AXES = Direction.Axis.values();
    private static final int[][] RING_OFFSETS = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

    private BlockState replaced = Blocks.AIR.defaultBlockState();
    private int elapsed;
    private int maxTicks = DEFAULT_TICKS;
    private int depth;
    private @Nullable Direction heading;

    public BlockEntityHole(BlockPos pos, BlockState state) {
        super(TTBlockEntities.HOLE.get(), pos, state);
    }

    public void configure(BlockState replaced, int ticks, int depth, @Nullable Direction heading) {
        this.elapsed = 0;
        this.heading = heading;
        this.depth = depth;
        this.maxTicks = ticks;
        this.replaced = replaced;
        setChangedAndSync();
    }

    public BlockState originalBlockState() {
        return replaced;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityHole hole) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        hole.elapsed++;
        if (hole.depth > MIN_SPREAD_DEPTH && hole.heading != null) {
            hole.spread(server, pos, hole.heading);
        }
        if (hole.elapsed >= hole.maxTicks) {
            server.setBlock(pos, hole.replaced, Block.UPDATE_ALL);
            return;
        }
        if (hole.elapsed % SAVE_INTERVAL == 0) {
            hole.setChanged();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityHole hole) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < SPARKLE_ATTEMPTS; attempt++) {
            hole.sparkle(level, pos, random);
        }
    }

    private void spread(ServerLevel level, BlockPos pos, Direction facing) {
        heading = null;
        Direction.Axis first = AXES[(facing.getAxis().ordinal() + 1) % AXIS_COUNT];
        Direction.Axis second = AXES[(facing.getAxis().ordinal() + 2) % AXIS_COUNT];
        for (int[] offset : RING_OFFSETS) {
            BlockPos ringPos = pos.relative(first, offset[0] * RING_REACH).relative(second, offset[1] * RING_REACH);
            PortableHoles.open(level, ringPos, null, RING_DEPTH, maxTicks);
        }
        if (!PortableHoles.open(level, pos.relative(facing.getOpposite()), facing, depth - 1, maxTicks)) {
            depth = 0;
        }
        setChanged();
    }

    private void sparkle(Level level, BlockPos pos, RandomSource random) {
        int edge = random.nextInt(EDGE_COUNT);
        Direction.Axis free = AXES[edge / EDGES_PER_AXIS];
        Direction.Axis first = AXES[(free.ordinal() + 1) % AXIS_COUNT];
        Direction.Axis second = AXES[(free.ordinal() + 2) % AXIS_COUNT];
        int firstSide = (edge & FIRST_SIDE_BIT) == 0 ? -1 : 1;
        int secondSide = (edge & SECOND_SIDE_BIT) == 0 ? -1 : 1;
        if (!exposed(level, pos, first, firstSide, second, secondSide) || random.nextInt(SPARKLE_SKIP_ODDS) == 0) {
            return;
        }
        double along = random.nextDouble();
        double x = pos.getX() + offset(Direction.Axis.X, free, along, first, firstSide, secondSide);
        double y = pos.getY() + offset(Direction.Axis.Y, free, along, first, firstSide, secondSide);
        double z = pos.getZ() + offset(Direction.Axis.Z, free, along, first, firstSide, secondSide);
        int color = ARGB.colorFromFloat(SPARKLE_ALPHA, SPARKLE_RED, SPARKLE_GREEN, SPARKLE_BLUE);
        float scale = SPARKLE_BASE_SCALE + random.nextFloat() * SPARKLE_SCALE_RANGE;
        level.addParticle(new SparkleParticleOptions(color, scale, SPARKLE_DELAY, SPARKLE_DECAY, SPARKLE_GRAVITY, SPARKLE_BASE_AGE, false), x, y, z, 0.0, 0.0, 0.0);
    }

    private static double offset(Direction.Axis axis, Direction.Axis free, double along, Direction.Axis first, int firstSide, int secondSide) {
        if (axis == free) {
            return along;
        }
        return (axis == first ? firstSide : secondSide) > 0 ? 1.0 : 0.0;
    }

    private static boolean exposed(Level level, BlockPos pos, Direction.Axis first, int firstSide, Direction.Axis second, int secondSide) {
        BlockPos firstNeighbour = pos.relative(first, firstSide);
        BlockPos secondNeighbour = pos.relative(second, secondSide);
        BlockPos diagonal = firstNeighbour.relative(second, secondSide);
        boolean diagonalOpaque = opaque(level, diagonal);
        return open(level, firstNeighbour) && (opaque(level, secondNeighbour) || diagonalOpaque) || open(level, secondNeighbour) && (opaque(level, firstNeighbour) || diagonalOpaque);
    }

    private static boolean open(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.is(TTBlocks.HOLE.get()) && !state.isSolidRender();
    }

    private static boolean opaque(Level level, BlockPos pos) {
        return level.getBlockState(pos).isSolidRender();
    }

    private static int headingOrdinal(@Nullable Direction direction) {
        return direction != null ? direction.ordinal() : NO_HEADING;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(DIRECTION_KEY, headingOrdinal(heading));
        output.putInt(DEPTH_KEY, depth);
        output.putInt(COUNTDOWN_MAX_KEY, maxTicks);
        output.putInt(COUNTDOWN_KEY, elapsed);
        output.store(OLD_BLOCK_KEY, BlockState.CODEC, replaced);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        replaced = input.read(OLD_BLOCK_KEY, BlockState.CODEC).orElse(Blocks.AIR.defaultBlockState());
        elapsed = input.getIntOr(COUNTDOWN_KEY, 0);
        maxTicks = input.getIntOr(COUNTDOWN_MAX_KEY, DEFAULT_TICKS);
        depth = input.getIntOr(DEPTH_KEY, 0);
        int ordinal = input.getIntOr(DIRECTION_KEY, NO_HEADING);
        heading = ordinal >= 0 && ordinal < DIRECTIONS.length ? DIRECTIONS[ordinal] : null;
    }
}
