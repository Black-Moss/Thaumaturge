package com.leclowndu93150.thaumaturge.api.casters;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * An item that holds a focus, pays vis for what it does and casts the focus's spell. Thaumaturge's wands implement it.
 *
 * <p>Every method reads or writes the state of the caster stack passed in, never of the item singleton, so one
 * implementation serves every stack of the item. The mod recognises a caster by its item implementing this interface:
 * the focus picker, the caster HUD and the arcane workbench's caster slot all test for it.
 *
 * @since 1.1.0
 */
public interface FocusCaster {
    /**
     * Returns the focus currently socketed in the caster.
     *
     * @param caster the caster stack
     * @return a copy of the socketed focus, or {@link ItemStack#EMPTY} when the caster holds none; changing the returned
     *     stack does not change the caster
     * @since 1.1.0
     */
    ItemStack socketedFocus(ItemStack caster);

    /**
     * Sockets a focus into the caster, replacing whatever was there. The previous focus is discarded, so callers that
     * swap foci read {@link #socketedFocus} first and return it to the player themselves.
     *
     * @param caster the caster stack
     * @param focus the focus to socket, or an empty stack to leave the caster without a focus
     * @since 1.1.0
     */
    void socketFocus(ItemStack caster, ItemStack focus);

    /**
     * Returns the block state the caster remembers from a sneak-use on a block, as used by foci that place or swap
     * blocks.
     *
     * @param caster the caster stack
     * @return the remembered state, or {@code null} when nothing was picked
     * @see IFocusBlockPicker
     * @since 1.1.0
     */
    @Nullable
    BlockState rememberedBlock(ItemStack caster);

    /**
     * Returns the factor applied to every vis cost paid through this caster. Values below 1 are discounts.
     *
     * @param caster the caster stack
     * @param player the player paying, or {@code null} when no player is involved, as when a tooltip lists the caster's
     *     costs
     * @param forCrafting {@code true} for an arcane crafting cost, {@code false} for a spell cast
     * @return the cost factor, where 1 is full price
     * @since 1.1.0
     */
    float visCostFactor(ItemStack caster, @Nullable Player player, boolean forCrafting);

    /**
     * Pays a vis cost through the caster. The {@link #visCostFactor cost factor} is applied before anything is taken,
     * and a cost that cannot be paid in full takes nothing.
     *
     * @implNote Thaumaturge's wands pay from the vis stored in the wand, split evenly across the primal aspects.
     *
     * @param caster the caster stack
     * @param player the player paying
     * @param baseCost the cost before the factor is applied; a cost of zero or less always succeeds
     * @param forCrafting {@code true} for an arcane crafting cost, {@code false} for a spell cast
     * @param simulate {@code true} to only test whether the cost could be paid, leaving the caster unchanged
     * @return {@code true} when the full cost was paid, or could be paid when simulating
     * @since 1.1.0
     */
    boolean drawVis(ItemStack caster, Player player, float baseCost, boolean forCrafting, boolean simulate);
}
