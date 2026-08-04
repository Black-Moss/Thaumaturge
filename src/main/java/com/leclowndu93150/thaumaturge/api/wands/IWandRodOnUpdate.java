package com.leclowndu93150.thaumaturge.api.wands;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Callback attached to a {@link WandRod} that runs while a wand built from that rod sits in a
 * player's inventory. Used by elemental rods to slowly regenerate their associated primal vis.
 *
 * @since 1.0.0
 */
public interface IWandRodOnUpdate {
    /**
     * Called once per inventory tick on the logical server for each wand whose rod declares
     * this callback.
     *
     * @param wand   the wand stack being ticked
     * @param player the player carrying the wand
     */
    void onUpdate(ItemStack wand, Player player);
}
