package com.leclowndu93150.thaumaturge.content.golem.press;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.research.DeviceGate;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockGolemBuilder extends BaseEntityBlock {
    public static final MapCodec<BlockGolemBuilder> CODEC = simpleCodec(BlockGolemBuilder::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final Identifier RESEARCH = TTIds.rl("mind_clockwork");
    private static final int RESTORE_REACH = 1;
    private static final int RESTORE_HEIGHT = 1;

    public BlockGolemBuilder(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<BlockGolemBuilder> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return GolemPressShapes.at(state.getValue(FACING), BlockPos.ZERO);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityGolemBuilder(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
                ? createTickerHelper(type, TTBlockEntities.GOLEM_BUILDER.get(), BlockEntityGolemBuilder::clientTick)
                : createTickerHelper(type, TTBlockEntities.GOLEM_BUILDER.get(), BlockEntityGolemBuilder::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return openBuilderGui(level, pos, player);
    }

    public static InteractionResult openBuilderGui(Level level, BlockPos core, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (DeviceGate.passes(player, RESEARCH) && level.getBlockEntity(core) instanceof BlockEntityGolemBuilder builder) {
            player.openMenu(builder, buf -> buf.writeBlockPos(core));
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        restoreStructure(level, pos, pos);
        super.destroy(level, pos, state);
    }

    public static void restoreStructure(LevelAccessor level, BlockPos core, BlockPos broken) {
        if (level.isClientSide()) {
            return;
        }
        for (BlockPos pos : BlockPos.betweenClosed(core.offset(-RESTORE_REACH, 0, -RESTORE_REACH), core.offset(RESTORE_REACH, RESTORE_HEIGHT, RESTORE_REACH))) {
            if (pos.equals(broken)) {
                continue;
            }
            BlockState counterpart = counterpart(level.getBlockState(pos));
            if (counterpart != null) {
                level.setBlock(pos, counterpart, Block.UPDATE_ALL);
            }
        }
        if (!broken.equals(core)) {
            level.setBlock(core, Blocks.PISTON.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP), Block.UPDATE_ALL);
        }
    }

    private static @Nullable BlockState counterpart(BlockState placeholder) {
        if (placeholder.is(TTBlocks.PLACEHOLDER_IRON_BARS)) {
            return Blocks.IRON_BARS.defaultBlockState();
        }
        if (placeholder.is(TTBlocks.PLACEHOLDER_ANVIL)) {
            return Blocks.ANVIL.defaultBlockState();
        }
        if (placeholder.is(TTBlocks.PLACEHOLDER_CAULDRON)) {
            return Blocks.CAULDRON.defaultBlockState();
        }
        if (placeholder.is(TTBlocks.PLACEHOLDER_TABLE)) {
            return TTBlocks.TABLE_STONE.get().defaultBlockState();
        }
        return null;
    }
}
