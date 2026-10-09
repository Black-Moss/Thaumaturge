package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.content.essentia.tube.behaviour.DirectionalTubeBehaviour;
import com.leclowndu93150.thaumaturge.content.essentia.tube.facing.SideRanking;
import com.leclowndu93150.thaumaturge.content.essentia.tube.facing.SideRotation;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class BlockEntityTubeOneway extends BlockEntityTube {
    private static final int ACCEPTABLE_INLET = 0;

    public BlockEntityTubeOneway(BlockPos pos, BlockState state) {
        super(TTBlockEntities.TUBE_ONEWAY.get(), pos, state, DirectionalTubeBehaviour.INSTANCE);
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face != null && face != flowSide().getOpposite() && isSideOpen(face);
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face != null && face == flowSide().getOpposite() && isSideOpen(face);
    }

    @Override
    public boolean rotateFacing() {
        Direction target = SideRotation.next(flowSide(), this::rankInletCandidate);
        if (target == null) {
            return false;
        }
        assignFlowSide(target);
        setChanged();
        return true;
    }

    private int rankInletCandidate(Direction candidate) {
        Direction inlet = candidate.getOpposite();
        return isSideOpen(inlet) && hasTransportNeighbour(inlet) ? ACCEPTABLE_INLET : SideRanking.UNACCEPTABLE;
    }

    @Override
    protected void assignFlowSide(Direction direction) {
        BlockState state = getBlockState();
        if (level == null || !state.hasProperty(BlockStateProperties.FACING)) {
            return;
        }
        flowSide = direction;
        level.setBlock(worldPosition, state.setValue(BlockStateProperties.FACING, direction), Block.UPDATE_ALL);
    }

    @Override
    public Direction flowSide() {
        BlockState state = getBlockState();
        return state.hasProperty(BlockStateProperties.FACING) ? state.getValue(BlockStateProperties.FACING) : flowSide;
    }
}
