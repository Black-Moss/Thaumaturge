package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockEldritchCap extends BlockEldritchStructure implements EntityBlock {
    public static final MapCodec<BlockEldritchCap> CODEC = simpleCodec(BlockEldritchCap::new);

    public BlockEldritchCap(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockEldritchCap> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityEldritchCap(pos, state);
    }
}
