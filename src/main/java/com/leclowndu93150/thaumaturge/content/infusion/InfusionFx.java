package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundInfusionSourcePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class InfusionFx {
    private static final double ITEM_STREAM_RANGE = 32.0;
    private static final float UNREST_RED = 0.65F;
    private static final float UNREST_GREEN = 0.2F;
    private static final float UNREST_BLUE = 0.9F;
    private static final float UNREST_SIZE = 0.35F;

    private InfusionFx() {}

    public static void itemStream(ServerLevel level, BlockPos matrixPos, BlockPos pedestalPos) {
        PacketDistributor.sendToPlayersNear(level, null, matrixPos.getX(), matrixPos.getY(), matrixPos.getZ(), ITEM_STREAM_RANGE, new ClientboundInfusionSourcePayload(matrixPos, pedestalPos));
    }

    public static void unrestSpark(ServerLevel level, Vec3 pos) {
        Effects.spark(level, pos).color(UNREST_RED, UNREST_GREEN, UNREST_BLUE).size(UNREST_SIZE).send();
    }

    public static void pedestalBamf(ServerLevel level, BlockPos pedestalPos) {
        Effects.bamf(level, pedestalPos.above()).withSound().fancy().send();
    }
}
