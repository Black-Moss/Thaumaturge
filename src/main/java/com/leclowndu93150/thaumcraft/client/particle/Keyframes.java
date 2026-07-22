package com.leclowndu93150.thaumcraft.client.particle;

import net.minecraft.util.Mth;

public final class Keyframes {
    private Keyframes() {}

    public static float sample(float progress, float... keys) {
        if (keys.length == 1) {
            return keys[0];
        }
        float scaled = Mth.clamp(progress, 0.0F, 1.0F) * (keys.length - 1);
        int index = Math.min((int) scaled, keys.length - 2);
        return Mth.lerp(scaled - index, keys[index], keys[index + 1]);
    }
}
