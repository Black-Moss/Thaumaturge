package com.leclowndu93150.thaumcraft.client.golem;

import com.leclowndu93150.thaumcraft.content.golem.press.BlockEntityGolemBuilder;
import com.leclowndu93150.thaumcraft.network.ClientboundGolemPressStuffPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class GolemPressClientHandler {
    private GolemPressClientHandler() {}

    public static void handle(ClientboundGolemPressStuffPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null
                    && mc.level.getBlockEntity(payload.pos()) instanceof BlockEntityGolemBuilder builder) {
                boolean[] stuff = new boolean[payload.stuff().length];
                for (int i = 0; i < stuff.length; i++) {
                    stuff[i] = payload.stuff()[i] == 1;
                }
                builder.hasStuff = stuff;
            }
        });
    }
}
