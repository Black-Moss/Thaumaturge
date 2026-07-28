package com.leclowndu93150.thaumaturge.api.essentia;

/**
 * Implemented by jar blocks to declare how much essentia one jar of that kind holds.
 *
 * <p>The capacity is a property of the block rather than the block entity, so every jar of a
 * given kind shares it. Renderers derive the fill level from it, so a jar reports a full
 * body at {@link #jarCapacity()} regardless of the value chosen.
 *
 * @since 1.0.0
 */
public interface IEssentiaJar {
    /** The capacity of a standard jar. */
    int DEFAULT_CAPACITY = 250;

    /**
     * The amount of a single aspect this jar kind can hold.
     *
     * @return the capacity in essentia units, greater than zero
     */
    default int jarCapacity() {
        return DEFAULT_CAPACITY;
    }
}
