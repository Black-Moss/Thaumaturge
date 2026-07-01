package com.leclowndu93150.thaumcraft.content.taint.block;

import com.leclowndu93150.thaumcraft.api.aura.AuraHelper;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockTaintGeyser extends AbstractTaintBlock {
    public static final MapCodec<BlockTaintGeyser> CODEC = simpleCodec(BlockTaintGeyser::new);

    private static final float LOW_FLUX_THRESHOLD = 2.0F;
    private static final float POLLUTE_AMOUNT = 0.25F;

    public BlockTaintGeyser(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BlockTaintGeyser> codec() {
        return CODEC;
    }

    @Override
    public void die(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, TCBlocks.FLUX_GOO.get().defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void subRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (AuraHelper.getFlux(level, pos) < LOW_FLUX_THRESHOLD) {
            AuraHelper.polluteAura(level, pos, POLLUTE_AMOUNT, true);
        }
    }
}
