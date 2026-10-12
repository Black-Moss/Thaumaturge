package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntityGolemOrb;
import com.leclowndu93150.thaumaturge.content.entity.eldritch.LeadingAim;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

final class GolemBeamCore {
    private static final int CAPACITY = 150;
    private static final int RECHARGE_PER_TICK = 1;
    private static final int SHOT_COST_BASE = 15;
    private static final int SHOT_COST_SPREAD = 5;
    private static final float ORB_SPEED = 0.66F;
    private static final float ORB_INACCURACY = 5.0F;
    private static final float ORB_VOLUME = 1.0F;
    private static final float ORB_PITCH_BASE = 0.9F;
    private static final float ORB_PITCH_SPREAD = 0.2F;
    private static final boolean WHITE_ORB = false;
    private static final int ARC_COLOR = 0xD8F6FF;
    private static final float ARC_WIDTH = 0.6F;
    private static final int ARC_REFRESH_TICKS = 2;
    private static final int ARC_HOLD_TICKS = 10;
    private static final double ARC_REACH_MIN = 1.5;
    private static final double ARC_REACH_SPREAD = 1.25;
    private static final double ARC_FLOOR_LIFT = 0.05;
    private static final double ARC_SOURCE_HEIGHT = 0.55;
    private static final float HUM_VOLUME = 0.6F;
    private static final float HUM_PITCH_BASE = 0.9F;
    private static final float HUM_PITCH_SPREAD = 0.3F;

    private final EntityEldritchGolem golem;
    private int charge;
    private boolean recharging = true;
    private int arcAge;
    private Vec3 arcSpot = Vec3.ZERO;

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
        EntityGolemOrb orb = new EntityGolemOrb(level, golem, target, WHITE_ORB);
        Vec3 aim = LeadingAim.at(orb, target);
        orb.shoot(aim.x, aim.y, aim.z, ORB_SPEED, ORB_INACCURACY);
        level.addFreshEntity(orb);
        level.playSound(null, golem.getX(), golem.getY(), golem.getZ(), TTSounds.EGATTACK.get(), SoundSource.HOSTILE, ORB_VOLUME, ORB_PITCH_BASE + random.nextFloat() * ORB_PITCH_SPREAD);
        charge -= SHOT_COST_BASE + random.nextInt(SHOT_COST_SPREAD);
        if (charge <= 0) {
            charge = 0;
            recharging = true;
            arcAge = 0;
        }
        return true;
    }

    private void crackle(ServerLevel level) {
        if (arcAge % ARC_HOLD_TICKS == 0) {
            pickArcSpot(level);
        }
        if (arcAge % ARC_REFRESH_TICKS == 0) {
            Vec3 source = golem.position().add(0.0, golem.getBbHeight() * ARC_SOURCE_HEIGHT, 0.0);
            Effects.boltStrike(level, source).to(arcSpot).color(ARC_COLOR).width(ARC_WIDTH).send();
        }
        arcAge++;
    }

    private void pickArcSpot(ServerLevel level) {
        RandomSource random = golem.getRandom();
        float bearing = random.nextFloat() * Mth.TWO_PI;
        double reach = golem.getBbWidth() / 2.0 + ARC_REACH_MIN + random.nextDouble() * ARC_REACH_SPREAD;
        arcSpot = new Vec3(golem.getX() + Mth.cos(bearing) * reach, golem.getY() + ARC_FLOOR_LIFT, golem.getZ() + Mth.sin(bearing) * reach);
        level.playSound(null, golem.getX(), golem.getY(), golem.getZ(), TTSounds.JACOBS.get(), SoundSource.HOSTILE, HUM_VOLUME, HUM_PITCH_BASE + random.nextFloat() * HUM_PITCH_SPREAD);
    }
}
