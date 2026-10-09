package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.content.entity.ai.FetchItemGoal;
import com.leclowndu93150.thaumaturge.content.entity.ai.HoldStillGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;

final class PechGoals {
    static final int COMBAT_PRIORITY = 2;

    private static final int FLOAT_PRIORITY = 0;
    private static final int HOLD_PRIORITY = 1;
    private static final int FETCH_PRIORITY = 3;
    private static final int AVOID_PRIORITY = 4;
    private static final int DOOR_PRIORITY = 5;
    private static final int HOME_PRIORITY = 6;
    private static final int STROLL_PRIORITY = 9;
    private static final int LOOK_PLAYER_PRIORITY = 9;
    private static final int LOOK_LIVING_PRIORITY = 10;
    private static final int LOOK_AROUND_PRIORITY = 11;
    private static final int RETALIATE_PRIORITY = 1;
    private static final int ANGER_TARGET_PRIORITY = 2;
    private static final double HOME_SPEED = 0.5;
    private static final double STROLL_SPEED = 0.6;
    private static final float LOOK_PLAYER_DISTANCE = 3.0F;
    private static final float LOOK_PLAYER_PROBABILITY = 1.0F;
    private static final float LOOK_LIVING_DISTANCE = 8.0F;

    private PechGoals() {}

    static void register(EntityPech pech, GoalSelector goals, GoalSelector targets) {
        goals.addGoal(FLOAT_PRIORITY, new FloatGoal(pech));
        goals.addGoal(HOLD_PRIORITY, new HoldStillGoal<>(pech));
        goals.addGoal(FETCH_PRIORITY, new FetchItemGoal<>(pech));
        goals.addGoal(AVOID_PRIORITY, new PechAvoidGoal(pech));
        goals.addGoal(DOOR_PRIORITY, new OpenDoorGoal(pech, true));
        goals.addGoal(HOME_PRIORITY, new MoveTowardsRestrictionGoal(pech, HOME_SPEED));
        goals.addGoal(STROLL_PRIORITY, new WaterAvoidingRandomStrollGoal(pech, STROLL_SPEED));
        goals.addGoal(LOOK_PLAYER_PRIORITY, new LookAtPlayerGoal(pech, Player.class, LOOK_PLAYER_DISTANCE, LOOK_PLAYER_PROBABILITY));
        goals.addGoal(LOOK_LIVING_PRIORITY, new LookAtPlayerGoal(pech, LivingEntity.class, LOOK_LIVING_DISTANCE));
        goals.addGoal(LOOK_AROUND_PRIORITY, new RandomLookAroundGoal(pech));
        targets.addGoal(RETALIATE_PRIORITY, new HurtByTargetGoal(pech));
        targets.addGoal(ANGER_TARGET_PRIORITY, new PechAngerTargetGoal(pech));
    }
}
