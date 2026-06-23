package com.leclowndu93150.thaumcraft.api.research;

import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * A research category. Categories partition the Thaumonomicon into thematic tabs and
 * supply the formula used to convert scanned aspects into category progress.
 *
 * <p>Categories are loaded from the {@link #REGISTRY_KEY} datapack registry under
 * {@code data/<namespace>/thaumcraft/research_category/}. Code typically resolves a category by
 * {@link ResourceKey} and looks up the holder through a registry access.
 *
 * @since 1.0.0
 */
public interface IResearchCategory {
    /** Datapack registry key for research categories. */
    ResourceKey<Registry<IResearchCategory>> REGISTRY_KEY = ResourceKey.createRegistryKey(
            Identifier.fromNamespaceAndPath("thaumcraft", "research_category"));

    /**
     * Identifier of the research entry that gates access to this category in the Thaumonomicon.
     * When absent the category is unconditionally visible.
     *
     * @return the gating research entry identifier, or empty
     */
    Optional<Identifier> requiredResearch();

    /**
     * Aspect-weighted formula used to convert scanned aspect amounts into raw category knowledge.
     *
     * @return the formula, never null
     */
    AspectList formula();

    /**
     * Identifier of the icon sprite for this category's tab.
     *
     * @return the texture identifier
     */
    Identifier icon();

    /**
     * Identifier of the background sprite drawn behind the category's entry grid.
     *
     * @return the texture identifier
     */
    Identifier background();

    /**
     * Identifier of an optional second background sprite drawn over the primary background.
     *
     * @return the texture identifier, or empty when the category uses a single layer
     */
    Optional<Identifier> overlayBackground();
}
