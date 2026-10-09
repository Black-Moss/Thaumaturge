package com.leclowndu93150.thaumaturge.content.warp;

import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public final class DeadlyGazePulse {
    private static final int BASE_RANGE = 8;
    private static final int RANGE_PER_LEVEL = 3;
    private static final int MAX_RANGE = 24;
    private static final int WITHER_TICKS = 80;
    private static final int WITHER_AMPLIFIER = 0;

    private DeadlyGazePulse() {}

    public static void pulse(ServerPlayer player) {
        MobEffectInstance gaze = player.getEffect(TTMobEffects.DEATH_GAZE);
        if (gaze == null) {
            return;
        }
        ServerLevel level = player.level();
        int range = Math.min(BASE_RANGE + RANGE_PER_LEVEL * gaze.getAmplifier(), MAX_RANGE);
        boolean pvp = level.isPvpAllowed();
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(range), EntitySelector.NO_SPECTATORS);
        for (LivingEntity target : targets) {
            if (!isWitherable(player, target, pvp)) {
                continue;
            }
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHER_TICKS, WITHER_AMPLIFIER, false, true));
            if (target instanceof Mob mob) {
                mob.setTarget(player);
            }
        }
    }

    private static boolean isWitherable(ServerPlayer player, LivingEntity target, boolean pvp) {
        if (target == player || !target.isAlive() || target.hasEffect(MobEffects.WITHER)) {
            return false;
        }
        if (target instanceof Player && !pvp) {
            return false;
        }
        return player.hasLineOfSight(target);
    }
}
