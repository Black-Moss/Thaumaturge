package com.leclowndu93150.thaumcraft.network;

import com.leclowndu93150.thaumcraft.content.essentia.thaumatorium.BlockEntityThaumatorium;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerboundThaumatoriumToggleHandler {
    private ServerboundThaumatoriumToggleHandler() {}

    public static void handle(ServerboundThaumatoriumTogglePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !(player.level() instanceof ServerLevel level)) {
                return;
            }
            if (payload.pos().distToCenterSqr(player.getX(), player.getY(), player.getZ()) > 64.0) {
                return;
            }
            if (level.getBlockEntity(payload.pos()) instanceof BlockEntityThaumatorium machine) {
                machine.toggleRecipe(level, player, payload.recipeId());
            }
        });
    }
}
