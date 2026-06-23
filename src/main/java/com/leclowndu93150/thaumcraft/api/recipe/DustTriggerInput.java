package com.leclowndu93150.thaumcraft.api.recipe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public record DustTriggerInput(ItemStack dust, Level level, BlockPos pos, BlockState clicked) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        if (index == 0) {
            return this.dust;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }
}
