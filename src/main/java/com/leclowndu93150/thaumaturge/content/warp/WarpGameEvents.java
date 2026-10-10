package com.leclowndu93150.thaumaturge.content.warp;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.warp.roll.WarpCheck;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class WarpGameEvents {
    private static final int WARP_CHECK_INTERVAL = 2000;
    private static final int GAZE_INTERVAL = 20;
    private static final List<Holder<MobEffect>> MILK_RESISTANT = List.of(TTMobEffects.VIS_EXHAUST, TTMobEffects.INFECTIOUS_VIS_EXHAUST, TTMobEffects.THAUMARHIA, TTMobEffects.UNNATURAL_HUNGER,
            TTMobEffects.SUN_SCORNED, TTMobEffects.DEATH_GAZE, TTMobEffects.FLUX_TAINT);

    private WarpGameEvents() {}

    @SubscribeEvent
    public static void onEffectRemove(MobEffectEvent.Remove event) {
        LivingEntity entity = event.getEntity();
        if (!entity.isUsingItem() || !entity.getUseItem().is(Items.MILK_BUCKET)) {
            return;
        }
        Holder<MobEffect> effect = event.getEffect();
        for (Holder<MobEffect> resistant : MILK_RESISTANT) {
            if (effect.value() == resistant.value()) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        int age = player.tickCount;
        if (age > 0 && age % WARP_CHECK_INTERVAL == 0 && !ThaumaturgeCommonConfig.WUSS_MODE.get() && !player.hasEffect(TTMobEffects.WARP_WARD)) {
            WarpCheck.run(player);
        }
        if (age % GAZE_INTERVAL == 0) {
            DeadlyGazePulse.pulse(player);
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            return;
        }
        WarpLedger.change(player, WarpGear.craftingWarp(event.getCrafting()), WarpType.NORMAL);
    }

    @SubscribeEvent
    public static void onItemFinished(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack used = event.getItem();
        if (used.is(TTItems.BRAIN.get())) {
            WarpFoodReactions.brainEaten(player);
        }
        WarpFoodReactions.consumed(player, used);
    }
}
