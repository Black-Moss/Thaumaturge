package com.leclowndu93150.thaumaturge.api.casters;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * One use of a caster on a block, as handed to a {@link CasterBlockAction}.
 *
 * @param level the level of the click, on either logical side
 * @param caster the caster stack that was used
 * @param player the player who used it
 * @param pos the clicked position
 * @param face the clicked face
 * @param state the block state that was clicked, which selects the actions to run
 * @since 1.1.0
 */
public record CasterBlockUse(Level level, ItemStack caster, Player player, BlockPos pos, Direction face, BlockState state) {
}
