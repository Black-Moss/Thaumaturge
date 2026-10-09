package com.leclowndu93150.thaumaturge.content.equipment.whirlwind;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public interface WhirlwindStrategy {
    void apply(Level level, LivingEntity user);
}
