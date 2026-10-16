package com.leclowndu93150.thaumaturge.content.entity.construct;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;

final class StationaryNavigation extends GroundPathNavigation {
    StationaryNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected boolean canUpdatePath() {
        return false;
    }
}
