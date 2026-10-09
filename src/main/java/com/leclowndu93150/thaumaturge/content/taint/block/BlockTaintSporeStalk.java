package com.leclowndu93150.thaumaturge.content.taint.block;

import com.leclowndu93150.thaumaturge.api.taint.ITaintBlock;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.content.taint.entity.AbstractTaintSpore;
import com.leclowndu93150.thaumaturge.content.taint.entity.EntityTaintSporeSwarmer;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockTaintSporeStalk extends Block implements ITaintBlock {
    public static final MapCodec<BlockTaintSporeStalk> CODEC = simpleCodec(BlockTaintSporeStalk::new);
    public static final BooleanProperty MATURE = BooleanProperty.create("mature");

    private static final double SHAPE_MIN_XZ = 4.0;
    private static final double SHAPE_MAX_XZ = 12.0;
    private static final double SHAPE_HEIGHT = 14.0;
    private static final VoxelShape SHAPE = Block.box(SHAPE_MIN_XZ, 0.0, SHAPE_MIN_XZ, SHAPE_MAX_XZ, SHAPE_HEIGHT, SHAPE_MAX_XZ);
    private static final double OCCUPANCY_INFLATE = 0.25;
    private static final int PRODUCE_ONE_IN = 10;
    private static final int SPORE_CROWD_RADIUS = 24;
    private static final int SPORE_CROWD_LIMIT = 6;
    private static final float SWARMER_MIN_SATURATION = 0.85F;
    private static final int SWARMER_ONE_IN = 20;
    private static final int SWARMER_SPACING = 16;
    private static final double SPORE_HEIGHT = 1.0;
    private static final double CELL_CENTRE = 0.5;
    private static final float PRODUCE_PRESSURE = 0.03F;

    public BlockTaintSporeStalk(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(MATURE, false));
    }

    @Override
    public MapCodec<BlockTaintSporeStalk> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MATURE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!mayProduce(level, pos)) {
            return;
        }
        boolean occupied = !level.getEntitiesOfClass(AbstractTaintSpore.class, new AABB(pos.above()).inflate(OCCUPANCY_INFLATE)).isEmpty();
        if (state.getValue(MATURE)) {
            if (!occupied) {
                level.setBlock(pos, state.setValue(MATURE, false), Block.UPDATE_CLIENTS);
            }
            return;
        }
        if (occupied || random.nextInt(PRODUCE_ONE_IN) != 0 || !level.getBlockState(pos.above()).isAir()) {
            return;
        }
        if (level.getEntitiesOfClass(AbstractTaintSpore.class, new AABB(pos).inflate(SPORE_CROWD_RADIUS)).size() >= SPORE_CROWD_LIMIT) {
            return;
        }
        produce(state, level, pos, random);
    }

    private static boolean mayProduce(ServerLevel level, BlockPos pos) {
        return !ThaumaturgeCommonConfig.WUSS_MODE.get() && level.getDifficulty() != Difficulty.PEACEFUL && !TaintBlooms.isProtected(level, pos) && TaintEcology.isTainted(level, pos);
    }

    private static boolean wantsSwarmer(ServerLevel level, BlockPos pos, RandomSource random) {
        return TaintEcology.getSaturation(level, pos) >= SWARMER_MIN_SATURATION && random.nextInt(SWARMER_ONE_IN) == 0
                && level.getEntitiesOfClass(EntityTaintSporeSwarmer.class, new AABB(pos).inflate(SWARMER_SPACING)).isEmpty();
    }

    private static void produce(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        EntityType<? extends AbstractTaintSpore> type = wantsSwarmer(level, pos, random) ? TTEntities.TAINT_SPORE_SWARMER.get() : TTEntities.TAINT_SPORE.get();
        AbstractTaintSpore spore = type.create(level, EntitySpawnReason.NATURAL);
        if (spore == null) {
            return;
        }
        spore.snapTo(pos.getX() + CELL_CENTRE, pos.getY() + SPORE_HEIGHT, pos.getZ() + CELL_CENTRE, 0.0F, 0.0F);
        level.addFreshEntity(spore);
        level.setBlock(pos, state.setValue(MATURE, true), Block.UPDATE_CLIENTS);
        TaintEcology.addPressure(level, pos, PRODUCE_PRESSURE);
    }

    @Override
    public void decay(Level level, BlockPos pos, BlockState state) {
        level.removeBlock(pos, false);
    }
}
