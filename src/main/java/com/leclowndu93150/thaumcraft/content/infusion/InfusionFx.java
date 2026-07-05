package com.leclowndu93150.thaumcraft.content.infusion;

import com.leclowndu93150.thaumcraft.content.fx.FX;
import com.leclowndu93150.thaumcraft.network.fx.ClientboundInfusionSourcePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;

public final class InfusionFx {
    private static final double ITEM_STREAM_RANGE = 32.0;

    private InfusionFx() {}

    public static void itemStream(ServerLevel level, BlockPos matrixPos, BlockPos pedestalPos) {
        PacketDistributor.sendToPlayersNear(level, null,
                matrixPos.getX(), matrixPos.getY(), matrixPos.getZ(), ITEM_STREAM_RANGE,
                new ClientboundInfusionSourcePayload(matrixPos, pedestalPos));
    }

    public static void pedestalBamf(ServerLevel level, BlockPos pedestalPos) {
        FX.bamf(level, pedestalPos.above()).withSound().fancy().send();
    }
}
