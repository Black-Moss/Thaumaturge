package com.leclowndu93150.thaumaturge.content.taint.entity;

import net.minecraft.world.entity.LivingEntity;

public interface TaintConversionTarget {
    void copyConvertedState(LivingEntity source);
}
