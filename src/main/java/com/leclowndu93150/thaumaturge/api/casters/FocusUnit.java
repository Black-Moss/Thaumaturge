package com.leclowndu93150.thaumaturge.api.casters;

import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;

/**
 * One configured node of a spell: the id of a registered focus element plus the setting
 * values chosen for it. Split elements additionally carry their branch packages.
 *
 * <p>Units are pure data. The behavior they reference lives in the
 * {@link FocusElementType} registry and is resolved at execution time through
 * {@link FocusEngine#element(Identifier)}.
 *
 * @param element  the id of the focus element this unit configures
 * @param settings the chosen setting values, keyed by {@link SettingDefinition#key()};
 *                 keys absent from the map fall back to the definition's default
 * @param branches the branch packages of a {@link FocusSplit} element; empty for every
 *                 other element kind
 * @since 1.0.0
 */
public record FocusUnit(Identifier element, Map<String, Integer> settings, List<FocusPackage> branches) {
    /**
     * Canonicalizes the components into immutable copies.
     */
    public FocusUnit {
        settings = Map.copyOf(settings);
        branches = List.copyOf(branches);
    }

    /**
     * Creates a unit with default settings and no branches.
     *
     * @param element the focus element id
     * @return the unit
     */
    public static FocusUnit of(Identifier element) {
        return new FocusUnit(element, Map.of(), List.of());
    }

    /**
     * Creates a unit with explicit settings and no branches.
     *
     * @param element  the focus element id
     * @param settings the setting values
     * @return the unit
     */
    public static FocusUnit of(Identifier element, Map<String, Integer> settings) {
        return new FocusUnit(element, settings, List.of());
    }
}
