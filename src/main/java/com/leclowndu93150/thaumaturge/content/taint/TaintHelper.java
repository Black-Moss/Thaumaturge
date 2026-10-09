package com.leclowndu93150.thaumaturge.content.taint;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.taint.ITaintBlock;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintSeed;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSeed;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFeature;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintSeedRegistry;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public final class TaintHelper {
    private static final double EDGE_INNER_FRACTION = 0.8;
    private static final double SEED_ENTITY_INFLATE = 1.0;
    private static final double SEED_CROWD_FRACTION = 0.8;
    private static final float SEED_MIN_FLUX = 5.0F;
    private static final float SEED_FLUX_DRAIN = 5.0F;
    private static final float SEED_PRESSURE = 0.08F;
    private static final float SEED_MIN_SATURATION = 0.85F;
    private static final int SEED_SPAWN_ONE_IN = 100;
    private static final float FULL_TURN_DEGREES = 360.0F;
    private static final int SATELLITE_ATTEMPTS = 8;
    private static final int SATELLITE_RADIUS = 4;
    private static final int SATELLITE_HEIGHT = 1;
    private static final int SPREAD_RADIUS = 1;
    private static final int SPREAD_HEIGHT = 2;
    private static final int PERCENT = 100;
    private static final float MAX_TARGET_HARDNESS = 10.0F;
    private static final float MAX_CONVERT_HARDNESS = 5.0F;
    private static final float PLACE_PRESSURE = 0.01F;
    private static final float CONVERT_PRESSURE = 0.01F;
    private static final int LEAF_FEATURE_PERCENT = 60;
    private static final int LOG_ADJACENT_REQUIRED = 2;
    private static final int CRUST_ADJACENT_REQUIRED = 2;
    private static final int SOIL_ADJACENT_REQUIRED = 3;
    private static final int ROCK_ADJACENT_REQUIRED = 3;
    private static final int FRONTIER_MIN_NEIGHBOURS = 2;
    private static final int FRONTIER_ROLL_FACTOR = 5;
    private static final float FRONTIER_MAX_SATURATION = 2.0F;
    private static final float FRONTIER_BASE_RATE = 1.0F;
    private static final float FRONTIER_RATE_SCALE = 0.5F;
    private static final float FRONTIER_RATE_CAP = 0.5F;
    private static final float FRONTIER_PRESSURE_BASE = 0.01F;
    private static final float FRONTIER_PRESSURE_SCALE = 0.01F;
    private static final float FRONTIER_PRESSURE_CAP = 0.02F;

    private static final List<Conversion> CONVERSIONS = List.of(new Conversion(TTBlockTags.TAINT_CONVERTIBLE_LOG, LOG_ADJACENT_REQUIRED, state -> TTBlocks.TAINT_LOG.get().withAxis(axisOf(state))),
            new Conversion(TTBlockTags.TAINT_CONVERTIBLE_CRUST, CRUST_ADJACENT_REQUIRED, state -> TTBlocks.TAINT_CRUST.get().defaultBlockState()),
            new Conversion(TTBlockTags.TAINT_CONVERTIBLE_SOIL, SOIL_ADJACENT_REQUIRED, state -> TTBlocks.TAINT_SOIL.get().defaultBlockState()),
            new Conversion(TTBlockTags.TAINT_CONVERTIBLE_ROCK, ROCK_ADJACENT_REQUIRED, state -> TTBlocks.TAINT_ROCK.get().defaultBlockState()));

    private TaintHelper() {}

    public static double spreadArea() {
        return ThaumaturgeCommonConfig.TAINT_SPREAD_AREA.get();
    }

    private static TaintSeedRegistry seeds(ServerLevel level) {
        return TaintSeedRegistry.get(level);
    }

    public static void registerSeedAnchor(ServerLevel level, BlockPos pos) {
        seeds(level).addSeed(pos);
    }

    public static void unregisterSeedAnchor(ServerLevel level, BlockPos pos) {
        seeds(level).removeSeed(pos);
    }

    public static boolean isWithinSeedInfluence(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) {
            return false;
        }
        TaintSeedRegistry registry = TaintSeedRegistry.get(server);
        double radius = spreadArea();
        double radiusSq = radius * radius;
        List<BlockPos> stale = new ArrayList<>();
        boolean near = false;
        for (BlockPos seed : registry.all()) {
            if (seed.distSqr(pos) > radiusSq) {
                continue;
            }
            if (server.getEntitiesOfClass(AbstractTaintSeed.class, new AABB(seed).inflate(SEED_ENTITY_INFLATE)).isEmpty()) {
                stale.add(seed);
            } else {
                near = true;
                break;
            }
        }
        for (BlockPos seed : stale) {
            registry.removeSeed(seed);
        }
        return near;
    }

    public static boolean isEcologicallySustained(ServerLevel level, BlockPos pos) {
        return TaintBiomeManager.isTainted(level, pos) || isWithinSeedInfluence(level, pos);
    }

    public static boolean isOnSeedFringe(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) {
            return false;
        }
        double outerRadius = spreadArea();
        double innerRadius = outerRadius * EDGE_INNER_FRACTION;
        double outerSq = outerRadius * outerRadius;
        double innerSq = innerRadius * innerRadius;
        for (BlockPos seed : seeds(server).all()) {
            if (strictlyBetween(seed.distSqr(pos), innerSq, outerSq)) {
                return true;
            }
        }
        return false;
    }

    private static boolean strictlyBetween(double value, double lower, double upper) {
        return value > lower && value < upper;
    }

    public static boolean hasSturdyNeighbour(LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbour = pos.relative(direction);
            if (level.getBlockState(neighbour).isFaceSturdy(level, neighbour, direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    public static int countAdjacentTaint(LevelReader level, BlockPos pos) {
        int count = 0;
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).getBlock() instanceof ITaintBlock) {
                count++;
            }
        }
        return count;
    }

    public static boolean placeFibreFromFlux(ServerLevel level, BlockPos pos) {
        boolean permitted = ThaumaturgeCommonConfig.TAINT_FROM_FLUX.get() && !ThaumaturgeCommonConfig.WUSS_MODE.get() && !TaintBlooms.isProtected(level, pos);
        return permitted && level.setBlock(pos, BlockTaintFibre.stateForWorld(level, pos), Block.UPDATE_ALL);
    }

    public static void attemptFibreGrowth(ServerLevel level, BlockPos origin, boolean force) {
        RandomSource random = level.getRandom();
        if (!originAllowsGrowth(level, origin) || !spreadRollPasses(random, force)) {
            return;
        }
        BlockPos target = pickTarget(origin, random);
        if (!level.hasChunkAt(target) || TaintBlooms.isProtected(level, target)) {
            return;
        }
        BlockState state = level.getBlockState(target);
        float hardness = state.getDestroySpeed(level, target);
        boolean breakable = hardness >= 0.0F && hardness <= MAX_TARGET_HARDNESS;
        boolean biomeOk = force || TaintBiomeManager.isTainted(level, target);
        if (!breakable || !biomeOk || state.is(TTBlockTags.TAINT_CONVERSION_IMMUNE)) {
            return;
        }
        if (isOpenSpace(state)) {
            placeFibre(level, target, force);
            return;
        }
        if (state.is(BlockTags.LEAVES)) {
            growFeatureOnLeaves(level, target, random);
            return;
        }
        Conversion conversion = findConversion(level, target, state, hardness);
        if (conversion != null) {
            convert(level, target, state, conversion, force);
            return;
        }
        if (random.nextInt(SEED_SPAWN_ONE_IN) == 0 && TaintEcology.getSaturation(level, target) >= SEED_MIN_SATURATION) {
            trySpawnSeed(level, target, true);
        }
    }

    private static boolean originAllowsGrowth(ServerLevel level, BlockPos origin) {
        return !ThaumaturgeCommonConfig.WUSS_MODE.get() && level.hasChunkAt(origin) && !TaintBlooms.isProtected(level, origin);
    }

    private static boolean spreadRollPasses(RandomSource random, boolean force) {
        return force || random.nextDouble() * PERCENT < ThaumaturgeCommonConfig.TAINT_SPREAD_RATE.get();
    }

    public static boolean canHostFoothold(BlockState state) {
        return PhysicalFlux.isPhysicalFlux(state) || (state.canBeReplaced() && state.getFluidState().isEmpty());
    }

    public static void establishFoothold(ServerLevel level, BlockPos pos, float pressure, int spreadAttempts) {
        if (canHostFoothold(level.getBlockState(pos))) {
            level.setBlock(pos, BlockTaintFibre.stateForWorld(level, pos), Block.UPDATE_ALL);
        }
        TaintEcology.addPressure(level, pos, pressure);
        for (int attempt = 0; attempt < spreadAttempts; attempt++) {
            attemptFibreGrowth(level, pos, true);
        }
    }

    public static boolean trySpreadTaintedBiome(ServerLevel level, BlockPos pos, RandomSource random) {
        int rate = ThaumaturgeCommonConfig.TAINT_FRONTIER_RATE.get();
        if (ThaumaturgeCommonConfig.WUSS_MODE.get() || rate <= 0 || !TaintBiomeManager.isTainted(level, pos) || countAdjacentTaint(level, pos) < FRONTIER_MIN_NEIGHBOURS
                || TaintBlooms.isProtected(level, pos)) {
            return false;
        }
        float saturation = Math.clamp(AuraHelper.getFluxSaturation(level, pos), 0.0F, FRONTIER_MAX_SATURATION);
        float acceleration = FRONTIER_BASE_RATE + Math.min(FRONTIER_RATE_CAP, FRONTIER_RATE_SCALE * saturation);
        int bound = Math.max(1, Math.round(FRONTIER_ROLL_FACTOR * rate / acceleration));
        if (random.nextInt(bound) != 0) {
            return false;
        }
        BlockPos column = pos.offset(jitter(random, SPREAD_RADIUS), 0, jitter(random, SPREAD_RADIUS));
        if (!level.hasChunkAt(column) || !TaintBiomeManager.taintColumn(level, column)) {
            return false;
        }
        TaintEcology.addPressure(level, column, FRONTIER_PRESSURE_BASE + Math.min(FRONTIER_PRESSURE_CAP, FRONTIER_PRESSURE_SCALE * saturation));
        return true;
    }

    public static boolean trySpawnSatelliteSeed(ServerLevel level, BlockPos origin, RandomSource random) {
        if (!satelliteAllowed(level, origin)) {
            return false;
        }
        for (int remaining = SATELLITE_ATTEMPTS; remaining > 0; remaining--) {
            BlockPos target = origin.offset(jitter(random, SATELLITE_RADIUS), jitter(random, SATELLITE_HEIGHT), jitter(random, SATELLITE_RADIUS));
            if (level.hasChunkAt(target) && trySpawnSeed(level, target, false)) {
                return true;
            }
        }
        return false;
    }

    private static boolean satelliteAllowed(ServerLevel level, BlockPos origin) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get() || level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        return !TaintBlooms.isProtected(level, origin) && TaintEcology.getSaturation(level, origin) >= SEED_MIN_SATURATION;
    }

    private static int jitter(RandomSource random, int extent) {
        return random.nextInt(2 * extent + 1) - extent;
    }

    private static BlockPos pickTarget(BlockPos origin, RandomSource random) {
        while (true) {
            int dx = jitter(random, SPREAD_RADIUS);
            int dy = jitter(random, SPREAD_HEIGHT);
            int dz = jitter(random, SPREAD_RADIUS);
            if ((dx | dy | dz) != 0) {
                return origin.offset(dx, dy, dz);
            }
        }
    }

    private static boolean isOpenSpace(BlockState state) {
        return state.isAir() || (state.canBeReplaced() && !state.is(BlockTags.LEAVES) && state.getFluidState().isEmpty());
    }

    private static boolean columnTainted(ServerLevel level, BlockPos pos, boolean force) {
        return TaintBiomeManager.isTainted(level, pos) || (force && TaintBiomeManager.taintColumn(level, pos));
    }

    private static void placeFibre(ServerLevel level, BlockPos target, boolean force) {
        if (!BlockTaintFibre.hasSolidAttachment(level, target) || BlockTaintFibre.isOnlyAdjacentToTaint(level, target)) {
            return;
        }
        if (!columnTainted(level, target, force)) {
            return;
        }
        level.setBlock(target, BlockTaintFibre.stateForWorld(level, target), Block.UPDATE_ALL);
        TaintEcology.addPressure(level, target, PLACE_PRESSURE);
    }

    private static void growFeatureOnLeaves(ServerLevel level, BlockPos leaves, RandomSource random) {
        Direction logSide = pickLogSide(level, leaves, random);
        if (logSide == null || random.nextInt(PERCENT) >= LEAF_FEATURE_PERCENT) {
            return;
        }
        BlockState feature = TTBlocks.TAINT_FEATURE.get().defaultBlockState().setValue(BlockTaintFeature.FACING, logSide.getOpposite());
        level.setBlock(leaves, feature, Block.UPDATE_ALL);
    }

    private static @Nullable Direction pickLogSide(ServerLevel level, BlockPos leaves, RandomSource random) {
        Direction chosen = null;
        int seen = 0;
        for (Direction side : Direction.values()) {
            if (!level.getBlockState(leaves.relative(side)).is(TTBlocks.TAINT_LOG)) {
                continue;
            }
            seen++;
            if (random.nextInt(seen) == 0) {
                chosen = side;
            }
        }
        return chosen;
    }

    private static @Nullable Conversion findConversion(ServerLevel level, BlockPos target, BlockState state, float hardness) {
        if (state.getBlock() instanceof ITaintBlock || hardness >= MAX_CONVERT_HARDNESS) {
            return null;
        }
        int adjacent = countAdjacentTaint(level, target);
        return CONVERSIONS.stream().filter(candidate -> candidate.accepts(state, adjacent)).findFirst().orElse(null);
    }

    private static void convert(ServerLevel level, BlockPos target, BlockState state, Conversion conversion, boolean force) {
        if (columnTainted(level, target, force)) {
            level.setBlock(target, conversion.result().apply(state), Block.UPDATE_ALL);
            TaintEcology.addPressure(level, target, CONVERT_PRESSURE);
        }
    }

    private static Direction.Axis axisOf(BlockState state) {
        return state.hasProperty(RotatedPillarBlock.AXIS) ? state.getValue(RotatedPillarBlock.AXIS) : Direction.Axis.Y;
    }

    private static boolean seedSiteValid(ServerLevel level, BlockPos target, boolean requireEdge) {
        BlockState ground = level.getBlockState(target);
        if (!ground.is(TTBlocks.TAINT_SOIL) && !ground.is(TTBlocks.TAINT_ROCK)) {
            return false;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL || !level.getBlockState(target.above()).isAir()) {
            return false;
        }
        return AuraHelper.getFlux(level, target) >= SEED_MIN_FLUX && (!requireEdge || isOnSeedFringe(level, target));
    }

    private static @Nullable EntityTaintSeed createSeedAbove(ServerLevel level, BlockPos target) {
        EntityTaintSeed seed = TTEntities.TAINT_SEED.get().create(level, EntitySpawnReason.NATURAL);
        if (seed == null) {
            return null;
        }
        seed.snapTo(target.getX() + 0.5, target.getY() + 1.0, target.getZ() + 0.5, level.getRandom().nextFloat() * FULL_TURN_DEGREES, 0.0F);
        AABB box = seed.getBoundingBox();
        boolean crowded = !level.getEntitiesOfClass(AbstractTaintSeed.class, box.inflate(spreadArea() * SEED_CROWD_FRACTION)).isEmpty();
        return level.noBlockCollision(seed, box) && !crowded ? seed : null;
    }

    private static boolean trySpawnSeed(ServerLevel level, BlockPos target, boolean requireEdge) {
        if (!seedSiteValid(level, target, requireEdge)) {
            return false;
        }
        EntityTaintSeed seed = createSeedAbove(level, target);
        if (seed == null) {
            return false;
        }
        AuraHelper.drainFlux(level, target, SEED_FLUX_DRAIN, false);
        level.addFreshEntity(seed);
        TaintEcology.addPressure(level, target, SEED_PRESSURE);
        return true;
    }

    private record Conversion(TagKey<Block> tag, int minAdjacent, UnaryOperator<BlockState> result) {
        boolean accepts(BlockState state, int adjacentTaint) {
            return adjacentTaint >= minAdjacent && state.is(tag);
        }
    }
}
