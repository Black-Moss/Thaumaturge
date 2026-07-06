package com.leclowndu93150.thaumcraft.api.casters;

import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * A configurable knob on a {@link FocusNode}, such as projectile speed or effect potency.
 * The setting stores an internal index; its {@link INodeSettingType} maps that index onto a
 * real value and clamps it into range.
 *
 * <p>The description is a translation key rather than pre-localized text, and the research gate is
 * a typed {@link ResearchGate} rather than a string.
 *
 * @since 1.0.0
 */
public class NodeSetting {
    private final String key;
    private final String description;
    private final INodeSettingType type;
    private final @Nullable ResearchGate research;
    private int value;

    /**
     * @param key         the setting id, unique within its node
     * @param description the translation key for the setting's display name
     * @param type        the value model
     * @param research    the research required to change this setting, or null when ungated
     */
    public NodeSetting(String key, String description, INodeSettingType type, @Nullable ResearchGate research) {
        this.key = key;
        this.description = description;
        this.type = type;
        this.research = research;
        this.value = type.getDefault();
    }

    /**
     * Creates an ungated setting.
     *
     * @param key         the setting id, unique within its node
     * @param description the translation key for the setting's display name
     * @param type        the value model
     */
    public NodeSetting(String key, String description, INodeSettingType type) {
        this(key, description, type, null);
    }

    /**
     * The setting id, unique within its node.
     *
     * @return the setting key
     */
    public String getKey() {
        return key;
    }

    /**
     * The real value the current internal index maps to.
     *
     * @return the current value
     */
    public int getValue() {
        return type.getValue(value);
    }

    /**
     * Sets the setting by real value: resets the internal index and increments until the
     * mapped value matches, stopping when the index no longer changes.
     *
     * @param trueValue the real value to select
     */
    public void setValue(int trueValue) {
        int last = -1;
        value = 0;
        while (getValue() != trueValue && last != value) {
            last = value;
            increment();
        }
    }

    /**
     * The research required to change this setting.
     *
     * @return the gating research, or null when ungated
     */
    public @Nullable ResearchGate getResearch() {
        return research;
    }

    /**
     * A display component for the current value.
     *
     * @return the value text, never null
     */
    public Component getValueText() {
        return type.getValueText(value);
    }

    /** Advances the internal index by one, clamped into range. */
    public void increment() {
        value = type.clamp(value + 1);
    }

    /** Retreats the internal index by one, clamped into range. */
    public void decrement() {
        value = type.clamp(value - 1);
    }

    /**
     * The value model backing this setting.
     *
     * @return the setting type
     */
    public INodeSettingType getType() {
        return type;
    }

    /**
     * The display name of this setting.
     *
     * @return the translated name component
     */
    public Component getName() {
        return Component.translatable(description);
    }
}
