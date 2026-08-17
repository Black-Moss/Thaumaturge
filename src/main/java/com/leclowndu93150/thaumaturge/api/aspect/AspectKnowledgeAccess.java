package com.leclowndu93150.thaumaturge.api.aspect;

import java.util.function.Function;
import net.minecraft.core.Holder;

/**
 * Static facade resolving how much the viewing player understands about an aspect.
 *
 * <p>
 * This is the single gate every display surface passes through. {@link AspectComponents#name} and
 * {@link AspectComponents#description} consult it themselves, so a new tooltip, screen, or compat integration masks
 * undiscovered aspects without doing anything, and revealing one is an explicit call to
 * {@link AspectComponents#trueName}.
 *
 * <p>
 * The resolver is player-scoped and therefore client-only. It is left unbound on the dedicated server, where
 * {@link #of} reports {@link AspectKnowledge#KNOWN} for everything, so server-built messages and datagen keep real
 * names.
 *
 * <p>
 * The implementation is bound once at client init by Thaumaturge via {@link #bind(Function)}; addons must not call
 * {@code bind}.
 *
 * @since 1.0.0
 */
public final class AspectKnowledgeAccess {
    private static Function<Holder<IAspect>, AspectKnowledge> resolver = aspect -> AspectKnowledge.KNOWN;

    private AspectKnowledgeAccess() {}

    /**
     * Binds the resolver backing this facade.
     *
     * @param binding resolves the viewing player's knowledge of an aspect; must not be null
     */
    public static void bind(Function<Holder<IAspect>, AspectKnowledge> binding) {
        resolver = binding;
    }

    /**
     * Returns how much the viewing player understands about the given aspect.
     *
     * @param aspect the aspect to test
     * @return the knowledge tier; {@link AspectKnowledge#KNOWN} while unbound
     */
    public static AspectKnowledge of(Holder<IAspect> aspect) {
        return resolver.apply(aspect);
    }

    /**
     * Returns whether the viewing player has discovered the given aspect.
     *
     * @param aspect the aspect to test
     * @return {@code true} when the aspect's name may be shown
     */
    public static boolean isKnown(Holder<IAspect> aspect) {
        return of(aspect).isKnown();
    }
}
