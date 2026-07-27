package com.leclowndu93150.thaumaturge.debug.network;

import com.leclowndu93150.thaumaturge.debug.client.RaycastDebugOverlay;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ToggleRaycastDebugHandler {

    public static void handle(ClientboundToggleRaycastDebugPayload payload, IPayloadContext context){
        context.enqueueWork(RaycastDebugOverlay::toggle);
    }

    public static void handleServerRaycast(ClientboundRaycastDebugPayload payload, IPayloadContext context){
        context.enqueueWork(()->{
            RaycastDebugOverlay.serverHitResult = payload.result();
        });
    }


}
