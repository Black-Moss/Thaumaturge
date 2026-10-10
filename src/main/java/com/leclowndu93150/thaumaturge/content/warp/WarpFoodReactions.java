package com.leclowndu93150.thaumaturge.content.warp;

import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class WarpFoodReactions {
    private static final int BRAIN_NORMAL_CHANCE = 10;
    private static final int CHANCE_SCALE = 100;
    private static final int BRAIN_NORMAL_WARP = 1;
    private static final int BRAIN_MIN_TEMPORARY = 1;
    private static final int BRAIN_TEMPORARY_SPREAD = 3;
    private static final int HUNGER_EASE_TICKS = 600;
    private static final int WEAKEST_AMPLIFIER = 0;
    private static final String HUNGER_UNSATED = "warp.thaumaturge.text.hunger.1";
    private static final String HUNGER_FADES = "warp.thaumaturge.text.hunger.2";

    private WarpFoodReactions() {}

    public static void brainEaten(ServerPlayer player) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            return;
        }
        RandomSource random = player.getRandom();
        if (random.nextInt(CHANCE_SCALE) < BRAIN_NORMAL_CHANCE) {
            WarpLedger.change(player, BRAIN_NORMAL_WARP, WarpType.NORMAL);
        } else {
            WarpLedger.change(player, BRAIN_MIN_TEMPORARY + random.nextInt(BRAIN_TEMPORARY_SPREAD), WarpType.TEMPORARY);
        }
    }

    public static void consumed(ServerPlayer player, ItemStack eaten) {
        MobEffectInstance hunger = player.getEffect(TTMobEffects.UNNATURAL_HUNGER);
        if (hunger == null) {
            return;
        }
        if (eaten.is(Items.ROTTEN_FLESH) || eaten.is(TTItems.BRAIN.get())) {
            ease(player, hunger);
            tell(player, HUNGER_FADES, ChatFormatting.GREEN);
        } else if (restoresHunger(eaten)) {
            tell(player, HUNGER_UNSATED, ChatFormatting.DARK_RED);
        }
    }

    private static void ease(ServerPlayer player, MobEffectInstance hunger) {
        int amplifier = hunger.getAmplifier();
        int remaining = hunger.getDuration();
        player.removeEffect(TTMobEffects.UNNATURAL_HUNGER);
        if (amplifier <= WEAKEST_AMPLIFIER || remaining <= HUNGER_EASE_TICKS) {
            return;
        }
        player.addEffect(new MobEffectInstance(TTMobEffects.UNNATURAL_HUNGER, remaining - HUNGER_EASE_TICKS, amplifier - 1, true, false));
    }

    private static boolean restoresHunger(ItemStack eaten) {
        FoodProperties food = eaten.get(DataComponents.FOOD);
        return food != null && food.nutrition() > 0;
    }

    private static void tell(ServerPlayer player, String key, ChatFormatting colour) {
        player.sendSystemMessage(Component.translatable(key).withStyle(colour, ChatFormatting.ITALIC));
    }
}
