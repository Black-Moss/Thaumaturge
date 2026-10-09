package com.leclowndu93150.thaumaturge.content.entity.ai;

import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public final class FetchItemGoal<T extends PathfinderMob & ItemCollector> extends Goal {
    private static final double SEARCH_RANGE = 16.0;
    private static final double SEARCH_RANGE_SQR = SEARCH_RANGE * SEARCH_RANGE;
    private static final int RESCAN_COOLDOWN = 10;
    private static final double MOVE_SPEED = 1.5;
    private static final float LOOK_SPEED = 30.0F;
    private static final int REPATH_MIN_TICKS = 4;
    private static final int REPATH_SPREAD_TICKS = 4;
    private static final double PICKUP_RANGE = 1.2;
    private static final double PICKUP_RANGE_SQR = PICKUP_RANGE * PICKUP_RANGE;
    private static final float CHIME_VOLUME = 0.2F;
    private static final float CHIME_PITCH = 2.0F;
    private static final float CHIME_PITCH_SPREAD = 0.7F;

    private static final EnumSet<Goal.Flag> FLAGS = EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE);

    private final T collector;
    private int repathDelay;
    private int scanDelay;
    private @Nullable ItemEntity quarry;

    public FetchItemGoal(T collector) {
        setFlags(FLAGS);
        this.collector = collector;
    }

    @Override
    public boolean canUse() {
        boolean waiting = scanDelay > 0;
        scanDelay = waiting ? scanDelay - 1 : RESCAN_COOLDOWN;
        return !waiting && pickQuarry();
    }

    @Override
    public boolean canContinueToUse() {
        return pickQuarry();
    }

    @Override
    public void start() {
        repathDelay = 0;
        pathToQuarry();
    }

    @Override
    public void stop() {
        quarry = null;
        collector.getNavigation().stop();
    }

    @Override
    public void tick() {
        ItemEntity drop = quarry;
        if (drop == null) {
            return;
        }
        collector.getLookControl().setLookAt(drop, LOOK_SPEED, LOOK_SPEED);
        if (collector.distanceToSqr(drop) <= PICKUP_RANGE_SQR) {
            pickUp(drop);
            return;
        }
        if (--repathDelay <= 0 && collector.hasLineOfSight(drop)) {
            pathToQuarry();
        }
    }

    private boolean pickQuarry() {
        quarry = closestWanted().orElse(null);
        return quarry != null;
    }

    private void pathToQuarry() {
        if (quarry == null) {
            return;
        }
        repathDelay = REPATH_MIN_TICKS + collector.getRandom().nextInt(REPATH_SPREAD_TICKS);
        collector.getNavigation().moveTo(quarry, MOVE_SPEED);
    }

    private Optional<ItemEntity> closestWanted() {
        AABB area = collector.getBoundingBox().inflate(SEARCH_RANGE);
        return collector.level().getEntitiesOfClass(ItemEntity.class, area, collector::wantsToCollect).stream().filter(drop -> collector.distanceToSqr(drop) <= SEARCH_RANGE_SQR)
                .reduce((best, next) -> collector.distanceToSqr(next) <= collector.distanceToSqr(best) ? next : best);
    }

    private void pickUp(ItemEntity drop) {
        ItemStack offer = drop.getItem().copy();
        int offered = offer.getCount();
        ItemStack leftover = collector.collect(offer);
        if (leftover.isEmpty()) {
            drop.discard();
        } else {
            drop.setItem(leftover);
        }
        if (leftover.getCount() < offered) {
            chime(drop);
        }
        quarry = null;
    }

    private void chime(ItemEntity drop) {
        RandomSource random = collector.getRandom();
        float variation = (random.nextFloat() - random.nextFloat()) * CHIME_PITCH_SPREAD + 1.0F;
        float pitch = variation * CHIME_PITCH;
        collector.level().playSound(null, drop.getX(), drop.getY(), drop.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, CHIME_VOLUME, pitch);
    }
}
