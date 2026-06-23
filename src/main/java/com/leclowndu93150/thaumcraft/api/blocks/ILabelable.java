package com.leclowndu93150.thaumcraft.api.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Marker for blocks and block entities that accept the label item.
 *
 * <p>The label item walks the clicked block then the block entity, calling {@link #applyLabel} on
 * the first match.
 *
 * @since 1.0.0
 */
public interface ILabelable {
    /**
     * Applies the label stack to this target.
     *
     * @param player the player applying the label
     * @param pos    the clicked position
     * @param face   the clicked face
     * @param stack  the label item stack
     * @return {@code true} when the stack should shrink by one
     */
    boolean applyLabel(Player player, BlockPos pos, Direction face, ItemStack stack);
}
