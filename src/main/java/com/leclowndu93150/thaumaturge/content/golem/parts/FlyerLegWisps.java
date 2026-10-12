package com.leclowndu93150.thaumaturge.content.golem.parts;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.parts.IGolemPartAbility;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class FlyerLegWisps implements IGolemPartAbility {
    private static final int GROUNDED_PERIOD = 5;
    private static final double TRAIL_LIFT = 0.1D;
    private static final double DRIFT_DIVISOR = 100.0D;
    private static final double SINK_SPEED = 0.1D;

    @Override
    public void tick(IGolemAPI golem) {
        Level level = golem.level();
        if (!level.isClientSide()) {
            return;
        }
        LivingEntity body = golem.asEntity();
        if (body.onGround() && body.tickCount % GROUNDED_PERIOD != 0) {
            return;
        }
        RandomSource random = level.getRandom();
        level.addParticle(TTParticles.GOLEM_TRAIL.get(), body.getX(), body.getY() + TRAIL_LIFT, body.getZ(), random.nextGaussian() / DRIFT_DIVISOR, -SINK_SPEED,
                random.nextGaussian() / DRIFT_DIVISOR);
    }
}
