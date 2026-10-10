package com.leclowndu93150.thaumaturge.content.golem.parts;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.parts.IGolemArmAbility;
import com.leclowndu93150.thaumaturge.content.entity.EntityGolemDart;
import com.leclowndu93150.thaumaturge.content.golem.ai.ShootingCadence;
import com.leclowndu93150.thaumaturge.content.golem.ai.StandAndShootGoal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.jspecify.annotations.Nullable;

public final class DartLauncherArms implements IGolemArmAbility {
    private static final double RANGE = 16.0D;
    private static final int SIGHT_TICKS = 20;
    private static final int BASE_INTERVAL = 20;
    private static final int INTERVAL_AT_FULL_RANGE = 5;
    private static final ShootingCadence CADENCE = new ShootingCadence(RANGE, SIGHT_TICKS, BASE_INTERVAL, INTERVAL_AT_FULL_RANGE);
    private static final double DAMAGE_DIVISOR = 3.0D;
    private static final double DAMAGE_DEVIATION = 0.25D;
    private static final float LAUNCH_SPEED = 1.6F;
    private static final float INACCURACY = 3.0F;
    private static final float SHOT_VOLUME = 1.0F;
    private static final float SHOT_PITCH = 1.0F;
    private static final float SHOT_PITCH_SPREAD = 0.1F;

    @Override
    public @Nullable Goal createRangedGoal(RangedAttackMob mob) {
        return mob instanceof Mob shooter ? new StandAndShootGoal(shooter, mob, CADENCE) : null;
    }

    @Override
    public void onRangedAttack(IGolemAPI golem, LivingEntity target, float power) {
        if (!(golem.level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntity body = golem.asEntity();
        RandomSource random = body.getRandom();
        double damage = body.getAttributeValue(Attributes.ATTACK_DAMAGE) / DAMAGE_DIVISOR + power + random.nextGaussian() * DAMAGE_DEVIATION;
        EntityGolemDart.loose(body, target, damage, power * power, LAUNCH_SPEED, INACCURACY);
        level.playSound(null, body.getX(), body.getY(), body.getZ(), SoundEvents.ARROW_SHOOT, body.getSoundSource(), SHOT_VOLUME, random.triangle(SHOT_PITCH, SHOT_PITCH_SPREAD));
    }
}
