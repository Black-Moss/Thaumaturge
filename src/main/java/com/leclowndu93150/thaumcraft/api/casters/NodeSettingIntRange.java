package com.leclowndu93150.thaumcraft.api.casters;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * A setting type whose internal index is itself the value, clamped into an inclusive range.
 *
 *
 * @since 1.0.0
 */
public final class NodeSettingIntRange implements INodeSettingType {
    private final int min;
    private final int max;

    /**
     * @param min the inclusive lower bound and default value
     * @param max the inclusive upper bound
     */
    public NodeSettingIntRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public int getDefault() {
        return min;
    }

    @Override
    public int clamp(int index) {
        return Mth.clamp(index, min, max);
    }

    @Override
    public int getValue(int index) {
        return clamp(index);
    }

    @Override
    public Component getValueText(int index) {
        return Component.literal(Integer.toString(getValue(index)));
    }
}
