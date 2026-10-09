package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityInfusionMatrix;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityPedestal;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundInfusionSourcePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class InfusionSourceClientHandler {
    private static final int PEDESTAL_SOURCE_TICKS = 60;
    private static final int OTHER_SOURCE_TICKS = 15;

    private InfusionSourceClientHandler() {}

    public static void handle(ClientboundInfusionSourcePayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> apply(payload));
    }

    private static void apply(ClientboundInfusionSourcePayload payload) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null)
            return;
        if (!(level.getBlockEntity(payload.matrixPos()) instanceof BlockEntityInfusionMatrix matrix))
            return;
        BlockEntity source = level.getBlockEntity(payload.sourcePos());
        int ticks = source instanceof BlockEntityPedestal ? PEDESTAL_SOURCE_TICKS : OTHER_SOURCE_TICKS;
        matrix.addClientSourceFX(payload.sourcePos(), ticks);
    }
}
