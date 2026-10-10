package com.leclowndu93150.thaumaturge.content.golem.ai;

import net.minecraft.util.Mth;

public record ShootingCadence(double range, int sightTicks, int baseInterval, int intervalAtFullRange) {
    public int interval(float rangeFraction) {
        return Mth.floor(baseInterval + intervalAtFullRange * rangeFraction);
    }
}
