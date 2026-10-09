package com.leclowndu93150.thaumaturge.content.entity;

import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.player.Player;

final class PechAvoidGoal extends AvoidEntityGoal<Player> {
    private static final float RADIUS = 8.0F;
    private static final double WALK_SPEED = 0.5;
    private static final double SPRINT_SPEED = 0.6;

    private final EntityPech pech;

    PechAvoidGoal(EntityPech pech) {
        super(pech, Player.class, RADIUS, WALK_SPEED, SPRINT_SPEED);
        this.pech = pech;
    }

    @Override
    public boolean canUse() {
        return !this.pech.isDomesticated() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.pech.isDomesticated() && super.canContinueToUse();
    }
}
