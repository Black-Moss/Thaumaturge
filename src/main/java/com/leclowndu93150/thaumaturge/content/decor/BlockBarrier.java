package com.leclowndu93150.thaumaturge.content.decor;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockBarrier extends Block {
    public static final MapCodec<BlockBarrier> CODEC = simpleCodec(BlockBarrier::new);
    private static final int NEAR_STONE_DISTANCE = 1;
    private static final int FAR_STONE_DISTANCE = 2;

    public BlockBarrier(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockBarrier> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!(level instanceof Level world)) {
            return Shapes.block();
        }
        if (!(context instanceof EntityCollisionContext entityContext)) {
            return Shapes.empty();
        }
        Entity entity = entityContext.getEntity();
        if (!(entity instanceof LivingEntity) || entity instanceof Player || carriesPlayer(entity) || isSupportPowered(world, pos)) {
            return Shapes.empty();
        }
        return Shapes.block();
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        if (level.isClientSide()) {
            return;
        }
        BlockState below = level.getBlockState(pos.below());
        if (!isStone(below) && !below.is(this)) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState neighbor, Direction direction) {
        return true;
    }

    private static boolean carriesPlayer(Entity entity) {
        for (Entity passenger : entity.getPassengers()) {
            if (passenger instanceof Player || carriesPlayer(passenger)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSupportPowered(Level level, BlockPos pos) {
        BlockPos stonePos = isStone(level.getBlockState(pos.below(NEAR_STONE_DISTANCE))) ? pos.below(NEAR_STONE_DISTANCE) : pos.below(FAR_STONE_DISTANCE);
        return isStone(level.getBlockState(stonePos)) && level.hasNeighborSignal(stonePos);
    }

    private static boolean isStone(BlockState state) {
        return state.is(TTBlocks.PAVING_STONE_BARRIER.get());
    }
}
