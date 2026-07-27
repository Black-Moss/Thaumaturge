package com.leclowndu93150.thaumaturge.api.casters;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * An item capable of storing vis, holding a focus, and casting it. Implemented by the
 * caster's gauntlet.
 *
 *
 * @since 1.0.0
 */
public interface ICaster {
    /**
     * The multiplier applied to any vis cost paid through this caster. Values below 1 are
     * discounts.
     *
     * @param stack    the caster stack
     * @param player   the player paying the cost
     * @param crafting true when the cost is an arcane crafting cost rather than a cast
     * @return the cost multiplier, 1.0 meaning full price
     */
    float getConsumptionModifier(ItemStack stack, Player player, boolean crafting);

    /**
     * Attempts to pay a vis cost from the aura around the player. The consumption modifier is
     * applied before draining.
     *
     * @param stack    the caster stack
     * @param player   the player paying the cost
     * @param amount   the unmodified vis cost
     * @param crafting true when paying an arcane crafting cost
     * @param simulate true to only test whether the cost could be paid
     * @return true when the full cost was (or could be) paid
     */
    boolean consumeVis(ItemStack stack, Player player, float amount, boolean crafting, boolean simulate);

    /**
     * The focus item currently socketed into this caster.
     *
     * @param stack the caster stack
     * @return the socketed focus stack, or {@link ItemStack#EMPTY} when none
     */
    ItemStack getFocusStack(ItemStack stack);

    /**
     * Sockets a focus into this caster, replacing any previous focus.
     *
     * @param stack the caster stack
     * @param focus the focus stack to socket; empty to clear
     */
    void setFocus(ItemStack stack, ItemStack focus);

    /**
     * The block state remembered by a sneak-right-click block pick, consumed by foci that
     * place or exchange blocks.
     *
     * @param stack the caster stack
     * @return the picked block state, or null when nothing is picked
     */
    @Nullable BlockState getPickedBlock(ItemStack stack);
}
