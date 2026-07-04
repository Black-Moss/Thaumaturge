package com.leclowndu93150.thaumcraft.content.aura;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.config.ThaumcraftCommonConfig;
import com.leclowndu93150.thaumcraft.content.entity.EntityFluxRift;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class RiftTriggerHandler {
    private static final int POLL_INTERVAL_TICKS = 20;

    private RiftTriggerHandler() {}

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || level.getGameTime() % POLL_INTERVAL_TICKS != 0) {
            return;
        }
        BlockPos trigger = AuraManager.pollRiftTrigger(level);
        if (trigger != null && !ThaumcraftCommonConfig.WUSS_MODE.get()) {
            EntityFluxRift.createRift(level, trigger);
        }
    }
}
