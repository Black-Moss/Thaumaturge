package com.leclowndu93150.thaumaturge.api.aspect;

/**
 * How much a player understands about an aspect.
 *
 * <p>The tier drives every player-facing rendering of an aspect: its name, description, chip
 * sprite, and whether its composition is listed at all. Resolve it through
 * {@link AspectKnowledgeAccess#of} rather than testing discovery directly, so that new display
 * surfaces inherit masking instead of opting into it.
 *
 * @since 1.0.0
 */
public enum AspectKnowledge {
    /**
     * The aspect has not been discovered and at least one of its components is itself unknown.
     * Nothing is revealed beyond its colour.
     */
    UNKNOWN,
    /**
     * The aspect has not been discovered, but every component is known, so the player can derive
     * it by combining those components at a research table. Its composition is revealed, its name
     * is not.
     *
     * @apiNote this is the same condition scanning uses to decide whether an object may be
     * studied, so an object is scannable exactly when none of its aspects are {@link #UNKNOWN}.
     */
    DEDUCIBLE,
    /**
     * The aspect has been discovered. Everything about it is revealed.
     */
    KNOWN;

    /**
     * Returns whether the aspect's name and description may be shown.
     *
     * @return {@code true} only for {@link #KNOWN}
     */
    public boolean isKnown() {
        return this == KNOWN;
    }

    /**
     * Returns whether the aspect's composition may be shown.
     *
     * @return {@code true} for {@link #DEDUCIBLE} and {@link #KNOWN}
     */
    public boolean isCompositionRevealed() {
        return this != UNKNOWN;
    }
}
