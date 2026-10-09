package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class EquipmentEvents {
    private static final float FALL_DIVISOR = 2.0F;
    private static final float FALL_FLAT_REDUCTION = 1.0F;
    private static final float FALL_CANCEL_BELOW = 1.0F;

    private EquipmentEvents() {}

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || !(player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof TravellerBootsItem) || !event.getSource().is(DamageTypeTags.IS_FALL)) {
            return;
        }
        float reduced = Math.max(0.0F, event.getAmount() / FALL_DIVISOR - FALL_FLAT_REDUCTION);
        if (reduced < FALL_CANCEL_BELOW) {
            event.setCanceled(true);
        } else {
            event.setAmount(reduced);
        }
    }
}
