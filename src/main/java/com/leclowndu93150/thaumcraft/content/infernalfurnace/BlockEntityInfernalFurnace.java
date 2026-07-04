package com.leclowndu93150.thaumcraft.content.infernalfurnace;

import com.leclowndu93150.thaumcraft.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class BlockEntityInfernalFurnace extends BlockEntity {

    private final ItemStacksResourceHandler inventory = new ItemStacksResourceHandler(32);

    public BlockEntityInfernalFurnace(BlockPos worldPosition, BlockState blockState) {
        super(TCBlockEntities.INFERNAL_FURNACE.get(),worldPosition, blockState);
    }

    public ItemStacksResourceHandler inventory() {
        return inventory;
    }


}
