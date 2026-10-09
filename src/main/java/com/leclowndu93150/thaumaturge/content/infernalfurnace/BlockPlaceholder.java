package com.leclowndu93150.thaumaturge.content.infernalfurnace;

import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.AdvancedAlchemicalFurnaceStructure;
import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.AdvancedFurnaceShapes;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockGolemBuilder;
import com.leclowndu93150.thaumaturge.content.golem.press.GolemPressShapes;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import java.util.List;
import org.jspecify.annotations.Nullable;

public class BlockPlaceholder extends Block {
    private static final int SEARCH_RADIUS = 1;
    private static final int SHAPE_SEARCH_MIN_Y = -1;
    private static final int SHAPE_SEARCH_MAX_Y = 0;
    private static final int SHAPE_SEARCH_WIDTH = 2 * SEARCH_RADIUS + 1;
    private static final int SHAPE_SEARCH_HEIGHT = SHAPE_SEARCH_MAX_Y - SHAPE_SEARCH_MIN_Y + 1;
    private static final int SHAPE_SEARCH_CELLS = SHAPE_SEARCH_WIDTH * SHAPE_SEARCH_HEIGHT * SHAPE_SEARCH_WIDTH;
    private static final float FULL_SHADE = 1.0F;
    private static final int NO_DAMPENING = 0;
    private static final List<DeferredBlock<BlockPlaceholder>> SHELL_BLOCKS = List.of(TTBlocks.NETHER_BRICKS_PLACEHOLDER, TTBlocks.OBSIDIAN_PLACEHOLDER);
    private static final List<DeferredBlock<BlockPlaceholder>> PRESS_BLOCKS = List.of(TTBlocks.PLACEHOLDER_IRON_BARS, TTBlocks.PLACEHOLDER_ANVIL, TTBlocks.PLACEHOLDER_CAULDRON,
            TTBlocks.PLACEHOLDER_TABLE);

    private final boolean visible;

    public BlockPlaceholder(BlockBehaviour.Properties properties) {
        this(properties, false);
    }

    public BlockPlaceholder(BlockBehaviour.Properties properties, boolean visible) {
        super(properties);
        this.visible = visible;
    }

    private enum Role {
        SHELL, PRESS, FURNACE_PART, PLAIN
    }

    private static boolean matchesAny(BlockState state, List<? extends Holder<Block>> candidates) {
        for (Holder<Block> candidate : candidates) {
            if (state.is(candidate)) {
                return true;
            }
        }
        return false;
    }

    private static Role roleOf(BlockState state) {
        if (matchesAny(state, SHELL_BLOCKS)) {
            return Role.SHELL;
        }
        if (matchesAny(state, PRESS_BLOCKS)) {
            return Role.PRESS;
        }
        return AdvancedAlchemicalFurnaceStructure.isPart(state) ? Role.FURNACE_PART : Role.PLAIN;
    }

    private static BlockPos pressOffset(BlockState state, Direction facing) {
        if (state.is(TTBlocks.PLACEHOLDER_IRON_BARS)) {
            return BlockPos.ZERO.above();
        }
        BlockPos result = BlockPos.ZERO;
        if (!state.is(TTBlocks.PLACEHOLDER_CAULDRON)) {
            result = result.relative(facing.getClockWise());
        }
        if (!state.is(TTBlocks.PLACEHOLDER_TABLE)) {
            result = result.relative(facing.getOpposite());
        }
        return result;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = switch (roleOf(state)) {
            case FURNACE_PART -> AdvancedFurnaceShapes.find(level, pos);
            case PRESS -> pressShape(state, level, pos);
            default -> null;
        };
        return shape == null ? Shapes.block() : shape;
    }

    private static @Nullable VoxelShape pressShape(BlockState state, BlockGetter level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int cell = 0; cell < SHAPE_SEARCH_CELLS; cell++) {
            int x = cell / (SHAPE_SEARCH_HEIGHT * SHAPE_SEARCH_WIDTH) - SEARCH_RADIUS;
            int y = cell / SHAPE_SEARCH_WIDTH % SHAPE_SEARCH_HEIGHT + SHAPE_SEARCH_MIN_Y;
            int z = cell % SHAPE_SEARCH_WIDTH - SEARCH_RADIUS;
            BlockState candidate = level.getBlockState(cursor.setWithOffset(pos, x, y, z));
            if (!candidate.is(TTBlocks.GOLEM_BUILDER)) {
                continue;
            }
            Direction facing = candidate.getValue(BlockGolemBuilder.FACING);
            BlockPos offset = new BlockPos(-x, -y, -z);
            if (offset.equals(pressOffset(state, facing))) {
                return GolemPressShapes.at(facing, offset);
            }
        }
        return null;
    }

    private static @Nullable BlockPos findAround(BlockGetter level, BlockPos pos, Block block) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int y = -SEARCH_RADIUS; y <= SEARCH_RADIUS; y++) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                    cursor.setWithOffset(pos, x, y, z);
                    if (level.getBlockState(cursor).is(block)) {
                        return cursor.immutable();
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState neighborState, Direction direction) {
        return true;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return FULL_SHADE;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return state.getFluidState().isEmpty();
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return visible ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
        if (level.isClientSide()) {
            return;
        }
        switch (roleOf(state)) {
            case SHELL -> destroyShell(level, pos);
            case PRESS -> releasePress(level, pos);
            case FURNACE_PART -> AdvancedAlchemicalFurnaceStructure.disassembleAround(level, pos);
            default -> {
            }
        }
    }

    private static void destroyShell(LevelAccessor level, BlockPos pos) {
        BlockPos furnacePos = findAround(level, pos, TTBlocks.INFERNAL_FURNACE.get());
        if (furnacePos != null) {
            BlockInfernalFurnace.destroyFurnace(level, furnacePos, level.getBlockState(furnacePos), pos);
        }
    }

    private static void releasePress(LevelAccessor level, BlockPos pos) {
        BlockPos builderPos = findAround(level, pos, TTBlocks.GOLEM_BUILDER.get());
        if (builderPos != null) {
            BlockGolemBuilder.restoreStructure(level, builderPos, pos);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockPos builderPos = roleOf(state) == Role.PRESS ? findAround(level, pos, TTBlocks.GOLEM_BUILDER.get()) : null;
        if (builderPos == null) {
            return super.useWithoutItem(state, level, pos, player, hit);
        }
        return BlockGolemBuilder.openBuilderGui(level, builderPos, player);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        return Shapes.empty();
    }

    @Override
    protected int getLightDampening(BlockState state) {
        return NO_DAMPENING;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }
}
