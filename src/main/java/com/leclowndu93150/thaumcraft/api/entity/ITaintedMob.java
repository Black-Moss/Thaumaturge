package com.leclowndu93150.thaumcraft.api.entity;

/**
 * Marker interface for mobs that count as part of the taint biome ecology. Tainted mobs are
 * immune to {@code thaumcraft:flux_taint}, friend-fire one another, and are valid targets for
 * various taint-themed mechanics.
 *
 * <p>Implementations should also be added to the {@code #thaumcraft:tainted} entity-type tag so
 * data-driven predicates can identify them without an instanceof check.
 *
 * @since 1.0.0
 */
public interface ITaintedMob {
}
