package com.leclowndu93150.thaumaturge.content.manabean;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockManaPod extends BaseEntityBlock {
    public static final MapCodec<BlockManaPod> CODEC = simpleCodec(BlockManaPod::new);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    private static final int SUPPORT_LOSS_TICK_DELAY = 1;
    private static final int GROWTH_CHANCE_BOUND = 30;
    private static final int BREAK_SPEED_BASE = BlockEntityManaPod.MAX_AGE + 1;
    private static final int SPROUT_AGE = 0;
    private static final int SHOOT_AGE = 1;
    private static final VoxelShape SPROUT = Block.box(5.0, 11.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape SHOOT = Block.box(4.0, 9.0, 4.0, 12.0, 16.0, 12.0);
    private static final VoxelShape POD = Shapes.or(Block.box(5.0, 12.0, 5.0, 11.0, 16.0, 11.0), Block.box(6.5, 11.0, 6.5, 9.5, 12.0, 9.5), Block.box(5.5, 10.0, 5.5, 10.5, 11.0, 10.5),
            Block.box(4.5, 6.0, 4.5, 11.5, 10.0, 11.5), Block.box(5.5, 4.0, 5.5, 10.5, 6.0, 10.5), Block.box(6.5, 3.0, 6.5, 9.5, 4.0, 9.5), Block.box(7.5, 2.0, 7.5, 8.5, 3.0, 8.5)).optimize();
    private static final VoxelShape[] SHAPES = buildShapes();

    public BlockManaPod(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }

    private static VoxelShape[] buildShapes() {
        VoxelShape[] shapes = new VoxelShape[BlockEntityManaPod.MAX_AGE + 1];
        Arrays.fill(shapes, POD);
        shapes[SPROUT_AGE] = SPROUT;
        shapes[SHOOT_AGE] = SHOOT;
        return shapes;
    }

    @Override
    protected MapCodec<BlockManaPod> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public static boolean canGrowAt(LevelReader level, BlockPos pos) {
        return level.getBiome(pos).is(TTBiomeTags.IS_MAGICAL) && level.getBlockState(pos.above()).is(BlockTags.LOGS);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canGrowAt(level, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (directionToNeighbour == Direction.UP && !neighbourState.is(BlockTags.LOGS)) {
            ticks.scheduleTick(pos, this, SUPPORT_LOSS_TICK_DELAY);
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        dropIfUnsupported(state, level, pos);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (dropIfUnsupported(state, level, pos)) {
            return;
        }
        if (random.nextInt(GROWTH_CHANCE_BOUND) != 0) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof BlockEntityManaPod pod) {
            pod.checkGrowth();
        }
    }

    private boolean dropIfUnsupported(BlockState state, ServerLevel level, BlockPos pos) {
        boolean unsupported = !canSurvive(state, level, pos);
        if (unsupported) {
            level.destroyBlock(pos, true);
        }
        return unsupported;
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        float multiplier = BREAK_SPEED_BASE - state.getValue(AGE);
        return multiplier * super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack bean = new ItemStack(TTItems.MANA_BEAN.get());
        if (!(level.getBlockEntity(pos) instanceof BlockEntityManaPod pod)) {
            return bean;
        }
        Holder<IAspect> aspect = pod.aspect();
        if (aspect != null) {
            bean.set(TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(aspect, 1));
        }
        return bean;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityManaPod(pos, state);
    }
}
