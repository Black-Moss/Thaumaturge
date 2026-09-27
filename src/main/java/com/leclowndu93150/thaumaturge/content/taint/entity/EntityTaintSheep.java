package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class EntityTaintSheep extends Sheep implements ITaintedMob, TaintConversionTarget {
    private static final double MAX_HEALTH = 20.0;
    private static final double ATTACK_DAMAGE = 3.0;
    private static final double MOVEMENT_SPEED = 0.25;
    private static final double ARMOR = 2.0;
    private static final double FOLLOW_RANGE = 24.0;

    public EntityTaintSheep(EntityType<? extends Sheep> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Sheep.createAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE).add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED).add(Attributes.ARMOR, ARMOR)
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.removeAllGoals(goal -> true);
        targetSelector.removeAllGoals(goal -> true);
        TaintedLivestockGoals.addHostileGoals(this, goalSelector, targetSelector, true, false);
        goalSelector.addGoal(TaintedLivestockGoals.EXTRA_GOAL_PRIORITY, new TaintGrazeGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public void copyConvertedState(LivingEntity source) {
        if (source instanceof Sheep sheep) {
            setColor(sheep.getColor());
            setSheared(sheep.isSheared());
        }
    }
}
