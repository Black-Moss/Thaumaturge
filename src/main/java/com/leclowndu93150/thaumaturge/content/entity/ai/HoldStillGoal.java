package com.leclowndu93150.thaumaturge.content.entity.ai;

import java.util.EnumSet;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public final class HoldStillGoal<T extends Mob & HoldsStill> extends Goal {
    private final T creature;

    public HoldStillGoal(T creature) {
        this.creature = creature;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return creature.holdingStill() && creature.isAlive() && creature.onGround() && !creature.isInWater();
    }

    @Override
    public void start() {
        creature.getNavigation().stop();
    }

    @Override
    public void stop() {
        creature.releaseHold();
    }
}
