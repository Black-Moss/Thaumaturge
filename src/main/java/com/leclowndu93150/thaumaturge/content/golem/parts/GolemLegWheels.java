package com.leclowndu93150.thaumaturge.content.golem.parts;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.parts.IGolemPartAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class GolemLegWheels implements IGolemPartAbility {
    private static final double MIN_DUST_DISTANCE = 0.25;
    private static final double GROUND_PROBE_DEPTH = 0.2;
    private static final double DUST_OFFSET_Y = 0.1;
    private static final double DUST_RISE = 1.5;
    private static final double DUST_KICK = 4.0;
    private static final double CENTER = 0.5;

    @Override
    public void tick(IGolemAPI golem) {
        Level level = golem.level();
        if (!level.isClientSide()) {
            return;
        }
        LivingEntity entity = golem.asEntity();
        if (!entity.onGround() || entity.isInWater()) {
            return;
        }
        double dx = entity.getX() - entity.xo;
        double dy = entity.getY() - entity.yo;
        double dz = entity.getZ() - entity.zo;
        if (Math.sqrt(dx * dx + dy * dy + dz * dz) <= MIN_DUST_DISTANCE) {
            return;
        }
        BlockState ground = level.getBlockState(new BlockPos(entity.getBlockX(), Mth.floor(entity.getY() - GROUND_PROBE_DEPTH), entity.getBlockZ()));
        if (ground.getRenderShape() == RenderShape.INVISIBLE) {
            return;
        }
        RandomSource random = level.getRandom();
        Vec3 velocity = entity.getDeltaMovement();
        double x = entity.getX() + (random.nextDouble() - CENTER) * entity.getBbWidth();
        double z = entity.getZ() + (random.nextDouble() - CENTER) * entity.getBbWidth();
        level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, ground), x, entity.getBoundingBox().minY + DUST_OFFSET_Y, z, -velocity.x * DUST_KICK, DUST_RISE, -velocity.z * DUST_KICK);
    }
}
