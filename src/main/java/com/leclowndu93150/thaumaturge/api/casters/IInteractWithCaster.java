package com.leclowndu93150.thaumaturge.api.casters;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Implemented by a block, block entity or item that wants to handle a caster right-click
 * before any other caster behaviour runs.
 *
 * <p>The caster invokes this on both the logical client and the logical server, so
 * implementations receive a level of either side and must tolerate both.
 *
 * @since 1.0.0
 */
public interface IInteractWithCaster {
    /**
     * Called when a caster is used on the implementing target.
     *
     * @param level       the level the click happened in, client or server
     * @param casterStack the caster stack used
     * @param player      the clicking player
     * @param pos         the clicked position
     * @param side        the clicked face
     * @param hand        the hand holding the caster
     * @return true to consume the interaction and suppress all further caster behaviour for the
     *         click, false to let the click continue to later handling
     */
    boolean onCasterRightClick(Level level, ItemStack casterStack, Player player, BlockPos pos, Direction side, InteractionHand hand);
}
