package com.leclowndu93150.thaumaturge.api.capability;

/**
 * The state of one research entry for one player.
 *
 * <p>The states are mutually exclusive and exhaustive. The per-player knowledge store decides
 * which applies. The ordinal is unspecified and the type is never persisted or sent over the
 * network.
 *
 * @since 1.0.0
 */
public enum ResearchStatus {
    /** The player has no record of the entry. */
    UNKNOWN,
    /** The entry is recorded and every stage is finished. */
    COMPLETE,
    /** The entry is recorded and not complete. */
    IN_PROGRESS
}
