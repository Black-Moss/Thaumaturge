package com.leclowndu93150.thaumcraft.client.warp;

import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = TCIds.MODID)
public final class WarpFogEvents {
    private static final float MIST_FAR_PLANE = 12.0F;
    private static final float MIST_NEAR_PLANE = 2.0F;

    private WarpFogEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        WarpFogState.tick();
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        WarpFogState.reset();
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (!WarpFogState.active()) {
            return;
        }
        float intensity = WarpFogState.intensity();
        FogData fog = event.getFogData();
        fog.environmentalEnd = Mth.lerp(intensity, fog.environmentalEnd, MIST_FAR_PLANE);
        fog.environmentalStart = Mth.lerp(intensity, fog.environmentalStart, MIST_NEAR_PLANE);
    }
}
