package com.leclowndu93150.thaumaturge.content.device.levitator;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockLevitator extends BaseEntityBlock {
    private static final float KEY_VOLUME = 0.5F;
    private static final float KEY_PITCH = 1.0F;
    private static final double INSET = 2.0;
    private static final double FULL = 16.0;
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes();

    public static final MapCodec<BlockLevitator> CODEC = simpleCodec(BlockLevitator::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public BlockLevitator(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(BlockStateProperties.ENABLED, true));
    }

    @Override
    protected MapCodec<BlockLevitator> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BlockStateProperties.ENABLED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction looking = context.getNearestLookingDirection();
        Direction facing = context.isSecondaryUseActive() ? looking : looking.getOpposite();
        return defaultBlockState().setValue(FACING, facing).setValue(BlockStateProperties.ENABLED, !context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof BlockEntityLevitator levitator) {
            levitator.markClearanceStale();
        }
        if (level.isClientSide()) {
            return;
        }
        boolean enabled = !level.hasNeighborSignal(pos);
        if (enabled != state.getValue(BlockStateProperties.ENABLED)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.ENABLED, enabled), Block.UPDATE_ALL);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (hit.getDirection() != state.getValue(FACING).getOpposite() || !(level.getBlockEntity(pos) instanceof BlockEntityLevitator levitator)) {
            return InteractionResult.PASS;
        }
        levitator.cycleReach(player);
        if (!level.isClientSide()) {
            level.playSound(null, pos, TTSounds.KEY.get(), SoundSource.BLOCKS, KEY_VOLUME, KEY_PITCH);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityLevitator(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TTBlockEntities.LEVITATOR.get(), BlockEntityLevitator::tick);
    }

    private static Map<Direction, VoxelShape> buildShapes() {
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        shapes.put(Direction.UP, box(0.0, INSET, 0.0, FULL, FULL, FULL));
        shapes.put(Direction.DOWN, box(0.0, 0.0, 0.0, FULL, FULL - INSET, FULL));
        shapes.put(Direction.EAST, box(INSET, 0.0, 0.0, FULL, FULL, FULL));
        shapes.put(Direction.WEST, box(0.0, 0.0, 0.0, FULL - INSET, FULL, FULL));
        shapes.put(Direction.SOUTH, box(0.0, 0.0, INSET, FULL, FULL, FULL));
        shapes.put(Direction.NORTH, box(0.0, 0.0, 0.0, FULL, FULL, FULL - INSET));
        return shapes;
    }
}
