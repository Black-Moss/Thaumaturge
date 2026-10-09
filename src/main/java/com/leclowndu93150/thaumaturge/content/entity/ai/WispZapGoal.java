package com.leclowndu93150.thaumaturge.content.entity.ai;

import com.leclowndu93150.thaumaturge.content.entity.WispEntity;
import com.leclowndu93150.thaumaturge.network.ClientboundWispZapPayload;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class WispZapGoal extends Goal {
    private static final double APPROACH_DISTANCE_SQR = 128.0;
    private static final double APPROACH_EYE_FRACTION = 2.0 / 3.0;
    private static final double APPROACH_SPEED = 1.0;
    private static final double CHARGE_RANGE = 16.0;
    private static final double CHARGE_RANGE_SQR = CHARGE_RANGE * CHARGE_RANGE;
    private static final int CHARGE_TICKS = 20;
    private static final int RECHARGE_MIN_TICKS = 1;
    private static final int RECHARGE_SPREAD_TICKS = 20;
    private static final float ZAP_VOLUME = 1.0F;
    private static final float ZAP_PITCH = 1.1F;
    private static final double STILL_SPEED = 0.1;
    private static final float STILL_HIT_CHANCE = 0.66F;
    private static final float MOVING_HIT_CHANCE = 0.4F;
    private static final float STILL_DAMAGE_BONUS = 1.0F;

    private final WispEntity wisp;
    private int charge;

    public WispZapGoal(WispEntity wisp) {
        this.wisp = wisp;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return wisp.getTarget() != null;
    }

    @Override
    public void start() {
        charge = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = livingTarget();
        if (target != null) {
            engage(target);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private LivingEntity livingTarget() {
        LivingEntity target = wisp.getTarget();
        return target != null && target.isAlive() ? target : null;
    }

    private void engage(LivingEntity target) {
        boolean visible = wisp.hasLineOfSight(target);
        double distanceSqr = wisp.distanceToSqr(target);
        if (visible && distanceSqr > APPROACH_DISTANCE_SQR) {
            moveTowards(target);
        }
        if (visible && distanceSqr < CHARGE_RANGE_SQR) {
            chargeUp(target);
        } else {
            discharge();
        }
    }

    private void moveTowards(LivingEntity target) {
        double aimY = target.getY() + target.getEyeHeight() * APPROACH_EYE_FRACTION;
        wisp.getMoveControl().setWantedPosition(target.getX(), aimY, target.getZ(), APPROACH_SPEED);
    }

    private void chargeUp(LivingEntity target) {
        charge++;
        if (charge != CHARGE_TICKS || !(wisp.level() instanceof ServerLevel level)) {
            return;
        }
        zap(level, target);
        charge = -RECHARGE_MIN_TICKS - wisp.getRandom().nextInt(RECHARGE_SPREAD_TICKS);
    }

    private void discharge() {
        if (charge > 0) {
            charge--;
        }
    }

    private void zap(ServerLevel level, LivingEntity target) {
        level.playSound(null, wisp.getX(), wisp.getY(), wisp.getZ(), TTSounds.ZAP.get(), SoundSource.HOSTILE, ZAP_VOLUME, ZAP_PITCH);
        PacketDistributor.sendToPlayersTrackingEntity(wisp, new ClientboundWispZapPayload(wisp.getId(), target.getId()));
        RandomSource random = wisp.getRandom();
        boolean still = isStill(target.getDeltaMovement());
        float damage = (float) wisp.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (random.nextFloat() < (still ? STILL_HIT_CHANCE : MOVING_HIT_CHANCE)) {
            target.hurtServer(level, wisp.damageSources().mobAttack(wisp), still ? damage + STILL_DAMAGE_BONUS : damage);
        }
    }

    private static boolean isStill(Vec3 motion) {
        return Math.abs(motion.x) <= STILL_SPEED && Math.abs(motion.y) <= STILL_SPEED && Math.abs(motion.z) <= STILL_SPEED;
    }
}
