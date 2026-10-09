package com.leclowndu93150.thaumaturge.api.research;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * A Thaumonomicon category (tab) holding research entries.
 *
 * <p>Implementations are immutable and safe to read from any thread. Callers compare categories
 * by registry holder or registry key, not by value.
 *
 * @since 1.0.0
 */
public interface IResearchCategory {
    /** The datapack registry key for categories. */
    ResourceKey<Registry<IResearchCategory>> REGISTRY_KEY = ResourceKey.createRegistryKey(TTIds.rl("research_category"));

    /** The modifier applied by {@link #applyFormula(AspectList)}. */
    double PLAIN_MODIFIER = 1.0;

    /** The divisor applied to each formula weight. */
    double WEIGHT_DIVISOR = 10.0;

    /**
     * The research that must be complete before the category is open.
     *
     * @return the gating research identifier, or empty when the category is always open
     */
    Optional<Identifier> requiredResearch();

    /**
     * The weighted aspects converting a scanned composition into category knowledge.
     *
     * @return the formula, never null; an empty list when nothing is weighed
     */
    AspectList formula();

    /**
     * Converts a scanned aspect composition into whole category knowledge points with a modifier of 1.0.
     *
     * @param composition the scanned aspects
     * @return the knowledge points, zero or higher
     * @apiNote not meant to be overridden; equals {@code applyFormula(composition, 1.0)}
     */
    default int applyFormula(AspectList composition) {
        return applyFormula(composition, PLAIN_MODIFIER);
    }

    /**
     * Converts a scanned aspect composition into whole category knowledge points.
     *
     * <p>The result is the ceiling of the square root of the sum, over every formula aspect, of the
     * composition amount times the formula weight divided by 10 times the squared modifier. The
     * calculation has no side effects and may be called from any side and thread.
     *
     * @param composition the scanned aspects
     * @param modifier the dimensionless multiplier; a negative value acts as its absolute value
     * @return the knowledge points, zero when the sum is zero
     * @apiNote not meant to be overridden
     */
    default int applyFormula(AspectList composition, double modifier) {
        final List<AspectInstance> weights = formula().entries();
        double total = 0.0;
        for (int i = 0; i < weights.size(); i++) {
            total += formulaTerm(composition, weights.get(i), modifier);
        }
        if (total <= 0.0) {
            return 0;
        }
        return (int) Math.ceil(Math.sqrt(total));
    }

    private static double formulaTerm(AspectList composition, AspectInstance weight, double modifier) {
        final double scaledWeight = weight.amount() / WEIGHT_DIVISOR;
        return composition.amountOf(weight.aspect()) * scaledWeight * modifier * modifier;
    }

    /**
     * The texture of the category tab icon.
     *
     * @return the icon texture identifier
     */
    Identifier icon();

    /**
     * The texture drawn over the whole grid area, scrolling at half the pan offset.
     *
     * @return the background texture identifier
     */
    Identifier background();

    /**
     * The optional texture drawn over the background, scrolling at two thirds of the pan offset.
     *
     * @return the overlay texture identifier, or empty when there is none
     */
    Optional<Identifier> overlayBackground();

    /**
     * The sort index among categories of the same namespace, ascending.
     *
     * @return the index, zero or higher
     */
    int index();
}
