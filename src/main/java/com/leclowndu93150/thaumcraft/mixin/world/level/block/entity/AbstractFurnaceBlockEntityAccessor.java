package com.leclowndu93150.thaumcraft.mixin.world.level.block.entity;

import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceBlockEntityAccessor {

    @Accessor("cookingTimer")
    int thaumcraft$getCookTime();

    @Accessor("cookingTimer")
    void thaumcraft$setCookTime(int cookTime);

    @Accessor("cookingTotalTime")
    int thaumcraft$getCookTimeTotal();

}
