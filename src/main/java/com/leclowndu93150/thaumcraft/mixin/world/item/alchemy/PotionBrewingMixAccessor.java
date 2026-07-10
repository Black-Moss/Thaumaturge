package com.leclowndu93150.thaumcraft.mixin.world.item.alchemy;

import net.minecraft.core.Holder;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.world.item.alchemy.PotionBrewing$Mix")
public interface PotionBrewingMixAccessor {
    @Accessor("from")
    Holder<?> thaumcraft$getFrom();

    @Accessor("ingredient")
    Ingredient thaumcraft$getIngredient();

    @Accessor("to")
    Holder<?> thaumcraft$getTo();
}
