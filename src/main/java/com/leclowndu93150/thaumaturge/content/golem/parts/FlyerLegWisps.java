package com.leclowndu93150.thaumaturge.content.golem.parts;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.parts.IGolemPartAbility;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class FlyerLegWisps implements IGolemPartAbility {
    private static final int GROUNDED_ONE_IN = 6;
    private static final double BASE_LIFT = 0.15D;
    private static final double FOOTPRINT_SHARE = 0.6D;
    private static final double CENTER = 0.5D;
    private static final double SINK_SPEED = 0.025D;
    private static final double SIDEWAYS_DRIFT = 0.008D;

    @Override
    public void tick(IGolemAPI golem) {
        Level level = golem.level();
        if (!level.isClientSide()) {
            return;
        }
        LivingEntity body = golem.asEntity();
        RandomSource random = level.getRandom();
        if (body.onGround() && random.nextInt(GROUNDED_ONE_IN) != 0) {
            return;
        }
        double footprint = body.getBbWidth() * FOOTPRINT_SHARE;
        double x = body.getX() + (random.nextDouble() - CENTER) * footprint;
        double z = body.getZ() + (random.nextDouble() - CENTER) * footprint;
        double y = body.getBoundingBox().minY + BASE_LIFT;
        level.addParticle(TTParticles.GOLEM_TRAIL.get(), x, y, z, random.triangle(0.0D, SIDEWAYS_DRIFT), -SINK_SPEED, random.triangle(0.0D, SIDEWAYS_DRIFT));
    }
}
