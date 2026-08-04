package com.leclowndu93150.thaumaturge.api.casters;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Handles a caster right-click on a block state registered with
 * {@link CasterTriggerRegistry}. One manager can serve several block states by
 * distinguishing them through the {@code event} number supplied at registration.
 *
 * @since 1.0.0
 */
public interface ICasterTriggerManager {
    /**
     * Performs the trigger action for a caster right-click.
     *
     * @param level       the level the click happened in
     * @param casterStack the caster stack used
     * @param player      the clicking player
     * @param pos         the clicked position
     * @param side        the clicked face
     * @param event       the event number this manager was registered with
     * @return true when the trigger handled the click and no further triggers should run
     */
    boolean performTrigger(Level level, ItemStack casterStack, Player player, BlockPos pos, Direction side, int event);
}
