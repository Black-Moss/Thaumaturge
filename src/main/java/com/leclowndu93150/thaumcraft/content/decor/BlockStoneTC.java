package com.leclowndu93150.thaumcraft.content.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockStoneTC extends Block {
    private final boolean unbreakable;

    public BlockStoneTC(BlockBehaviour.Properties properties, boolean unbreakable) {
        super(properties);
        this.unbreakable = unbreakable;
    }

    public BlockStoneTC(BlockBehaviour.Properties properties) {
        this(properties, false);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return !this.unbreakable;
    }
}
