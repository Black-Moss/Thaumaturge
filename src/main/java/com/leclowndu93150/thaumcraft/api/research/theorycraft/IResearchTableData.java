package com.leclowndu93150.thaumcraft.api.research.theorycraft;

import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

/**
 * Per-player state for an active research table session.
 *
 * <p>{@link TheorycraftCard} implementations read and mutate this object inside
 * {@link TheorycraftCard#initialize initialize} and {@link TheorycraftCard#activate activate}.
 * Mutating methods are server-authoritative; calling them on the client mirror has no effect.
 *
 * @since 1.0.0
 */
public interface IResearchTableData {
    /**
     * The amount of inspiration remaining in this session. Cards may not be played when their
     * {@link TheorycraftCard#inspirationCost() cost} exceeds this value.
     *
     * @return the remaining inspiration, non-negative
     */
    int inspiration();

    /**
     * The maximum inspiration the player started this session with. Used by cards that restore
     * inspiration so they do not exceed the cap.
     *
     * @return the starting inspiration, positive
     */
    int inspirationStart();

    /**
     * Number of points currently accumulated for the given category.
     *
     * @param category the research category key
     * @return the accumulated total, non-negative
     */
    int categoryTotal(ResourceKey<IResearchCategory> category);

    /**
     * Adds {@code amount} points to the given category. Negative amounts subtract; the total
     * clamps at zero.
     *
     * @param category the research category key
     * @param amount the amount to add, may be negative
     */
    void addCategoryTotal(ResourceKey<IResearchCategory> category, int amount);

    /**
     * Adds {@code amount} to the player's remaining inspiration. Clamped to {@link #inspirationStart()}.
     *
     * @param amount the inspiration to add, may be negative
     */
    void addInspiration(int amount);

    /**
     * Categories currently unlocked for this player and not blocked in this session. Cards that
     * declare a {@link TheorycraftCard#category() category} are only drawn when that category is
     * in this list; cards that pick a random category use this list as their source.
     *
     * @param player the table operator
     * @return the available categories, never null
     */
    List<ResourceKey<IResearchCategory>> availableCategories(Player player);

    /**
     * Whether the given category has been blocked from further draws this session.
     *
     * @param category the research category key
     * @return {@code true} when blocked
     */
    boolean isCategoryBlocked(ResourceKey<IResearchCategory> category);

    /**
     * Blocks the given category from further draws this session.
     *
     * @param category the research category key
     */
    void blockCategory(ResourceKey<IResearchCategory> category);

    /**
     * Adds {@code amount} bonus card draws to this session.
     *
     * @param amount the bonus draws to add, may be negative
     */
    void addBonusDraws(int amount);

    /**
     * Set of category keys that currently carry a non-zero total in this session. Order matches
     * the iteration order used by cards that walk the totals (insertion order, deterministic).
     *
     * @return the keys, never null
     */
    Set<ResourceKey<IResearchCategory>> categoriesWithTotal();

    /**
     * Sum of all category totals across categories that have not been blocked. Cards such as
     * {@code Balance} and {@code Rethink} gate themselves on this value.
     *
     * @return the unblocked total, non-negative
     */
    int totalUnblockedPoints();

    /**
     * Sets the total for the given category outright, replacing any previous value. A non-positive
     * {@code amount} clears the category.
     *
     * @param category the research category key
     * @param amount the new total
     */
    void setCategoryTotal(ResourceKey<IResearchCategory> category, int amount);

    /**
     * Increments the running penalty counter. Each penalty deducts an inspiration unit on the
     * next round; cards that warp the session (e.g. {@code Balance}) call this.
     */
    void incrementPenalty();
}
