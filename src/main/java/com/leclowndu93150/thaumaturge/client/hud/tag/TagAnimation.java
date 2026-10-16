package com.leclowndu93150.thaumaturge.client.hud.tag;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public final class TagAnimation {
    private static final double GROW_TICKS = 10.0;

    private final float cap;
    private @Nullable Object target;
    private double startTime;

    public TagAnimation(float cap) {
        this.cap = cap;
    }

    public float advance(Object currentTarget) {
        double now = clock();
        if (!currentTarget.equals(target)) {
            target = currentTarget;
            startTime = now;
        }
        float progress = (float) Mth.clamp((now - startTime) / GROW_TICKS, 0.0, 1.0);
        float remainder = 1.0F - progress;
        return cap * (1.0F - remainder * remainder * remainder);
    }

    public void reset() {
        target = null;
        startTime = 0.0;
    }

    private static double clock() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return 0.0;
        }
        return level.getGameTime() + minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }
}
