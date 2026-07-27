package com.leclowndu93150.thaumaturge.content.taint.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ITaintBlock {
    void die(Level level, BlockPos pos, BlockState state);
}
