package com.leclowndu93150.thaumcraft.content.eldritch.block;

import com.leclowndu93150.thaumcraft.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityEldritchNothing extends BlockEntity {
    public BlockEntityEldritchNothing(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ELDRITCH_NOTHING.get(), pos, state);
    }
}
