package com.leclowndu93150.thaumcraft.network;

import com.leclowndu93150.thaumcraft.content.golem.press.BlockEntityGolemBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerboundGolemPressHandler {
    private ServerboundGolemPressHandler() {}

    public static void handle(ServerboundGolemPressPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!(player.level().getBlockEntity(payload.pos()) instanceof BlockEntityGolemBuilder builder)) {
                return;
            }
            if (payload.craft()) {
                builder.startCraft(payload.props(), player);
            } else {
                boolean[] stuff = builder.checkCraft(payload.props());
                byte[] bytes = new byte[stuff.length];
                for (int i = 0; i < stuff.length; i++) {
                    bytes[i] = (byte) (stuff[i] ? 1 : 0);
                }
                PacketDistributor.sendToPlayer(player,
                        new ClientboundGolemPressStuffPayload(payload.pos(), bytes));
            }
        });
    }
}
