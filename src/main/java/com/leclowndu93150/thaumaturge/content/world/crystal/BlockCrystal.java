package com.leclowndu93150.thaumaturge.content.world.crystal;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockCrystal extends Block {
    private static final int MAX_SIZE = 3;
    private static final int MAX_GENERATION = 4;
    private static final int MIN_GENERATION = 1;

    public static final IntegerProperty SIZE = IntegerProperty.create("size", 0, MAX_SIZE);
    public static final IntegerProperty GENERATION = IntegerProperty.create("gen", MIN_GENERATION, MAX_GENERATION);

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final MapCodec<BlockCrystal> CODEC = simpleCodec(properties -> new BlockCrystal(properties, null, false));
    private static final int MAX_SPAWN_SIZE = 0;
    private static final int GROWTH_LIMIT_BASE = 5;
    private static final int GROWTH_BONUS_VALUES = 3;
    private static final int ACTIVITY_BASE = 3;
    private static final int SPREAD_ACCEPT_ONE_IN = 16;
    private static final int SAME_GENERATION_ONE_IN = 6;
    private static final int SUPPORT_RECHECK_DELAY = 1;
    private static final int CHANGE_FLAGS = 3;
    private static final int SPREAD_REACH = 1;
    private static final int SPREAD_SPAN = 2 * SPREAD_REACH + 1;
    private static final float THRESHOLD = 10.0F;
    private static final float TRANSFER = 10.0F;
    private static final float DECAY_FLUX = 1.0F;
    private static final float DRAIN_TOLERANCE = 0.001F;
    private static final float FLUX_CONVERSION_DIVISOR = 2.0F;

    private final @Nullable ResourceKey<IAspect> aspect;
    private final boolean flux;

    public BlockCrystal(Properties properties, @Nullable ResourceKey<IAspect> aspect, boolean flux) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(GENERATION, MIN_GENERATION).setValue(SIZE, 0));
        this.flux = flux;
        this.aspect = aspect;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    public @Nullable ResourceKey<IAspect> aspect() {
        return aspect;
    }

    public boolean isFlux() {
        return flux;
    }

    public int growth(BlockState state) {
        return state.getValue(SIZE);
    }

    public int generation(BlockState state) {
        return state.getValue(GENERATION);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SIZE, GENERATION);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CrystalShapes.shapeFor(state, level, pos);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return hasSupport(level, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        boolean unsupported = !hasSupport(level, pos);
        if (unsupported && !level.isClientSide()) {
            ticks.scheduleTick(pos, this, SUPPORT_RECHECK_DELAY);
        }
        return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!hasSupport(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int rollBound = ACTIVITY_BASE + generation(state);
        if (random.nextInt(rollBound) > 0) {
            return;
        }
        float base = AuraHelper.getAuraBase(level, pos);
        float stock = currentStock(level, pos);
        switch (classify(stock, base)) {
            case STARVED -> starve(state, level, pos);
            case RICH -> thrive(state, level, pos, random);
            case MIDDLE -> {
                if (!flux) {
                    convertToFlux(state, level, pos, stock, base);
                }
            }
        }
    }

    private static AuraBand classify(float stock, float base) {
        if (stock <= THRESHOLD) {
            return AuraBand.STARVED;
        }
        return stock > base + THRESHOLD ? AuraBand.RICH : AuraBand.MIDDLE;
    }

    private float currentStock(ServerLevel level, BlockPos pos) {
        return flux ? AuraHelper.getFlux(level, pos) : AuraHelper.getVis(level, pos);
    }

    private static boolean hasSupport(LevelReader level, BlockPos pos) {
        return Arrays.stream(DIRECTIONS).anyMatch(side -> CrystalShards.supports(level, pos, side));
    }

    private void starve(BlockState state, ServerLevel level, BlockPos pos) {
        int size = growth(state);
        boolean depleted = size == 0;
        if (depleted && !touchesSibling(level, pos)) {
            return;
        }
        BlockState next = depleted ? Blocks.AIR.defaultBlockState() : state.setValue(SIZE, size - 1);
        level.setBlock(pos, next, CHANGE_FLAGS);
        returnToAura(level, pos);
        if (depleted && !flux) {
            AuraHelper.addFlux(level, pos, DECAY_FLUX);
        }
    }

    private void thrive(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int generation = generation(state);
        int growthBonus = (int) Math.floorMod(state.getSeed(pos), (long) GROWTH_BONUS_VALUES);
        int sizeCeiling = Math.min(MAX_SIZE, GROWTH_LIMIT_BASE - generation + growthBonus);
        if (growth(state) < sizeCeiling) {
            grow(state, level, pos);
        } else if (generation < MAX_GENERATION) {
            spread(level, pos, random, generation);
        }
    }

    private void grow(BlockState state, ServerLevel level, BlockPos pos) {
        if (drain(level, pos, TRANSFER) > 0.0F) {
            level.setBlock(pos, state.setValue(SIZE, growth(state) + 1), CHANGE_FLAGS);
        }
    }

    private void spread(ServerLevel level, BlockPos pos, RandomSource random, int generation) {
        BlockPos target = pickSpreadTarget(level, pos, random);
        if (target != null && drain(level, pos, TRANSFER) > 0.0F) {
            int childGeneration = random.nextInt(SAME_GENERATION_ONE_IN) == 0 ? generation : generation + 1;
            level.setBlock(target, offspring(childGeneration), CHANGE_FLAGS);
        }
    }

    private BlockState offspring(int generation) {
        return defaultBlockState().setValue(SIZE, MAX_SPAWN_SIZE).setValue(GENERATION, generation);
    }

    private void convertToFlux(BlockState state, ServerLevel level, BlockPos pos, float vis, float base) {
        float fluxStock = AuraHelper.getFlux(level, pos);
        boolean fluxDominates = fluxStock > vis && fluxStock > base / FLUX_CONVERSION_DIVISOR;
        if (!fluxDominates) {
            return;
        }
        float cost = growth(state) + 1;
        float drained = AuraHelper.drainFlux(level, pos, cost, false);
        if (drained < cost - DRAIN_TOLERANCE) {
            return;
        }
        BlockState converted = TTBlocks.CRYSTAL_VITIUM.get().defaultBlockState().setValue(SIZE, growth(state)).setValue(GENERATION, generation(state));
        level.setBlock(pos, converted, CHANGE_FLAGS);
    }

    private @Nullable BlockPos pickSpreadTarget(ServerLevel level, BlockPos pos, RandomSource random) {
        int dx = random.nextInt(SPREAD_SPAN) - SPREAD_REACH;
        int dy = random.nextInt(SPREAD_SPAN) - SPREAD_REACH;
        int dz = random.nextInt(SPREAD_SPAN) - SPREAD_REACH;
        if (dx == 0 && dy == 0 && dz == 0) {
            return null;
        }
        if (random.nextInt(SPREAD_ACCEPT_ONE_IN) != 0) {
            return null;
        }
        BlockPos target = pos.offset(dx, dy, dz);
        return !level.isOutsideBuildHeight(target) && canReceiveSpread(level, target) ? target : null;
    }

    private static boolean canReceiveSpread(ServerLevel level, BlockPos target) {
        BlockState occupant = level.getBlockState(target);
        if (!occupant.getFluidState().isEmpty()) {
            return false;
        }
        return (occupant.isAir() || occupant.canBeReplaced()) && hasSupport(level, target);
    }

    private boolean touchesSibling(ServerLevel level, BlockPos pos) {
        for (Direction direction : DIRECTIONS) {
            if (level.getBlockState(pos.relative(direction)).is(this)) {
                return true;
            }
        }
        return false;
    }

    private float drain(ServerLevel level, BlockPos pos, float amount) {
        return flux ? AuraHelper.drainFlux(level, pos, amount, false) : AuraHelper.drainVis(level, pos, amount, false);
    }

    private void returnToAura(ServerLevel level, BlockPos pos) {
        if (flux) {
            AuraHelper.addFlux(level, pos, TRANSFER);
        } else {
            AuraHelper.addVis(level, pos, TRANSFER);
        }
    }

    private enum AuraBand {
        STARVED, RICH, MIDDLE
    }
}
