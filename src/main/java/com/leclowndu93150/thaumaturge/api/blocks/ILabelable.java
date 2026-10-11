package com.leclowndu93150.thaumaturge.api.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Marker for blocks and block entities that accept the label item.
 *
 * <p>The label item walks the clicked block then the block entity, calling {@link #applyLabel} on
 * the first match.
 *
 * @since 1.0.0
 * @deprecated implement {@link LabelTarget}. This interface extends it, so existing implementations keep working; it
 *     is removed in 2.0.0.
 */
@Deprecated(since = "1.1.0", forRemoval = true)
public interface ILabelable extends LabelTarget {
    /**
     * Applies the label stack to this target.
     *
     * @param player the player applying the label
     * @param pos    the clicked position
     * @param face   the clicked face
     * @param stack  the label item stack
     * @return {@code true} when the stack should shrink by one
     * @deprecated implement {@link LabelTarget#attachLabel}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    boolean applyLabel(Player player, BlockPos pos, Direction face, ItemStack stack);

    /**
     * Answers {@link LabelTarget#attachLabel} through {@link #applyLabel}. The level is not passed on; the old method
     * reads it from the player.
     *
     * @param level the server level of the click
     * @param pos the clicked position
     * @param face the clicked face
     * @param player the player holding the label
     * @param label the label stack in the player's hand
     * @return the result of {@link #applyLabel}
     * @since 1.1.0
     */
    @Override
    default boolean attachLabel(Level level, BlockPos pos, Direction face, Player player, ItemStack label) {
        return applyLabel(player, pos, face, label);
    }
}
