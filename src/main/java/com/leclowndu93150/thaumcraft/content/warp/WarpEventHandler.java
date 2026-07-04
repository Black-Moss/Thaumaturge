package com.leclowndu93150.thaumcraft.content.warp;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.warp.ItemWarp;
import com.leclowndu93150.thaumcraft.api.warp.WarpType;
import com.leclowndu93150.thaumcraft.config.ThaumcraftCommonConfig;
import com.leclowndu93150.thaumcraft.registry.TCDataMaps;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.registry.TCMobEffects;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class WarpEventHandler {
    private static final int WARP_CHECK_INTERVAL = 2000;
    private static final int DEATH_GAZE_INTERVAL = 20;
    private static final int HUNGER_CURE_DURATION_STEP = 600;

    private WarpEventHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!ThaumcraftCommonConfig.WUSS_MODE.get()
                && player.tickCount > 0
                && player.tickCount % WARP_CHECK_INTERVAL == 0
                && !player.hasEffect(TCMobEffects.WARP_WARD)) {
            WarpEvents.checkWarpEvent(player);
        }
        if (player.tickCount % DEATH_GAZE_INTERVAL == 0 && player.hasEffect(TCMobEffects.DEATH_GAZE)) {
            WarpEvents.checkDeathGaze(player);
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (ThaumcraftCommonConfig.WUSS_MODE.get()
                || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemWarp warp = event.getCrafting().getItem().builtInRegistryHolder().getData(TCDataMaps.ITEM_WARP);
        if (warp != null) {
            WarpManager.addWarp(player, warp.amount(), WarpType.NORMAL);
        }
    }

    @SubscribeEvent
    public static void onFinishUsingItem(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        ItemStack used = event.getItem();
        if (used.is(TCItems.BRAIN.get()) && !ThaumcraftCommonConfig.WUSS_MODE.get()) {
            if (player.getRandom().nextFloat() < 0.1F) {
                WarpManager.addWarp(player, 1, WarpType.NORMAL);
            } else {
                WarpManager.addWarp(player, 1 + player.getRandom().nextInt(3), WarpType.TEMPORARY);
            }
        }
        MobEffectInstance hunger = player.getEffect(TCMobEffects.UNNATURAL_HUNGER);
        if (hunger == null || used.get(DataComponents.CONSUMABLE) == null) {
            return;
        }
        if (used.is(Items.ROTTEN_FLESH) || used.is(TCItems.BRAIN.get())) {
            player.removeEffect(TCMobEffects.UNNATURAL_HUNGER);
            int amplifier = hunger.getAmplifier() - 1;
            int duration = hunger.getDuration() - HUNGER_CURE_DURATION_STEP;
            if (duration > 0 && amplifier >= 0) {
                player.addEffect(new MobEffectInstance(TCMobEffects.UNNATURAL_HUNGER, duration, amplifier, true, true));
            }
            WarpManager.sendActionBar(player, "warp.thaumcraft.text.hunger.2");
        } else {
            WarpManager.sendActionBar(player, "warp.thaumcraft.text.hunger.1");
        }
    }
}
