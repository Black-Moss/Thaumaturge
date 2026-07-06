package com.leclowndu93150.thaumcraft.api.casters;

import net.minecraft.network.chat.Component;

/**
 * The value model behind a {@link NodeSetting}: maps the setting's internal index onto a
 * real value, clamps the index into range, and describes values for display.
 *
 *
 * @since 1.0.0
 */
public interface INodeSettingType {
    /**
     * The internal index a fresh setting starts at.
     *
     * @return the default index
     */
    int getDefault();

    /**
     * Clamps an internal index into this type's valid range.
     *
     * @param index the raw internal index
     * @return the nearest valid index
     */
    int clamp(int index);

    /**
     * Maps an internal index onto the real value it represents.
     *
     * @param index the internal index; clamped before mapping
     * @return the real value
     */
    int getValue(int index);

    /**
     * A display component describing the value at an internal index.
     *
     * @param index the internal index
     * @return the display text, never null
     */
    Component getValueText(int index);
}
