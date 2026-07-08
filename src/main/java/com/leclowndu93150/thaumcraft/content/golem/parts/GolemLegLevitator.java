package com.leclowndu93150.thaumcraft.content.golem.parts;

import com.leclowndu93150.thaumcraft.api.golems.IGolemAPI;
import com.leclowndu93150.thaumcraft.api.golems.parts.GolemLeg;
import com.leclowndu93150.thaumcraft.content.fx.data.FXGenericData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class GolemLegLevitator implements GolemLeg.ILegFunction {
    private static final int PARTICLE_GRID = 16;
    private static final int PARTICLE_START = 56;
    private static final int GROUND_INTERVAL = 5;

    @Override
    public void onUpdateTick(IGolemAPI golem) {
        Level level = golem.getGolemWorld();
        LivingEntity entity = golem.getGolemEntity();
        if (!level.isClientSide() || (entity.onGround() && entity.tickCount % GROUND_INTERVAL != 0)) {
            return;
        }
        RandomSource rand = level.getRandom();
        FXGenericData data = FXGenericData.builder()
                .motion(rand.nextGaussian() / 100.0, -0.1, rand.nextGaussian() / 100.0)
                .maxAge(20 + rand.nextInt(5))
                .alpha(0.3F, 0.0F)
                .grid(PARTICLE_GRID)
                .particles(PARTICLE_START, 1, 1)
                .scale(1.5F, 3.0F, 8.0F)
                .layer(0)
                .slowDown(1.0)
                .wind(0.001)
                .rotation(rand.nextFloat(), rand.nextBoolean() ? -1.0F : 1.0F)
                .build();
        level.addParticle(data, entity.getX(), entity.getY() + 0.1, entity.getZ(), 0.0, 0.0, 0.0);
    }
}
