package com.leclowndu93150.thaumcraft.api.research;

import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * A research entry. Entries are the nodes shown in a Thaumonomicon category; each entry contains
 * one or more {@link IResearchStage stages} that the player completes in order.
 *
 * <p>Entries are loaded from the {@link #REGISTRY_KEY} datapack registry under
 * {@code data/<namespace>/thaumcraft/research_entry/}.
 *
 * @since 1.0.0
 */
public interface IResearchEntry {
    /** Datapack registry key for research entries. */
    ResourceKey<Registry<IResearchEntry>> REGISTRY_KEY = ResourceKey.createRegistryKey(
            Identifier.fromNamespaceAndPath("thaumcraft", "research_entry"));

    /**
     * Category this entry belongs to.
     *
     * @return the owning category holder
     */
    Holder<IResearchCategory> category();

    /**
     * Translation key used to display this entry's name.
     *
     * @return the translation key, e.g. {@code research.thaumcraft.unlocking_secrets.title}
     */
    String nameKey();

    /**
     * Parent entries that must be complete before this entry becomes available. A parent may be
     * referenced from another category.
     *
     * @return the parent identifiers, never null; may be empty
     */
    Set<Identifier> parents();

    /**
     * Sibling entries displayed in the Thaumonomicon as decorative connectors next to this entry,
     * with no progression effect.
     *
     * @return the sibling identifiers, never null
     */
    Set<Identifier> siblings();

    /**
     * Column position in the Thaumonomicon grid.
     *
     * @return the column
     */
    int column();

    /**
     * Row position in the Thaumonomicon grid.
     *
     * @return the row
     */
    int row();

    /**
     * Stages of this entry in order. The player advances through them as research progresses.
     *
     * @return the stages; never empty
     */
    List<IResearchStage> stages();

    /**
     * Meta flags that affect display and progression behaviour.
     *
     * @return the meta flag set
     */
    Set<ResearchEntryMeta> meta();

    /**
     * Whether this entry has the given meta flag.
     *
     * @param flag the flag to test
     * @return {@code true} when set
     */
    default boolean hasMeta(ResearchEntryMeta flag) {
        return meta().contains(flag);
    }
}
