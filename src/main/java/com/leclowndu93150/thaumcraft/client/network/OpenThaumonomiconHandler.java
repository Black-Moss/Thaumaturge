package com.leclowndu93150.thaumcraft.client.network;

import com.leclowndu93150.thaumcraft.client.screen.research.ThaumonomiconBrowserScreen;
import com.leclowndu93150.thaumcraft.network.ClientboundOpenThaumonomiconPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class OpenThaumonomiconHandler {
    private OpenThaumonomiconHandler() {}

    public static void handle(ClientboundOpenThaumonomiconPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> Minecraft.getInstance().setScreen(new ThaumonomiconBrowserScreen()));
    }
}
