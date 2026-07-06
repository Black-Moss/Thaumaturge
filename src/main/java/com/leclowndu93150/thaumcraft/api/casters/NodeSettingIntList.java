package com.leclowndu93150.thaumcraft.api.casters;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * A setting type whose internal index selects from a fixed list of values, each with its own
 * translation key for display.
 *
 *
 * @since 1.0.0
 */
public final class NodeSettingIntList implements INodeSettingType {
    private final int[] values;
    private final String[] descriptions;

    /**
     * @param values       the selectable real values, in index order
     * @param descriptions one translation key per value, same length and order as
     *                     {@code values}
     */
    public NodeSettingIntList(int[] values, String[] descriptions) {
        this.values = values;
        this.descriptions = descriptions;
    }

    @Override
    public int getDefault() {
        return 0;
    }

    @Override
    public int clamp(int index) {
        return Mth.clamp(index, 0, values.length - 1);
    }

    @Override
    public int getValue(int index) {
        return values[clamp(index)];
    }

    @Override
    public Component getValueText(int index) {
        return Component.translatable(descriptions[clamp(index)]);
    }
}
