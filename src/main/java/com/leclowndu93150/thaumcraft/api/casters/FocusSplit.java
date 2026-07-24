package com.leclowndu93150.thaumcraft.api.casters;

/**
 * An element that forks the spell into independent branches. The branches live in the
 * unit's {@link FocusUnit#branches()}; the engine executes each in order, feeding it the
 * streams this element passes through.
 *
 * @since 1.0.0
 */
public non-sealed interface FocusSplit extends FocusElement {
    /**
     * The streams handed to every branch.
     *
     * @param ctx      the execution scope
     * @param settings the configured settings
     * @param incoming the streams supplied by the previous node
     * @return the branch input streams, never null
     */
    CastStreams branchStreams(CastContext ctx, FocusSettings settings, CastStreams incoming);

    @Override
    default float powerMultiplier(FocusSettings settings) {
        return 0.75F;
    }
}
