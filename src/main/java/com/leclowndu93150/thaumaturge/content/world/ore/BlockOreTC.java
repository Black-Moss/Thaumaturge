package com.leclowndu93150.thaumaturge.content.world.ore;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class BlockOreTC extends Block {
    public static final MapCodec<BlockOreTC> CODEC = simpleCodec(BlockOreTC::new);

    public BlockOreTC(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockOreTC> codec() {
        return CODEC;
    }
}
