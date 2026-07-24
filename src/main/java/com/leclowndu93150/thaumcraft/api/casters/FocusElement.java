package com.leclowndu93150.thaumcraft.api.casters;

import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

/**
 * The behavior and metadata of one kind of spell node. Implementations are stateless
 * singletons registered through {@link FocusElementType}; per-spell configuration arrives as
 * {@link FocusSettings} and per-cast state as {@link CastContext}.
 *
 * <p>Every element is one of four kinds, dispatched exhaustively by the engine:
 * {@link FocusMedium} delivers, {@link FocusEffect} applies, {@link FocusMod} transforms,
 * {@link FocusSplit} forks.
 *
 * @since 1.0.0
 */
public sealed interface FocusElement permits FocusMedium, FocusEffect, FocusMod, FocusSplit {
    /** No supplied data. */
    Set<SupplyType> SUPPLIES_NOTHING = Set.of();
    /** Targets only. */
    Set<SupplyType> SUPPLIES_TARGETS = Set.of(SupplyType.TARGET);
    /** Trajectories only. */
    Set<SupplyType> SUPPLIES_TRAJECTORIES = Set.of(SupplyType.TRAJECTORY);
    /** Targets and trajectories. */
    Set<SupplyType> SUPPLIES_BOTH = Set.of(SupplyType.TARGET, SupplyType.TRAJECTORY);

    /**
     * The registry id of this element. Must match the id under which the element's
     * {@link FocusElementType} is registered.
     *
     * @return the element id, e.g. {@code thaumcraft:fire}
     */
    Identifier id();

    /**
     * The complexity this element adds to a focus, limiting what fits into one focus item.
     *
     * @param settings the configured settings
     * @return the complexity cost, zero or greater
     */
    int complexity(FocusSettings settings);

    /**
     * The research required before a player may place this element into a focus.
     *
     * @return the gating research, or null when ungated
     */
    default @Nullable ResearchGate research() {
        return null;
    }

    /**
     * The aspect associated with this element, shown and charged as a crystal in the focal
     * manipulator.
     *
     * @return the aspect key, or null when the element has none
     */
    default @Nullable ResourceKey<IAspect> aspect() {
        return null;
    }

    /**
     * The configurable settings this element exposes.
     *
     * @return the setting definitions; empty when the element has none
     */
    default List<SettingDefinition> settings() {
        return List.of();
    }

    /**
     * The factor this element applies to the spell's power when it executes.
     *
     * @param settings the configured settings
     * @return the power multiplier, 1.0 meaning unchanged
     */
    default float powerMultiplier(FocusSettings settings) {
        return 1.0F;
    }

    /**
     * Whether only one element of this kind may exist in a focus.
     *
     * @return true when the element is exclusive
     */
    default boolean exclusive() {
        return false;
    }

    /**
     * What the previous node must supply for this element to function. An empty set means
     * the element cannot be attached below another node.
     *
     * @return the required supply types
     */
    Set<SupplyType> requires();

    /**
     * What this element supplies to the node after it.
     *
     * @return the provided supply types
     */
    Set<SupplyType> supplies();

    /**
     * The translation key for this element's display name.
     *
     * @return {@code focus.<namespace>.<path>.name}
     */
    default String nameKey() {
        return "focus." + id().getNamespace() + "." + id().getPath() + ".name";
    }

    /**
     * The translation key for this element's descriptive text.
     *
     * @return {@code focus.<namespace>.<path>.text}
     */
    default String descriptionKey() {
        return "focus." + id().getNamespace() + "." + id().getPath() + ".text";
    }

    /**
     * What a spell node can require from or provide to its neighbors.
     *
     * @since 1.0.0
     */
    enum SupplyType {
        /** A resolved hit on an entity or block. */
        TARGET,
        /** A ray still in flight. */
        TRAJECTORY
    }
}
