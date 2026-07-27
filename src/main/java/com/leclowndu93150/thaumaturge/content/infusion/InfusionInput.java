package com.leclowndu93150.thaumaturge.content.infusion;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record InfusionInput(ItemStack catalyst, List<ItemStack> components) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? catalyst : components.get(index - 1);
    }

    @Override
    public int size() {
        return components.size() + 1;
    }
}
