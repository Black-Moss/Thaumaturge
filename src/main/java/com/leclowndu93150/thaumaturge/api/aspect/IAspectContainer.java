package com.leclowndu93150.thaumaturge.api.aspect;

import net.minecraft.core.Holder;

/**
 * Describes a block or device that keeps a store of aspects: jars, nodes, alembics, mirrors, the
 * crucible and the infusion matrix.
 *
 * <p>The sided block capability {@code thaumaturge:aspect_container} hands out instances of this
 * type. A capability instance can become invalid at any time, so a caller queries it again rather
 * than holding it between ticks. State changes belong on the logical server thread. The client
 * side may inspect the contents through {@link #getAspects()} and {@link #amountOf(Holder)} but
 * never calls {@link #fill(Holder, int)}, {@link #drain(Holder, int)} or
 * {@link #setAspects(AspectList)}.
 *
 * <p>This interface does no synchronisation itself. Each implementation marks its block entity
 * dirty and notifies clients whenever a change becomes visible to them.
 *
 * @since 1.0
 */
public interface IAspectContainer {

    /**
     * Reports whether the container is able to hold the aspect at all, regardless of how full it
     * currently is.
     *
     * @param aspect the aspect being asked about
     * @return {@code true} if the aspect is storable here
     */
    boolean accepts(Holder<IAspect> aspect);

    /**
     * Gives a snapshot of everything currently stored.
     *
     * @return the contents, never {@code null}; the returned list is not a handle for editing the
     *         container
     */
    AspectList getAspects();

    /**
     * Overwrites the whole store with the supplied contents.
     *
     * <p>Persistence and client synchronisation are the responsibility of the implementation.
     *
     * @param aspects the replacement contents, never {@code null}
     */
    void setAspects(AspectList aspects);

    /**
     * Looks up how much of one aspect is stored.
     *
     * @param aspect the aspect to query
     * @return the stored quantity, zero or greater
     */
    default int amountOf(Holder<IAspect> aspect) {
        return getAspects().amountOf(aspect);
    }

    /**
     * Checks that the store holds a minimum quantity of one aspect.
     *
     * @param aspect the aspect to query
     * @param amount the minimum quantity required
     * @return {@code true} if {@link #amountOf(Holder)} reaches {@code amount}; unless overridden,
     *         a requirement of zero or less is always met
     */
    default boolean holds(Holder<IAspect> aspect, int amount) {
        return amountOf(aspect) >= amount;
    }

    /**
     * Inserts up to the offered quantity, keeping whatever capacity allows.
     *
     * @param aspect the aspect being inserted
     * @param amount the quantity on offer
     * @return the leftover that could not be stored; zero when all of it fit, equal to
     *         {@code amount} when none of it fit, and always between zero and {@code amount}
     */
    int fill(Holder<IAspect> aspect, int amount);

    /**
     * Extracts a quantity only if the complete quantity is available.
     *
     * @param aspect the aspect being extracted
     * @param amount the quantity to extract
     * @return {@code true} when the whole quantity was present and has been taken out; when
     *         {@code false} the contents are left unchanged
     */
    boolean drain(Holder<IAspect> aspect, int amount);
}
