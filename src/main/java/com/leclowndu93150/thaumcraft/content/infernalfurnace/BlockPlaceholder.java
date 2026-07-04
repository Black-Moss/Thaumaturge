package com.leclowndu93150.thaumcraft.content.infernalfurnace;

import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class BlockPlaceholder extends Block {
    public BlockPlaceholder(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if ((state.is(TCBlocks.NETHER_BRICKS_PLACEHOLDER) || state.is(TCBlocks.OBSIDIAN_PLACEHOLDER)) && !level.isClientSide()){
            destroyFor:
            for (int x = -1; x <= 1; x++){
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos offsetPos = pos.offset(x, y, z);
                        BlockState offsetState = level.getBlockState(offsetPos);
                        if (offsetState.is(TCBlocks.INFERNAL_FURNACE)) {
                            BlockInfernalFurnace.destroyFurnace(level, offsetPos, offsetState, pos);
                            break destroyFor;
                        }
                    }
                }
            }
        }
        super.destroy(level, pos, state);
    }
}
