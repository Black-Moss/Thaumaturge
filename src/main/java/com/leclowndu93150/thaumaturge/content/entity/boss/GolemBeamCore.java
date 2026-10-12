package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntityGolemOrb;
import com.leclowndu93150.thaumaturge.content.entity.eldritch.LeadingAim;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

final class GolemBeamCore {
    private static final int CAPACITY = 150;
    private static final int RECHARGE_PER_TICK = 1;
    private static final int SHOT_COST_BASE = 15;
    private static final int SHOT_COST_SPREAD = 5;
    private static final float ORB_SPEED = 0.66F;
    private static final float ORB_INACCURACY = 5.0F;
    private static final float ORB_VOLUME = 1.0F;
    private static final float ORB_PITCH_BASE = 1.0F;
    private static final float ORB_PITCH_SPREAD = 0.1F;
    private static final boolean WHITE_ORB = false;
    private static final float AIM_TURN_LIMIT = 30.0F;
    private static final int ARC_COLOR = 0xA6FFFF;
    private static final int CRACKLE_INTERVAL = 5;
    private static final int BEARING_STEPS = 360;
    private static final double ARC_REACH_MIN = 2.0;
    private static final double ARC_REACH_SPREAD = 2.0;
    private static final int GROUND_SEARCH_STEPS = 5;
    private static final int ARC_COUNT_MIN = 8;
    private static final int ARC_COUNT_SPREAD = 5;
    private static final double ARC_SOURCE_HEIGHT = 0.5;
    private static final float HUM_VOLUME = 0.8F;
    private static final float HUM_PITCH_BASE = 1.0F;
    private static final float HUM_PITCH_SPREAD = 0.05F;

    private final EntityEldritchGolem golem;
    private int charge;
    private boolean recharging = true;
    private int pendingArcs;
    private BlockPos arcSpot = BlockPos.ZERO;

    GolemBeamCore(EntityEldritchGolem golem) {
        this.golem = golem;
    }

    void tick(ServerLevel level) {
        if (!recharging) {
            return;
        }
        charge = Math.min(CAPACITY, charge + RECHARGE_PER_TICK);
        crackle(level);
        if (charge >= CAPACITY) {
            recharging = false;
        }
    }

    boolean fire(ServerLevel level, LivingEntity target) {
        if (recharging) {
            return false;
        }
        RandomSource random = golem.getRandom();
        Vec3 targetCentre = target.getBoundingBox().getCenter();
        golem.getLookControl().setLookAt(targetCentre.x, targetCentre.y, targetCentre.z, AIM_TURN_LIMIT, AIM_TURN_LIMIT);
        Vec3 look = golem.getLookAngle();
        EntityGolemOrb orb = new EntityGolemOrb(level, golem, target, WHITE_ORB);
        orb.setPos(orb.getX() + look.x, orb.getY(), orb.getZ() + look.z);
        Vec3 aim = LeadingAim.at(orb, target);
        orb.shoot(aim.x, aim.y, aim.z, ORB_SPEED, ORB_INACCURACY);
        level.addFreshEntity(orb);
        if (!golem.isSilent()) {
            level.playSound(null, golem.getX(), golem.getY(), golem.getZ(), TTSounds.EGATTACK.get(), SoundSource.HOSTILE, ORB_VOLUME, ORB_PITCH_BASE + random.nextFloat() * ORB_PITCH_SPREAD);
        }
        charge -= SHOT_COST_BASE + random.nextInt(SHOT_COST_SPREAD);
        if (charge <= 0) {
            charge = 0;
            recharging = true;
        }
        return true;
    }

    private void crackle(ServerLevel level) {
        if (golem.tickCount % CRACKLE_INTERVAL != 0) {
            return;
        }
        if (pendingArcs <= 0) {
            pickArcSpot(level);
            return;
        }
        pendingArcs--;
        Vec3 source = golem.position().add(0.0, golem.getBbHeight() * ARC_SOURCE_HEIGHT, 0.0);
        Effects.zapArc(level, source).to(Vec3.atBottomCenterOf(arcSpot.above())).color(ARC_COLOR).send();
    }

    private void pickArcSpot(ServerLevel level) {
        RandomSource random = golem.getRandom();
        float bearing = random.nextInt(BEARING_STEPS) * Mth.DEG_TO_RAD;
        double reach = ARC_REACH_MIN + random.nextDouble() * ARC_REACH_SPREAD;
        BlockPos.MutableBlockPos probe = BlockPos.containing(golem.getX() + Mth.cos(bearing) * reach, golem.getY(), golem.getZ() + Mth.sin(bearing) * reach).mutable();
        BlockState state = level.getBlockState(probe);
        for (int step = 0; step < GROUND_SEARCH_STEPS && state.isAir(); step++) {
            probe.move(Direction.DOWN);
            state = level.getBlockState(probe);
        }
        if (state.isAir() || !level.getBlockState(probe.above()).isAir()) {
            return;
        }
        arcSpot = probe.immutable();
        pendingArcs = ARC_COUNT_MIN + random.nextInt(ARC_COUNT_SPREAD);
        if (!golem.isSilent()) {
            level.playSound(null, golem.getX(), golem.getY(), golem.getZ(), TTSounds.JACOBS.get(), SoundSource.HOSTILE, HUM_VOLUME, HUM_PITCH_BASE + (random.nextFloat() - random.nextFloat()) * HUM_PITCH_SPREAD);
        }
    }
}
