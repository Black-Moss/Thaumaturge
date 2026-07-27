package com.leclowndu93150.thaumaturge.content.entity.ai;

import com.leclowndu93150.thaumaturge.content.entity.EntityPech;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;

public final class PechTradeGoal extends Goal {
    private final EntityPech pech;

    public PechTradeGoal(EntityPech pech) {
        this.pech = pech;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!this.pech.isAlive()
                || this.pech.isInWater()
                || !this.pech.isTamed()
                || !this.pech.onGround()) {
            return false;
        }
        return this.pech.trading;
    }

    @Override
    public void start() {
        this.pech.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.pech.trading = false;
    }
}
