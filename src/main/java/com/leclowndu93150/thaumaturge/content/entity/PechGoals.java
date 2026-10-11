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
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;

final class PechGoals {
    private static final int SWIM_PRIORITY = 0;
    private static final int TRADE_HOLD_PRIORITY = 1;
    static final int COMBAT_PRIORITY = 2;
    private static final int FETCH_PRIORITY = 3;
    private static final int AVOID_PRIORITY = 4;
    private static final int DOOR_PRIORITY = 5;
    private static final int HOME_PRIORITY = 6;
    private static final int WANDER_PRIORITY = 7;
    private static final int GAZE_PRIORITY = 8;
    private static final int IDLE_LOOK_PRIORITY = 9;

    private static final int RETALIATE_PRIORITY = 0;
    private static final int GRUDGE_PRIORITY = 1;

    private static final double HOME_SPEED = 0.5;
    private static final double WANDER_SPEED = 0.6;
    private static final float CLOSE_PLAYER_RANGE = 3.0F;
    private static final float ALWAYS = 1.0F;
    private static final float GAZE_RANGE = 8.0F;
    private static final boolean CLOSE_DOORS_BEHIND = true;

    private PechGoals() {}

    static void register(EntityPech pech, GoalSelector goals, GoalSelector targets) {
        goals.addGoal(SWIM_PRIORITY, new FloatGoal(pech));
        goals.addGoal(TRADE_HOLD_PRIORITY, new HoldStillGoal<>(pech));
        goals.addGoal(FETCH_PRIORITY, new FetchItemGoal<>(pech));
        goals.addGoal(AVOID_PRIORITY, new PechAvoidGoal(pech));
        goals.addGoal(DOOR_PRIORITY, new OpenDoorGoal(pech, CLOSE_DOORS_BEHIND));
        goals.addGoal(HOME_PRIORITY, new MoveTowardsRestrictionGoal(pech, HOME_SPEED));
        goals.addGoal(WANDER_PRIORITY, new RandomStrollGoal(pech, WANDER_SPEED));
        goals.addGoal(WANDER_PRIORITY, new LookAtPlayerGoal(pech, Player.class, CLOSE_PLAYER_RANGE, ALWAYS));
        goals.addGoal(GAZE_PRIORITY, new LookAtPlayerGoal(pech, LivingEntity.class, GAZE_RANGE));
        goals.addGoal(IDLE_LOOK_PRIORITY, new RandomLookAroundGoal(pech));

        targets.addGoal(RETALIATE_PRIORITY, new HurtByTargetGoal(pech));
        targets.addGoal(GRUDGE_PRIORITY, new PechAngerTargetGoal(pech));
    }
}
