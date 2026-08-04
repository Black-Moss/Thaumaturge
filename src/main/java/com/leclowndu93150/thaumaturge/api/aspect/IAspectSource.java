package com.leclowndu93150.thaumaturge.api.aspect;

/**
 *
 * @since 1.0.0
 */
public interface IAspectSource extends IAspectContainer {

    /**
     * @return if the current aspect container is blocked
     */
    boolean isBlocked();
}
