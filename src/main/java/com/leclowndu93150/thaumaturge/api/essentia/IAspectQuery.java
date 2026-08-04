package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;

/**
 * Side-agnostic synthetic aspect view exposed by tubes and filters whose aspect content is
 * a routing intent rather than physical storage.
 *
 * <p>The full container contract was inappropriate for tubes since they neither store nor accept
 * essentia by aspect; only the read-only side has a useful meaning. Tooltips, scanners, and JEI
 * consume this surface to display, for example, that a filter tube wants vitium.
 *
 * <p>Exposed through {@link EssentiaCapabilities#ASPECT_QUERY}.
 *
 * @since 1.0.0
 */
public interface IAspectQuery {
    /**
     * The aspect intent advertised by this block, or {@link AspectList#EMPTY} when none.
     *
     * <p>The amount carried by each entry signals an intent rather than a quantity, and
     * {@link AspectList} forbids non-positive amounts, so every entry is reported as {@code 1}.
     * Consumers must treat amounts on this surface as untyped markers and never as a storage
     * quantity.
     *
     * @return the advertised aspects, never {@code null}
     */
    AspectList queryAspects();
}
