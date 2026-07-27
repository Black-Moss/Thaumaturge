package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.TCIds;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class DustTriggerBlockedBreakHandler {
    private DustTriggerBlockedBreakHandler() {}

    @SubscribeEvent
    public static void onBreak(BreakBlockEvent event) {
        if (DustTriggerSwapQueue.isBlocked(event.getLevel(), event.getPos())) {
            event.setCanceled(true);
            event.setNotifyClient(true);
        }
    }
}
