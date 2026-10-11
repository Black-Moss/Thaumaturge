package com.leclowndu93150.thaumaturge.content.entity.ai;

import com.leclowndu93150.thaumaturge.content.entity.EntityFireBat;
import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class FireBatAttackGoal extends Goal {
    private static final double FLY_SPEED = 1.0;
    private static final double MIN_REACH = 2.5;
    private static final double REACH_PER_WIDTH = 1.1;
    private static final int COOLDOWN_MIN_TICKS = 20;
    private static final int COOLDOWN_SPREAD_TICKS = 20;
    private static final int EXPLODE_ONE_IN = 10;
    private static final float EXPLOSION_POWER = 1.5F;
    private static final double CHEST_FRACTION = 2.0 / 3.0;
    private static final float HURT_VOLUME = 0.1F;
    private static final float HURT_PITCH_BASE = 0.95F;
    private static final float HURT_PITCH_SPREAD = 0.1F;

    private static final EnumSet<Goal.Flag> FLAGS = EnumSet.of(Goal.Flag.MOVE);

    private int ticksUntilStrike;
    private final EntityFireBat flyer;

    public FireBatAttackGoal(EntityFireBat flyer) {
        setFlags(FLAGS);
        this.flyer = flyer;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        LivingEntity prey = flyer.getTarget();
        return prey != null && !flyer.isHanging() && prey.isAlive();
    }

    @Override
    public void tick() {
        LivingEntity prey = flyer.getTarget();
        if (prey == null) {
            return;
        }
        flyer.getMoveControl().setWantedPosition(prey.getX(), prey.getY() + prey.getEyeHeight() * CHEST_FRACTION, prey.getZ(), FLY_SPEED);
        if (ticksUntilStrike > 0) {
            ticksUntilStrike--;
            return;
        }
        if (!canStrike(prey) || !(flyer.level() instanceof ServerLevel world)) {
            return;
        }
        RandomSource random = flyer.getRandom();
        ticksUntilStrike = COOLDOWN_MIN_TICKS + random.nextInt(COOLDOWN_SPREAD_TICKS);
        if (random.nextInt(EXPLODE_ONE_IN) == 0) {
            detonate(world, prey);
        } else {
            bite(world, prey, random);
        }
    }

    private void bite(ServerLevel world, LivingEntity prey, RandomSource random) {
        flyer.doHurtTarget(world, prey);
        float pitch = HURT_PITCH_BASE + (random.nextFloat() - random.nextFloat()) * HURT_PITCH_SPREAD;
        world.playSound(null, flyer.getX(), flyer.getY(), flyer.getZ(), SoundEvents.BAT_HURT, flyer.getSoundSource(), HURT_VOLUME, pitch);
    }

    private boolean canStrike(LivingEntity prey) {
        return withinReach(prey) && flyer.hasLineOfSight(prey);
    }

    private boolean withinReach(LivingEntity prey) {
        double reach = Math.max(MIN_REACH, REACH_PER_WIDTH * prey.getBbWidth());
        AABB mine = flyer.getBoundingBox();
        AABB theirs = prey.getBoundingBox();
        boolean overlapsVertically = mine.maxY >= theirs.minY && mine.minY <= theirs.maxY;
        return flyer.distanceToSqr(prey) < reach * reach && overlapsVertically;
    }

    private void detonate(ServerLevel world, LivingEntity prey) {
        prey.invulnerableTime = 0;
        world.explode(flyer, flyer.getX(), flyer.getY(), flyer.getZ(), EXPLOSION_POWER, false, Level.ExplosionInteraction.NONE);
        flyer.discard();
    }
}
