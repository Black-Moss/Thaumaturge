package com.leclowndu93150.thaumcraft.client.input;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.client.screen.research.ThaumonomiconBrowserScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCKeybinds {
    public static final KeyMapping OPEN_THAUMONOMICON = new KeyMapping(
            "key.thaumcraft.thaumonomicon",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            KeyMapping.Category.MISC
    );

    private TCKeybinds() {}

    @SubscribeEvent
    public static void onRegisterMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_THAUMONOMICON);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().screen != null) return;
        while (OPEN_THAUMONOMICON.consumeClick()) {
            Minecraft.getInstance().setScreen(new ThaumonomiconBrowserScreen());
        }
    }
}
