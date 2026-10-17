package com.leclowndu93150.thaumaturge.content.golem.parts;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.parts.IGolemPartAbility;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class GolemLegLevitator implements IGolemPartAbility {
    private static final int GROUNDED_INTERVAL_TICKS = 5;
    private static final double TRAIL_OFFSET_Y = 0.1;
    private static final double TRAIL_SPREAD_DIVISOR = 100.0;
    private static final double TRAIL_FALL_SPEED = -0.1;

    @Override
    public void tick(IGolemAPI golem) {
        Level level = golem.level();
        if (!level.isClientSide()) {
            return;
        }
        LivingEntity entity = golem.asEntity();
        if (entity.onGround() && entity.tickCount % GROUNDED_INTERVAL_TICKS != 0) {
            return;
        }
        RandomSource random = level.getRandom();
        level.addParticle(TTParticles.GOLEM_TRAIL.get(), entity.getX(), entity.getY() + TRAIL_OFFSET_Y, entity.getZ(), random.nextGaussian() / TRAIL_SPREAD_DIVISOR, TRAIL_FALL_SPEED,
                random.nextGaussian() / TRAIL_SPREAD_DIVISOR);
    }
}
