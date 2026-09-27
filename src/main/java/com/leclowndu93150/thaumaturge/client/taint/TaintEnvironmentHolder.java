package com.leclowndu93150.thaumaturge.client.taint;

import net.minecraft.util.Mth;

public final class TaintEnvironmentHolder {
    private static final float EASING = 0.05F;

    private static float target;
    private static float displayed;

    private TaintEnvironmentHolder() {}

    public static void accept(float pressure) {
        target = Mth.clamp(pressure, 0.0F, 1.0F);
    }

    public static void tick() {
        displayed = Mth.lerp(EASING, displayed, target);
    }

    public static float pressure() {
        return displayed;
    }

    public static void reset() {
        target = 0.0F;
        displayed = 0.0F;
    }
}
