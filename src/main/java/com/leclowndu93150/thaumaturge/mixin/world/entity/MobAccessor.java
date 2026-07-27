package com.leclowndu93150.thaumaturge.mixin.world.entity;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mob.class)
public interface MobAccessor {
    @Accessor("goalSelector")
    GoalSelector thaumaturge$getGoalSelector();

    @Accessor("targetSelector")
    GoalSelector thaumaturge$getTargetSelector();
}
