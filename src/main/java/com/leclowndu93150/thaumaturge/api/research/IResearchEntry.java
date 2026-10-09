package com.leclowndu93150.thaumaturge.api.research;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * One research node of the Thaumonomicon.
 *
 * <p>Implementations are immutable and safe to read from any thread. Callers compare entries by
 * registry holder or registry key, not by value. Stage access is by position and the stage list is
 * never empty.
 *
 * @since 1.0.0
 */
public interface IResearchEntry {
    /** The datapack registry key for entries. */
    ResourceKey<Registry<IResearchEntry>> REGISTRY_KEY = ResourceKey.createRegistryKey(TTIds.rl("research_entry"));

    /**
     * The category owning this entry.
     *
     * @return the resolved category holder
     */
    Holder<IResearchCategory> category();

    /**
     * The translation key of the entry name.
     *
     * @return the name key
     */
    String nameKey();

    /**
     * The references that must be satisfied before the entry becomes available.
     *
     * @return the parent references in insertion order
     */
    Set<ResearchParent> parents();

    /**
     * The decorative connector targets and cascade-completion entries, possibly in other categories.
     *
     * @return the sibling entry identifiers in insertion order
     */
    Set<Identifier> siblings();

    /**
     * The grid column.
     *
     * @return the column, any sign
     */
    int column();

    /**
     * The grid row.
     *
     * @return the row, any sign
     */
    int row();

    /**
     * The stages of the entry, in completion order.
     *
     * @return the stages, never empty
     */
    List<IResearchStage> stages();

    /**
     * The display and unlock flags.
     *
     * @return the flags in enum order
     */
    Set<ResearchEntryMeta> meta();

    /**
     * Tests whether a flag is set.
     *
     * @param flag the flag to test
     * @return {@code true} when {@link #meta()} contains the flag
     */
    default boolean hasMeta(ResearchEntryMeta flag) {
        return meta().contains(flag);
    }

    /**
     * The icons cycled over time by the browser.
     *
     * @return the icons, empty by default; an empty list falls back to a stage-derived icon
     */
    default List<ResearchIcon> icons() {
        return List.of();
    }

    /**
     * The extra pages unlocked on completion.
     *
     * @return the addenda in page order, empty by default
     */
    default List<ResearchAddendum> addenda() {
        return List.of();
    }

    /**
     * The hand-authored aspect cost seeding research-note puzzles.
     *
     * @return the note aspects, the empty list by default
     */
    default AspectList noteAspects() {
        return AspectList.EMPTY;
    }

    /**
     * The puzzle complexity, driving the hex grid radius and the number of holes.
     *
     * @return the complexity from 1 to 3, 1 by default
     */
    default int complexity() {
        final int defaultComplexity = 1;
        return defaultComplexity;
    }
}
