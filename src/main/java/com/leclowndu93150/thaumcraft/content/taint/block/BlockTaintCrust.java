package com.leclowndu93150.thaumcraft.content.taint.block;

import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockTaintCrust extends AbstractTaintBlock {
    public static final MapCodec<BlockTaintCrust> CODEC = simpleCodec(BlockTaintCrust::new);

    public BlockTaintCrust(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BlockTaintCrust> codec() {
        return CODEC;
    }

    @Override
    public void die(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, TCBlocks.FLUX_GOO.get().defaultBlockState(), Block.UPDATE_ALL);
    }
}
