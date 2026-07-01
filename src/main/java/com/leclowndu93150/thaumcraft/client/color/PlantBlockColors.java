package com.leclowndu93150.thaumcraft.client.color;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSources;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class PlantBlockColors {
    private PlantBlockColors() {}

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.grassBlock()), TCBlocks.GRASS_AMBIENT.get());
    }
}
