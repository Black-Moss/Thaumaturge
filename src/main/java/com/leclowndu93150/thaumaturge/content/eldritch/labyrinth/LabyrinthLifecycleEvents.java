package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class LabyrinthLifecycleEvents {
    private LabyrinthLifecycleEvents() {}

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == OuterLands.DIMENSION) {
            LabyrinthService.publish(level);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == OuterLands.DIMENSION) {
            LabyrinthService.runtime(level).ifPresent(LabyrinthRuntime::clear);
        }
    }
}
