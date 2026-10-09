package com.leclowndu93150.thaumaturge.content.entity;

import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;

final class PechAngerTargetGoal extends NearestAttackableTargetGoal<Player> {
    private static final double RANGE = 10.0;
    private static final int CHECK_INTERVAL = 10;

    private final EntityPech pech;

    PechAngerTargetGoal(EntityPech pech) {
        super(pech, Player.class, CHECK_INTERVAL, true, false, null);
        this.pech = pech;
    }

    @Override
    public boolean canUse() {
        return this.pech.rageTicks() > 0 && super.canUse();
    }

    @Override
    protected double getFollowDistance() {
        return RANGE;
    }
}
