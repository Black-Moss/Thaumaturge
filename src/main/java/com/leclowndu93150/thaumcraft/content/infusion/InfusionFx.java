package com.leclowndu93150.thaumcraft.content.infusion;

import com.leclowndu93150.thaumcraft.content.fx.FX;
import com.leclowndu93150.thaumcraft.content.fx.TCParticleDispatch;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class InfusionFx {
    private static final int ITEM_STREAM_COLOR = 0xC8B4E6;
    private static final int ITEM_STREAM_EXTEND = 5;

    private InfusionFx() {}

    public static void itemStream(ServerLevel level, BlockPos matrixPos, BlockPos pedestalPos) {
        TCParticleDispatch.spawnEssentiaStream(level,
                Vec3.atCenterOf(pedestalPos.above()),
                Vec3.atCenterOf(matrixPos),
                ITEM_STREAM_COLOR,
                0,
                level.getRandom().nextInt(8),
                0.15F,
                ITEM_STREAM_EXTEND,
                0.0);
    }

    public static void pedestalBamf(ServerLevel level, BlockPos pedestalPos) {
        FX.bamf(level, pedestalPos.above()).withSound().fancy().send();
    }
}
