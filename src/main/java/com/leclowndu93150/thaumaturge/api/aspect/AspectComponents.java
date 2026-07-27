package com.leclowndu93150.thaumaturge.api.aspect;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Localized component factories for aspects. Used by tooltips, GUI labels, and chat output.
 *
 * <p>Translation keys follow the pattern {@code aspect.<namespace>.<tag>} for the display name,
 * {@code aspect.<namespace>.<tag>.desc} for the English description, and
 * {@code aspect.<namespace>.<tag>.help} for the study hint. The namespace is
 * derived from the aspect's registry id, so addon aspects resolve against the addon's lang
 * files without further configuration.
 *
 * @since 1.0.0
 */
public final class AspectComponents {
    private AspectComponents() {}

    /**
     * Returns the localized display name of the given aspect.
     *
     * @param aspect the aspect, referenced by holder so the registry id is available
     * @return a translatable component bound to {@code aspect.<namespace>.<tag>}
     */
    public static MutableComponent name(Holder<IAspect> aspect) {
        return Component.translatable(translationKey(aspect, ""));
    }

    /**
     * Returns the localized description of the given aspect.
     *
     * @param aspect the aspect, referenced by holder so the registry id is available
     * @return a translatable component bound to {@code aspect.<namespace>.<tag>.desc}
     */
    public static MutableComponent description(Holder<IAspect> aspect) {
        return Component.translatable(translationKey(aspect, ".desc"));
    }

    /**
     * Returns the localized study hint of the given aspect, a short phrase naming the kind of
     * thing a player should examine to discover it.
     *
     * @param aspect the aspect, referenced by holder so the registry id is available
     * @return a translatable component bound to {@code aspect.<namespace>.<tag>.help}
     */
    public static MutableComponent help(Holder<IAspect> aspect) {
        return Component.translatable(translationKey(aspect, ".help"));
    }

    private static String translationKey(Holder<IAspect> holder, String suffix) {
        String namespace = holder.unwrapKey()
                .map(key -> key.identifier().getNamespace())
                .orElse("thaumaturge");
        return "aspect." + namespace + "." + holder.value().tag() + suffix;
    }
}
