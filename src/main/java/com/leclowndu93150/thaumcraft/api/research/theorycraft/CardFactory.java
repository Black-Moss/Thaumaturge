package com.leclowndu93150.thaumcraft.api.research.theorycraft;

/**
 * Factory that produces fresh {@link TheorycraftCard} instances. Each entry in the
 * {@code thaumcraft:theorycraft_card} registry is a factory; the manager invokes the factory
 * once per draw to obtain an isolated stateful instance for the active player.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface CardFactory {
    /**
     * Creates a fresh card instance. The returned card has no seed assigned; the manager calls
     * {@link TheorycraftCard#setSeed setSeed} before {@link TheorycraftCard#initialize initialize}.
     *
     * @return a new card instance, never null
     */
    TheorycraftCard create();
}
