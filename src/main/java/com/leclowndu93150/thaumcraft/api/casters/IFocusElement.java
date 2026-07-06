package com.leclowndu93150.thaumcraft.api.casters;

import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * A single unit inside a focus package. Every piece of a spell, whether a medium, an effect,
 * a modifier, or a nested package, is a focus element.
 *
 * <p>String keys are replaced by {@link Identifier} registry ids resolved through {@link
 * FocusElementType#REGISTRY_KEY}.
 *
 * @since 1.0.0
 */
public interface IFocusElement {
    /**
     * The registry id of this element. Must match the id under which the element's
     * {@link FocusElementType} is registered.
     *
     * @return the element id, e.g. {@code thaumcraft:root}
     */
    Identifier getKey();

    /**
     * The research required before a player may place this element into a focus. Null means
     * always available.
     *
     * @return the gating research, or null when ungated
     */
    default @Nullable ResearchGate getResearch() {
        return null;
    }

    /**
     * The structural role this element plays inside a package.
     *
     * @return the unit type, never null
     */
    EnumUnitType getType();

    /**
     * The structural roles a focus element can play.
     *
     * @since 1.0.0
     */
    enum EnumUnitType {
        /** Applies a payload to a target, e.g. fire damage. */
        EFFECT,
        /** Delivers the spell along a trajectory, e.g. touch or projectile. */
        MEDIUM,
        /** Alters the elements that follow it, e.g. split or scatter. */
        MOD,
        /** A nested sub-package executed as a unit. */
        PACKAGE
    }
}
