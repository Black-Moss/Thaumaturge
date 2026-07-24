package com.leclowndu93150.thaumcraft.api.casters;

import java.util.Map;

/**
 * The resolved setting values of one {@link FocusUnit}, combining the stored values with the
 * defaults of the owning element's {@link SettingDefinition definitions}.
 *
 * @since 1.0.0
 */
public final class FocusSettings {
    private static final FocusSettings EMPTY = new FocusSettings(null, Map.of());

    private final FocusElement element;
    private final Map<String, Integer> values;

    private FocusSettings(FocusElement element, Map<String, Integer> values) {
        this.element = element;
        this.values = values;
    }

    /**
     * Resolves settings for an element.
     *
     * @param element the element supplying the definitions
     * @param values  the stored values, usually {@link FocusUnit#settings()}
     * @return the resolved view
     */
    public static FocusSettings of(FocusElement element, Map<String, Integer> values) {
        return new FocusSettings(element, values);
    }

    /**
     * Resolves an element's default settings.
     *
     * @param element the element supplying the definitions
     * @return the resolved view with every setting at its default
     */
    public static FocusSettings defaults(FocusElement element) {
        return new FocusSettings(element, Map.of());
    }

    /**
     * Settings with no element and no values; every lookup yields 0.
     *
     * @return the empty view
     */
    public static FocusSettings empty() {
        return EMPTY;
    }

    /**
     * The value of a setting: the stored value when present, else the definition's default,
     * else 0 for keys the element does not define.
     *
     * @param key the setting id
     * @return the resolved value
     */
    public int value(String key) {
        Integer stored = values.get(key);
        if (stored != null) {
            return stored;
        }
        if (element != null) {
            for (SettingDefinition definition : element.settings()) {
                if (definition.key().equals(key)) {
                    return definition.defaultValue();
                }
            }
        }
        return 0;
    }
}
