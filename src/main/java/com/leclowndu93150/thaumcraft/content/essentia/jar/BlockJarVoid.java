package com.leclowndu93150.thaumcraft.content.essentia.jar;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockJarVoid extends BlockJar {
    public static final MapCodec<BlockJarVoid> CODEC = simpleCodec(BlockJarVoid::new);

    public BlockJarVoid(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BlockJar> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityJarVoid(pos, state);
    }
}
