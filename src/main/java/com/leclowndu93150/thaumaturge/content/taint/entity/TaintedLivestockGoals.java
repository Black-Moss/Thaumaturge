package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;

public final class TaintedLivestockGoals {
    public static final int EXTRA_GOAL_PRIORITY = 4;

    private static final int FLOAT_PRIORITY = 0;
    private static final int MELEE_PRIORITY = 2;
    private static final int STROLL_PRIORITY = 5;
    private static final int LOOK_PRIORITY = 6;
    private static final int LOOK_AROUND_PRIORITY = 7;
    private static final int HURT_BY_PRIORITY = 0;
    private static final int PLAYER_PRIORITY = 2;
    private static final int VILLAGER_PRIORITY = 3;
    private static final int ANIMAL_PRIORITY = 8;
    private static final double SPEED = 1.0;
    private static final float LOOK_RANGE = 6.0F;
    private static final int TARGET_INTERVAL = 10;

    private TaintedLivestockGoals() {}

    public static void addHostileGoals(PathfinderMob mob, GoalSelector goals, GoalSelector targets, boolean huntVillagers, boolean huntAnimals) {
        goals.addGoal(FLOAT_PRIORITY, new FloatGoal(mob));
        goals.addGoal(MELEE_PRIORITY, new MeleeAttackGoal(mob, SPEED, false));
        goals.addGoal(STROLL_PRIORITY, new WaterAvoidingRandomStrollGoal(mob, SPEED));
        goals.addGoal(LOOK_PRIORITY, new LookAtPlayerGoal(mob, Player.class, LOOK_RANGE));
        goals.addGoal(LOOK_AROUND_PRIORITY, new RandomLookAroundGoal(mob));
        targets.addGoal(HURT_BY_PRIORITY, new HurtByTargetGoal(mob));
        targets.addGoal(PLAYER_PRIORITY, new NearestAttackableTargetGoal<>(mob, Player.class, true));
        if (huntVillagers) {
            targets.addGoal(VILLAGER_PRIORITY, new NearestAttackableTargetGoal<>(mob, Villager.class, true));
        }
        if (huntAnimals) {
            targets.addGoal(ANIMAL_PRIORITY, new NearestAttackableTargetGoal<>(mob, Animal.class, TARGET_INTERVAL, true, false, (target, level) -> !(target instanceof ITaintedMob)));
        }
    }
}
