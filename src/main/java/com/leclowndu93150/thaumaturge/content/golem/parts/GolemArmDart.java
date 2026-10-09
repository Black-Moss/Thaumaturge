package com.leclowndu93150.thaumaturge.content.golem.parts;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.parts.IGolemArmAbility;
import com.leclowndu93150.thaumaturge.content.entity.EntityGolemDart;
import com.leclowndu93150.thaumaturge.content.golem.ai.VolleyGoal;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;

public final class GolemArmDart implements IGolemArmAbility {
    private static final double BASE_DAMAGE_FRACTION = 3.0;
    private static final double DAMAGE_JITTER = 0.25;
    private static final float DART_VELOCITY = 1.6F;
    private static final float DART_SCATTER = 3.0F;
    private static final float SOUND_LOUDNESS = 1.0F;
    private static final float PITCH_RANGE = 0.4F;
    private static final float PITCH_FLOOR = 0.8F;
    private static final double APPROACH_SPEED = 1.0;
    private static final int COOLDOWN_LOW = 20;
    private static final int COOLDOWN_HIGH = 25;
    private static final float RANGE = 16.0F;

    @Override
    public Goal createRangedGoal(RangedAttackMob mob) {
        return new VolleyGoal(mob, APPROACH_SPEED, COOLDOWN_LOW, COOLDOWN_HIGH, RANGE);
    }

    @Override
    public void onRangedAttack(IGolemAPI golem, LivingEntity target, float power) {
        fire(golem.asEntity(), target, power);
    }

    private static void fire(LivingEntity owner, LivingEntity target, float power) {
        Level level = owner.level();
        if (level.isClientSide()) {
            return;
        }
        RandomSource rng = owner.getRandom();
        launchDart(owner, target, power, rng);
        playShotSound(level, owner, shotPitch(rng));
    }

    private static void launchDart(LivingEntity owner, LivingEntity target, float power, RandomSource rng) {
        double strike = owner.getAttributeValue(Attributes.ATTACK_DAMAGE) / BASE_DAMAGE_FRACTION;
        double total = strike + power + rng.nextGaussian() * DAMAGE_JITTER;
        EntityGolemDart.loose(owner, target, total, power * power, DART_VELOCITY, DART_SCATTER);
    }

    private static void playShotSound(Level level, LivingEntity source, float pitch) {
        SoundSource category = source.getSoundSource();
        level.playSound(null, source, SoundEvents.ARROW_SHOOT, category, SOUND_LOUDNESS, pitch);
    }

    private static float shotPitch(RandomSource rng) {
        return 1.0F / (rng.nextFloat() * PITCH_RANGE + PITCH_FLOOR);
    }
}
