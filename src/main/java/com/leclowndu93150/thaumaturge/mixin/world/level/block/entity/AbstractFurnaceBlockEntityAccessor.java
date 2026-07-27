package com.leclowndu93150.thaumaturge.mixin.world.level.block.entity;

import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceBlockEntityAccessor {

    @Accessor("cookingTimer")
    int thaumaturge$getCookTime();

    @Accessor("cookingTimer")
    void thaumaturge$setCookTime(int cookTime);

    @Accessor("cookingTotalTime")
    int thaumaturge$getCookTimeTotal();

}
