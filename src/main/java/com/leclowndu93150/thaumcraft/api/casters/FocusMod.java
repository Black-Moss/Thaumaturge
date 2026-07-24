package com.leclowndu93150.thaumcraft.api.casters;

/**
 * An element that transforms the streams flowing to the nodes after it, such as scattering
 * one trajectory into a cone of many.
 *
 * @since 1.0.0
 */
public non-sealed interface FocusMod extends FocusElement {
    /**
     * Transforms the incoming streams. The result replaces both streams entirely; a null
     * array in the result clears that stream for the following nodes.
     *
     * @param ctx      the execution scope
     * @param settings the configured settings
     * @param incoming the streams supplied by the previous node
     * @return the streams for the next node, never null
     */
    CastStreams modify(CastContext ctx, FocusSettings settings, CastStreams incoming);
}
