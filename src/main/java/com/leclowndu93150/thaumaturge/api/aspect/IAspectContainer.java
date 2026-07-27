package com.leclowndu93150.thaumaturge.api.aspect;

import net.minecraft.core.Holder;

/**
 * Sided aspect containers contract for blocks that can store aspects.
 *
 * <p>Aspects are referenced as {@code Holder<IAspect>} so that implementations remain valid across
 * datapack reloads.
 *
 * <p>Implementations expose the capability through {@link AspectCapabilities#CONTAINER}.
 *
 * @since 1.0.0
 */
public interface IAspectContainer {

    /**
     * The aspects currently stored in this container.
     *
     * @return the current aspect list, never {@code null}
     */
    AspectList getAspects();

    /**
     * Replaces the contents of this container.
     *
     * @param aspects the new aspects, never {@code null}
     */
    void setAspects(AspectList aspects);

    /**
     * Whether the given aspect can be stored in this container.
     *
     * @param aspect the aspect queried, never {@code null}
     * @return {@code true} when the aspect is accepted
     */
    boolean doesContainerAccept(Holder<IAspect> aspect);

    /**
     * Adds up to {@code amount} of {@code aspect} to this container.
     *
     * @param aspect the aspect supplied
     * @param amount the maximum amount to add
     * @return the amount that was not added, or {@code 0} when the full amount was accepted
     */
    int addToContainer(Holder<IAspect> aspect, int amount);

    /**
     * Removes up to {@code amount} of {@code aspect} from this container.
     *
     * @param aspect the aspect requested
     * @param amount the maximum amount to remove
     * @return {@code true} when the requested amount was removed
     */
    boolean takeFromContainer(Holder<IAspect> aspect, int amount);

    /**
     * Whether this container holds at least {@code amount} of {@code aspect}.
     *
     * @param aspect the aspect queried
     * @param amount the minimum amount required
     * @return {@code true} when the container contains the requested amount
     */
    boolean doesContainerContainAmount(Holder<IAspect> aspect, int amount);

    /**
     * The amount of {@code aspect} currently stored in this container.
     *
     * @param aspect the aspect queried
     * @return the stored amount, never negative
     */
    int containerContains(Holder<IAspect> aspect);
}
