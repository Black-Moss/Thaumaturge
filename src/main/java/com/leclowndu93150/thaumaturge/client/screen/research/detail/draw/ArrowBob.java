package com.leclowndu93150.thaumaturge.client.screen.research.detail.draw;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public final class ArrowBob {
    private static final float MIDPOINT = 0.1F;
    private static final float AMPLITUDE = 0.2F;
    private static final float TICKS_PER_RADIAN = 3.0F;

    private ArrowBob() {}

    public static float swell(@Nullable Player player) {
        if (player == null) {
            return 0.0F;
        }
        return MIDPOINT + AMPLITUDE * Mth.sin(player.tickCount / TICKS_PER_RADIAN);
    }
}
