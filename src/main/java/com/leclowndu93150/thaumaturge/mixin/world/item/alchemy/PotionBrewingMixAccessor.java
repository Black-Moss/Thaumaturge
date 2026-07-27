package com.leclowndu93150.thaumaturge.mixin.world.item.alchemy;

import net.minecraft.core.Holder;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.world.item.alchemy.PotionBrewing$Mix")
public interface PotionBrewingMixAccessor {
    @Accessor("from")
    Holder<?> thaumaturge$getFrom();

    @Accessor("ingredient")
    Ingredient thaumaturge$getIngredient();

    @Accessor("to")
    Holder<?> thaumaturge$getTo();
}
