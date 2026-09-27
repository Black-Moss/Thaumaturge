package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class EntityTaintChicken extends Chicken implements ITaintedMob {
    private static final double MAX_HEALTH = 8.0;
    private static final double ATTACK_DAMAGE = 3.0;
    private static final double MOVEMENT_SPEED = 0.4;
    private static final double ARMOR = 2.0;
    private static final double FOLLOW_RANGE = 24.0;
    private static final float LEAP_HEIGHT = 0.3F;

    public EntityTaintChicken(EntityType<? extends Chicken> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Chicken.createAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE).add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.ARMOR, ARMOR).add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE);
    }

    @Override
    protected void registerGoals() {
        TaintedLivestockGoals.addHostileGoals(this, goalSelector, targetSelector, true, true);
        goalSelector.addGoal(TaintedLivestockGoals.EXTRA_GOAL_PRIORITY, new LeapAtTargetGoal(this, LEAP_HEIGHT));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }
}
