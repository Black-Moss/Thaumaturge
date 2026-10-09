package com.leclowndu93150.thaumaturge.api.wands;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * Immutable description of a wand cap type. A cap sets the vis cost multiplier applied per aspect
 * when a wand spends vis, the cost factor used to price assembly recipes and the model texture.
 *
 * <p>Instances carry no identifier. The id is the key under which the instance is registered in the
 * registry identified by {@link #REGISTRY_KEY}. Equality is identity. All state is final, so
 * instances are safe to read from any thread.
 *
 * @since 1.0.0
 */
public final class WandCap {
    /**
     * Key of the cap registry. The registry is not synchronised to clients, so both sides must hold
     * identical content.
     *
     * @since 1.0.0
     */
    public static final ResourceKey<Registry<WandCap>> REGISTRY_KEY = ResourceKey.createRegistryKey(TTIds.rl("wand_cap"));

    private final float baseCostModifier;
    private final List<ResourceKey<IAspect>> specialCostAspects;
    private final float specialCostModifier;
    private final Map<ResourceKey<IAspect>, Float> aspectCostModifiers;
    private final int craftCost;
    private final Identifier texture;

    /**
     * Creates a cap with one multiplier for a list of special aspects and another for the rest.
     *
     * @param baseCostModifier    multiplier applied to every aspect not in the special list
     * @param specialCostAspects  aspects that use the special multiplier; copied, empty means none
     * @param specialCostModifier multiplier for the special aspects; ignored when the list is empty
     * @param craftCost           cost factor used to price assembly recipes
     * @param texture             model texture identifier
     * @since 1.0.0
     */
    public WandCap(float baseCostModifier, List<ResourceKey<IAspect>> specialCostAspects, float specialCostModifier, int craftCost, Identifier texture) {
        this.baseCostModifier = baseCostModifier;
        this.specialCostAspects = List.copyOf(specialCostAspects);
        this.specialCostModifier = specialCostModifier;
        this.aspectCostModifiers = Map.of();
        this.craftCost = craftCost;
        this.texture = texture;
    }

    /**
     * Creates a cap with an explicit multiplier per aspect.
     *
     * @param baseCostModifier    multiplier applied to every aspect missing from the map
     * @param aspectCostModifiers per-aspect multipliers; copied
     * @param craftCost           cost factor used to price assembly recipes
     * @param texture             model texture identifier
     * @since 1.0.0
     */
    public WandCap(float baseCostModifier, Map<ResourceKey<IAspect>, Float> aspectCostModifiers, int craftCost, Identifier texture) {
        this.baseCostModifier = baseCostModifier;
        this.specialCostAspects = List.of();
        this.specialCostModifier = baseCostModifier;
        this.aspectCostModifiers = Map.copyOf(aspectCostModifiers);
        this.craftCost = craftCost;
        this.texture = texture;
    }

    /**
     * Returns the generic multiplier used when no aspect is given.
     *
     * @return the base multiplier, also for caps built with the per-aspect map
     * @since 1.0.0
     */
    public float baseCostModifier() {
        return baseCostModifier;
    }

    /**
     * Returns the aspects that use the special multiplier.
     *
     * @return an immutable list, never null, empty when no special aspects exist
     * @since 1.0.0
     */
    public List<ResourceKey<IAspect>> specialCostAspects() {
        return specialCostAspects;
    }

    /**
     * Returns the special multiplier. Callers ignore the value when {@link #specialCostAspects()} is empty.
     *
     * @return the special multiplier, or the base multiplier for caps built with the per-aspect map
     * @since 1.0.0
     */
    public float specialCostModifier() {
        return specialCostModifier;
    }

    /**
     * Resolves the multiplier for one aspect. The per-aspect map wins, then the special list, then the base value.
     *
     * @param aspect the aspect key, matched by equality; any key including unknown or null ones is accepted
     * @return the cost multiplier, never throws
     * @since 1.0.0
     */
    public float costModifier(ResourceKey<IAspect> aspect) {
        if (aspect == null) {
            return baseCostModifier;
        }
        Float mapped = aspectCostModifiers.get(aspect);
        if (mapped != null) {
            return mapped;
        }
        if (specialCostAspects.contains(aspect)) {
            return specialCostModifier;
        }
        return baseCostModifier;
    }

    /**
     * Returns the cost factor that is multiplied with the rod factor to price assembly recipes.
     *
     * @return the craft cost factor
     * @since 1.0.0
     */
    public int craftCost() {
        return craftCost;
    }

    /**
     * Returns the model texture.
     *
     * @return the texture identifier, never null
     * @since 1.0.0
     */
    public Identifier texture() {
        return texture;
    }
}
