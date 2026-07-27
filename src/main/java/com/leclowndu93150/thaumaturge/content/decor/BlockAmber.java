package com.leclowndu93150.thaumaturge.content.decor;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.TransparentBlock;

public final class BlockAmber extends TransparentBlock {
    public static final MapCodec<BlockAmber> CODEC = simpleCodec(BlockAmber::new);

    public BlockAmber(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockAmber> codec() {
        return CODEC;
    }
}
