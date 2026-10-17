package com.leclowndu93150.thaumaturge.content.warp;

import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class WarpFoodReactions {
    private static final int BRAIN_NORMAL_CHANCE = 10;
    private static final int CHANCE_SCALE = 100;
    private static final int BRAIN_NORMAL_WARP = 1;
    private static final int BRAIN_MIN_TEMPORARY = 1;
    private static final int BRAIN_TEMPORARY_SPREAD = 3;
    private static final int HUNGER_EASE_TICKS = 600;
    private static final String HUNGER_UNSATED = "warp.thaumaturge.text.hunger.1";
    private static final String HUNGER_FADES = "warp.thaumaturge.text.hunger.2";

    private WarpFoodReactions() {}

    public static void brainEaten(ServerPlayer player) {
        RandomSource random = player.getRandom();
        if (random.nextInt(CHANCE_SCALE) < BRAIN_NORMAL_CHANCE) {
            WarpLedger.change(player, BRAIN_NORMAL_WARP, WarpType.NORMAL);
        } else {
            WarpLedger.change(player, BRAIN_MIN_TEMPORARY + random.nextInt(BRAIN_TEMPORARY_SPREAD), WarpType.TEMPORARY);
        }
    }

    public static void consumed(ServerPlayer player, ItemStack stack) {
        if (!stack.has(DataComponents.CONSUMABLE)) {
            return;
        }
        MobEffectInstance hunger = player.getEffect(TTMobEffects.UNNATURAL_HUNGER);
        if (hunger == null) {
            return;
        }
        if (!stack.is(Items.ROTTEN_FLESH) && !stack.is(TTItems.BRAIN.get())) {
            WarpNotices.send(player, HUNGER_UNSATED);
            return;
        }
        int amplifier = hunger.getAmplifier() - 1;
        int duration = hunger.getDuration() - HUNGER_EASE_TICKS;
        player.removeEffect(TTMobEffects.UNNATURAL_HUNGER);
        if (amplifier >= 0 && duration > 0) {
            player.addEffect(new MobEffectInstance(TTMobEffects.UNNATURAL_HUNGER, duration, amplifier, true, true));
        }
        WarpNotices.send(player, HUNGER_FADES);
    }
}
