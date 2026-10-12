package com.leclowndu93150.thaumaturge.content.entity.ai;

import com.leclowndu93150.thaumaturge.content.entity.WispEntity;
import com.leclowndu93150.thaumaturge.network.ClientboundWispZapPayload;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class WispZapGoal extends Goal {
    private static final double ZAP_RANGE_SQR = 16.0 * 16.0;
    private static final double HOVER_RANGE_SQR = 16.0 * 16.0 / 2.0;
    private static final double AIM_HEIGHT_FRACTION = 2.0 / 3.0;
    private static final double CHASE_ACCELERATION = 0.025;
    private static final int CHARGE_TO_FIRE = 20;
    private static final int RECHARGE_SPREAD = 20;
    private static final double STILL_THRESHOLD = 0.1;
    private static final float STILL_HIT_CHANCE = 0.66F;
    private static final float MOVING_HIT_CHANCE = 0.4F;
    private static final float STILL_DAMAGE_BONUS = 1.0F;
    private static final double ARC_VIEW_RANGE = 32.0;
    private static final float ZAP_VOLUME = 1.0F;
    private static final float ZAP_PITCH = 1.1F;

    private final WispEntity wisp;
    private int charge;

    public WispZapGoal(WispEntity wisp) {
        this.wisp = wisp;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = wisp.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void start() {
        charge = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = wisp.getTarget();
        if (target == null) {
            return;
        }
        boolean visible = wisp.getSensing().hasLineOfSight(target);
        double distanceSqr = wisp.distanceToSqr(target);
        if (visible) {
            steer(target, distanceSqr);
        }
        if (distanceSqr > ZAP_RANGE_SQR) {
            return;
        }
        if (!visible) {
            charge = charge > 0 ? charge - 1 : charge;
            return;
        }
        if (++charge >= CHARGE_TO_FIRE && wisp.level() instanceof ServerLevel level) {
            zap(level, target);
        }
    }

    private void steer(LivingEntity target, double distanceSqr) {
        wisp.getMoveControl().setWait();
        if (distanceSqr <= HOVER_RANGE_SQR) {
            return;
        }
        Vec3 aim = new Vec3(target.getX(), target.getY() + target.getEyeHeight() * AIM_HEIGHT_FRACTION, target.getZ());
        Vec3 heading = aim.subtract(wisp.position()).normalize();
        wisp.setDeltaMovement(wisp.getDeltaMovement().add(heading.scale(CHASE_ACCELERATION)));
    }

    private void zap(ServerLevel level, LivingEntity target) {
        RandomSource random = wisp.getRandom();
        level.playSound(null, wisp.getX(), wisp.getY(), wisp.getZ(), TTSounds.ZAP.get(), SoundSource.HOSTILE, ZAP_VOLUME, ZAP_PITCH);
        PacketDistributor.sendToPlayersNear(level, null, wisp.getX(), wisp.getY(), wisp.getZ(), ARC_VIEW_RANGE, new ClientboundWispZapPayload(wisp.getId(), target.getId()));
        boolean still = isStill(target.getKnownMovement());
        float chance = still ? STILL_HIT_CHANCE : MOVING_HIT_CHANCE;
        if (random.nextFloat() < chance) {
            float damage = (float) wisp.getAttributeValue(Attributes.ATTACK_DAMAGE) + (still ? STILL_DAMAGE_BONUS : 0.0F);
            target.hurtServer(level, wisp.damageSources().mobAttack(wisp), damage);
        }
        charge = -1 - random.nextInt(RECHARGE_SPREAD);
    }

    private static boolean isStill(Vec3 motion) {
        return Math.abs(motion.x) <= STILL_THRESHOLD && Math.abs(motion.y) <= STILL_THRESHOLD && Math.abs(motion.z) <= STILL_THRESHOLD;
    }
}
