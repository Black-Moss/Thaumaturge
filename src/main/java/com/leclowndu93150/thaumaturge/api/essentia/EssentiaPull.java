package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.Holder;
import org.jspecify.annotations.Nullable;

/**
 * How hard one side of an {@link IEssentiaConduit} draws essentia toward itself, and which aspect
 * the draw is for.
 *
 * <p>Essentia only ever moves from a weaker pull to a stronger one. A pull that names no aspect
 * accepts any aspect. A pull of zero strength draws nothing and never names an aspect: the
 * canonical constructor turns any non-positive strength into {@link #NONE}'s values.
 *
 * @param aspect   the aspect the pull is for, or {@code null} when any aspect is accepted
 * @param strength the pull strength, never negative
 * @since 1.1.0
 */
public record EssentiaPull(@Nullable Holder<IAspect> aspect, int strength) {
    /**
     * The absent pull: no aspect, zero strength.
     *
     * @since 1.1.0
     */
    public static final EssentiaPull NONE = new EssentiaPull(null, 0);

    /**
     * Normalises the pull so that a non-positive strength carries no aspect and reads as zero.
     *
     * @since 1.1.0
     */
    public EssentiaPull {
        if (strength <= 0) {
            strength = 0;
            aspect = null;
        }
    }

    /**
     * Returns a pull for the given aspect and strength, reusing {@link #NONE} when the strength is
     * not positive.
     *
     * @param aspect   the wanted aspect, or {@code null} for any aspect
     * @param strength the pull strength; zero or negative means no pull
     * @return the pull, never {@code null}
     * @since 1.1.0
     */
    public static EssentiaPull of(@Nullable Holder<IAspect> aspect, int strength) {
        return strength <= 0 ? NONE : new EssentiaPull(aspect, strength);
    }

    /**
     * Returns a pull of the given strength that accepts any aspect.
     *
     * @param strength the pull strength; zero or negative means no pull
     * @return the pull, never {@code null}
     * @since 1.1.0
     */
    public static EssentiaPull any(int strength) {
        return of(null, strength);
    }

    /**
     * Reports whether this pull draws anything at all.
     *
     * @return {@code true} when the strength is above zero
     * @since 1.1.0
     */
    public boolean isActive() {
        return strength > 0;
    }

    /**
     * Reports whether this pull would draw the given aspect. An inactive pull accepts nothing.
     *
     * @param candidate the aspect on offer
     * @return {@code true} when the pull is active and names no aspect or names {@code candidate}
     * @since 1.1.0
     */
    public boolean accepts(Holder<IAspect> candidate) {
        return strength > 0 && (aspect == null || aspect.equals(candidate));
    }
}
