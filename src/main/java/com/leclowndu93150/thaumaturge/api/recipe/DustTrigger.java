package com.leclowndu93150.thaumaturge.api.recipe;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * A transformation Salis Mundus performs when used on a block, either a single-block conversion or a
 * multiblock construction.
 *
 * <p>Implementations are recipes of the recipe type {@code thaumaturge:dust_trigger}. All methods run on the
 * logical server thread. Implementations hold no state across uses and never cache placements.
 *
 * @since 1.0.0
 */
public interface DustTrigger extends Recipe<DustTriggerInput>, ResearchGated {
    /**
     * Marks the recipe as special so recipe book and recipe display machinery never offer it as a craftable
     * recipe.
     *
     * @return {@code true} by default; overrides must keep that default
     * @since 1.0.0
     */
    @Override
    default boolean isSpecial() {
        return true;
    }

    /**
     * Lists the block positions that receive the sparkle effect when the trigger fires.
     *
     * <p>Called server-side only, before queued changes land. Order is not significant and duplicates produce
     * the effect twice. A {@code null} or empty answer emits nothing.
     *
     * @param level the level of the clicked block
     * @param player the player using the dust
     * @param clicked the clicked position, not the multiblock origin
     * @param placement the matched placement; {@link DustTriggerPlacement#origin()} for non-multiblock triggers
     * @return the positions to sparkle; by default a single-element list holding only {@code clicked}
     * @since 1.0.0
     */
    default List<BlockPos> sparkle(Level level, Player player, BlockPos clicked, DustTriggerPlacement placement) {
        return List.of(clicked);
    }

    /**
     * Finds the placement at which this trigger matches around the clicked block.
     *
     * <p>Free of side effects and repeatable; it depends only on the input and the world.
     *
     * @param input the dust stack, level, clicked position and clicked state
     * @return the matching placement, or {@code null} when none exists or the trigger is not a multiblock
     * @since 1.0.0
     */
    @Nullable
    default DustTriggerPlacement findPlacement(DustTriggerInput input) {
        return null;
    }

    /**
     * Classifies the trigger; constant for the lifetime of the recipe.
     *
     * @return {@code true} when the trigger performs every world change itself through
     *         {@link #execute}; {@code false} by default, where the item schedules the single-block swap
     * @since 1.0.0
     */
    default boolean isMultiblock() {
        return false;
    }

    /**
     * Applies or schedules the world changes of this trigger.
     *
     * <p>Runs on the logical server only, after the match, the research gate and the non-empty result check have
     * passed and one dust has been consumed unless the player is in creative mode. Implementations return
     * promptly and defer long-running changes through the {@code DustTriggerSwapQueue} attachment.
     *
     * @param input the dust stack, level, clicked position and clicked state
     * @param player the player using the dust
     * @param placement {@code null} for non-multiblock triggers, otherwise the non-null result of
     *        {@link #findPlacement}
     * @param face the face of the clicked block the dust was used on
     * @since 1.0.0
     */
    void execute(DustTriggerInput input, Player player, @Nullable DustTriggerPlacement placement, Direction face);
}
