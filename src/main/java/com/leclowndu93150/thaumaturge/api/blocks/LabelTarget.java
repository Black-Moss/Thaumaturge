package com.leclowndu93150.thaumaturge.api.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Something in the world that a label can be stuck on, such as an essentia jar or an alembic.
 *
 * <p>When a player uses a label on a block, the label item looks for a target in this order: the clicked block, then
 * the block entity at the clicked position. The first one that implements this interface is asked through
 * {@link #attachLabel}; a block entity is never asked when its block is a target. The target alone decides whether the
 * label sticks, typically from the clicked face, its own contents and the aspect the label names.
 *
 * <p>When the target accepts, the label item uses up one label unless the player is in creative mode. When it refuses,
 * the click carries on to whatever else it would do.
 *
 * @apiNote Implementations play their own sounds and apply their own state changes, and must not shrink the label stack
 * themselves.
 * @implNote {@link #attachLabel} is called on the logical server only, from the server thread.
 * @since 1.1.0
 */
public interface LabelTarget {
    /**
     * Offers a label to this target.
     *
     * @param level the server level of the click
     * @param pos the clicked position
     * @param face the clicked face
     * @param player the player holding the label
     * @param label the label stack in the player's hand; read it, do not modify it
     * @return {@code true} when the label was attached and one label should be used up, {@code false} when the target
     *     refused it and changed nothing
     * @since 1.1.0
     */
    boolean attachLabel(Level level, BlockPos pos, Direction face, Player player, ItemStack label);
}
