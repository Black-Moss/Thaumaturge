package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class RiftCollapse {
    private static final int SHRINK_PER_TICK = 1;
    private static final int FINISHED_SIZE = 1;
    private static final float RELEASE_PER_TICK = 1.0F;
    private static final int BLAST_ONE_IN = 10;
    private static final double BLAST_SPREAD = 2.0;
    private static final float BLAST_MAX_POWER = 0.5F;
    private static final int PEARL_ROLL = 100;
    private static final int PEARL_MIN_WEAR = 4;
    private static final int PEARL_WEAR_SPREAD = 4;
    private static final double EFFECT_REACH = 32.0;
    private static final double EFFECT_REACH_SQR = EFFECT_REACH * EFFECT_REACH;
    private static final int TICKS_PER_SECOND = 20;
    private static final int EFFECT_AMPLIFIER = 0;
    private static final float SMOKE_RED = 0.15F;
    private static final float SMOKE_GREEN = 0.1F;
    private static final float SMOKE_BLUE = 0.2F;

    boolean tick(ServerLevel level, EntityFluxRift rift) {
        int size = rift.currentSize() - SHRINK_PER_TICK;
        rift.resize(size);
        RandomSource random = rift.getRandom();
        BlockPos pos = rift.blockPosition();
        if (random.nextBoolean()) {
            AuraHelper.addVis(level, pos, RELEASE_PER_TICK);
        } else {
            AuraHelper.addFlux(level, pos, RELEASE_PER_TICK);
        }
        if (random.nextInt(BLAST_ONE_IN) == 0) {
            blast(level, rift, random);
        }
        if (size > FINISHED_SIZE) {
            return false;
        }
        finish(level, rift);
        return true;
    }

    private static void blast(ServerLevel level, EntityFluxRift rift, RandomSource random) {
        double x = rift.getX() + random.nextGaussian() * BLAST_SPREAD;
        double y = rift.getY() + random.nextGaussian() * BLAST_SPREAD;
        double z = rift.getZ() + random.nextGaussian() * BLAST_SPREAD;
        level.explode(rift, x, y, z, random.nextFloat() * BLAST_MAX_POWER, false, Level.ExplosionInteraction.NONE);
    }

    private static void finish(ServerLevel level, EntityFluxRift rift) {
        int strength = (int) Math.sqrt(Math.max(0, rift.collapseSize()));
        dropLoot(level, rift, strength);
        Effects.bamf(level, rift.position()).color(SMOKE_RED, SMOKE_GREEN, SMOKE_BLUE).withSound().send();
        RiftCollapseProfile profile = RiftCollapseProfiles.forTier(rift.stabilityTier());
        if (!profile.isInert()) {
            afflictSurroundings(level, rift, profile);
        }
        rift.discard();
    }

    private static void dropLoot(ServerLevel level, EntityFluxRift rift, int strength) {
        RandomSource random = rift.getRandom();
        if (random.nextInt(PEARL_ROLL) < strength) {
            ItemStack pearl = new ItemStack(TTItems.PRIMORDIAL_PEARL.get());
            pearl.set(DataComponents.DAMAGE, PEARL_MIN_WEAR + random.nextInt(PEARL_WEAR_SPREAD));
            rift.spawnAtLocation(level, pearl);
        }
        if (strength > 0) {
            rift.spawnAtLocation(level, new ItemStack(TTItems.VOID_SEED.get(), strength));
        }
    }

    private static void afflictSurroundings(ServerLevel level, EntityFluxRift rift, RiftCollapseProfile profile) {
        Vec3 centre = rift.position();
        AABB area = new AABB(centre, centre).inflate(EFFECT_REACH);
        for (LivingEntity creature : level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
            double distanceSqr = creature.distanceToSqr(centre);
            if (distanceSqr > EFFECT_REACH_SQR) {
                continue;
            }
            float closeness = (float) (1.0 - distanceSqr / EFFECT_REACH_SQR);
            afflict(level, creature, profile, closeness);
        }
    }

    private static void afflict(ServerLevel level, LivingEntity creature, RiftCollapseProfile profile, float closeness) {
        applyTimed(creature, TTMobEffects.FLUX_TAINT, profile.taintSeconds() * closeness);
        applyTimed(creature, MobEffects.WEAKNESS, profile.weaknessSeconds() * closeness);
        if (creature instanceof ServerPlayer player) {
            int warp = (int) (profile.warpPoints() * closeness);
            if (warp > 0) {
                WarpHelper.addWarp(player, warp, WarpType.PERMANENT);
                WarpHelper.addWarp(player, warp, WarpType.TEMPORARY);
            }
        }
    }

    private static void applyTimed(LivingEntity creature, Holder<MobEffect> effect, float seconds) {
        int whole = (int) seconds;
        if (whole > 0) {
            creature.addEffect(new MobEffectInstance(effect, whole * TICKS_PER_SECOND, EFFECT_AMPLIFIER));
        }
    }
}
