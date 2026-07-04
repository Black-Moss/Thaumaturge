package com.leclowndu93150.thaumcraft.mixin.data.recipes;

import net.minecraft.data.BlockFamily;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RecipeProvider.class)
public interface RecipeProviderAccessor {

    @Invoker("generateCraftingRecipe")
    void thaumcraft$generateCraftingRecipe(BlockFamily family, BlockFamily.Variant variant, Block result, ItemLike base);

    @Invoker("generateStonecutterRecipe")
    void thaumcraft$generateStonecutterRecipe(BlockFamily family, BlockFamily.Variant variant, Block base);
}
