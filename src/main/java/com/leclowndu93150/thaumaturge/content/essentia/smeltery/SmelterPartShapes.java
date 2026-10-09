package com.leclowndu93150.thaumaturge.content.essentia.smeltery;

import com.leclowndu93150.thaumaturge.content.device.DeviceShapes;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

final class SmelterPartShapes {
    private SmelterPartShapes() {}

    static Map<Direction, VoxelShape> fromNorth(VoxelShape northShape) {
        return DeviceShapes.facingShapesFromNorth(northShape);
    }

    static boolean attachedToSmelter(LevelReader level, BlockPos pos, Direction partFacing) {
        BlockState target = level.getBlockState(pos.relative(partFacing));
        return target.getBlock() instanceof BlockSmelter && target.getValue(BlockSmelter.FACING) != partFacing.getOpposite();
    }
}
