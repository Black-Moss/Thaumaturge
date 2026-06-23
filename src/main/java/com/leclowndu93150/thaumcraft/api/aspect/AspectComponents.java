package com.leclowndu93150.thaumcraft.api.aspect;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Localized component factories for aspects. Used by tooltips, GUI labels, and chat output.
 *
 * <p>Translation keys follow the pattern {@code aspect.<namespace>.<tag>} for the display name
 * and {@code aspect.<namespace>.<tag>.desc} for the English description. The namespace is
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
        return Component.translatable(translationKey(aspect, false));
    }

    /**
     * Returns the localized description of the given aspect.
     *
     * @param aspect the aspect, referenced by holder so the registry id is available
     * @return a translatable component bound to {@code aspect.<namespace>.<tag>.desc}
     */
    public static MutableComponent description(Holder<IAspect> aspect) {
        return Component.translatable(translationKey(aspect, true));
    }

    private static String translationKey(Holder<IAspect> holder, boolean description) {
        String namespace = holder.unwrapKey()
                .map(key -> key.identifier().getNamespace())
                .orElse("thaumcraft");
        String tag = holder.value().tag();
        return description
                ? "aspect." + namespace + "." + tag + ".desc"
                : "aspect." + namespace + "." + tag;
    }
}
