package com.leclowndu93150.thaumaturge.client;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectDiscoveryView;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCClientBindings {
    private TCClientBindings() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        AspectDiscoveryView.bind(aspect -> {
            Player player = Minecraft.getInstance().player;
            return player == null || AspectPools.isDiscovered(player, aspect);
        });
    }
}
