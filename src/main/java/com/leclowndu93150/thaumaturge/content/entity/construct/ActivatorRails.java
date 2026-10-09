package com.leclowndu93150.thaumaturge.content.entity.construct;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

final class ActivatorRails {
    private ActivatorRails() {}

    static @Nullable BlockState find(Level level, BlockPos pos, BlockPos.MutableBlockPos scratch) {
        BlockState here = level.getBlockState(pos);
        if (here.is(TTBlocks.ACTIVATOR_RAIL.get())) {
            return here;
        }
        BlockState below = level.getBlockState(scratch.setWithOffset(pos, Direction.DOWN));
        return below.is(TTBlocks.ACTIVATOR_RAIL.get()) ? below : null;
    }

    static boolean powered(BlockState rail) {
        return rail.getValue(BlockStateProperties.POWERED);
    }
}
