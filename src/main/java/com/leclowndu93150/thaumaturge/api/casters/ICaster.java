package com.leclowndu93150.thaumaturge.api.casters;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * An item capable of storing vis, holding a focus, and casting it. Implemented by the
 * caster's gauntlet.
 *
 * <p>Since 1.1.0 every method here and every {@link FocusCaster} method has a default that answers through its
 * counterpart, so an implementation overrides one set or the other. An implementation written against 1.0.0 overrides
 * the methods declared here and is a {@link FocusCaster} as well. Thaumaturge's own casters implement the
 * {@link FocusCaster} methods and still implement this interface, so code that tests for it keeps finding them.
 *
 * @apiNote An implementation that overrides neither set recurses without end on the first call.
 * @since 1.0.0
 * @deprecated use {@link FocusCaster}. This interface extends it and is removed in 2.0.0.
 */
@Deprecated(since = "1.1.0", forRemoval = true)
public interface ICaster extends FocusCaster {
    /**
     * The multiplier applied to any vis cost paid through this caster. Values below 1 are
     * discounts.
     *
     * @param stack    the caster stack
     * @param player   the player paying the cost
     * @param crafting true when the cost is an arcane crafting cost rather than a cast
     * @return the cost multiplier, 1.0 meaning full price
     * @deprecated use {@link FocusCaster#visCostFactor}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    default float getConsumptionModifier(ItemStack stack, Player player, boolean crafting) {
        return visCostFactor(stack, player, crafting);
    }

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
     * @deprecated use {@link FocusCaster#drawVis}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    default boolean consumeVis(ItemStack stack, Player player, float amount, boolean crafting, boolean simulate) {
        return drawVis(stack, player, amount, crafting, simulate);
    }

    /**
     * The focus item currently socketed into this caster.
     *
     * @param stack the caster stack
     * @return the socketed focus stack, or {@link ItemStack#EMPTY} when none
     * @deprecated use {@link FocusCaster#socketedFocus}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    default ItemStack getFocusStack(ItemStack stack) {
        return socketedFocus(stack);
    }

    /**
     * Sockets a focus into this caster, replacing any previous focus.
     *
     * @param stack the caster stack
     * @param focus the focus stack to socket; empty to clear
     * @deprecated use {@link FocusCaster#socketFocus}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    default void setFocus(ItemStack stack, ItemStack focus) {
        socketFocus(stack, focus);
    }

    /**
     * The block state remembered by a sneak-right-click block pick, consumed by foci that
     * place or exchange blocks.
     *
     * @param stack the caster stack
     * @return the picked block state, or null when nothing is picked
     * @deprecated use {@link FocusCaster#rememberedBlock}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    @Nullable
    default BlockState getPickedBlock(ItemStack stack) {
        return rememberedBlock(stack);
    }

    /**
     * Answers {@link FocusCaster#socketedFocus} through {@link #getFocusStack}.
     *
     * @param caster the caster stack
     * @return the result of {@link #getFocusStack}
     * @since 1.1.0
     */
    @Override
    default ItemStack socketedFocus(ItemStack caster) {
        return getFocusStack(caster);
    }

    /**
     * Answers {@link FocusCaster#socketFocus} through {@link #setFocus}.
     *
     * @param caster the caster stack
     * @param focus the focus to socket, or an empty stack to clear
     * @since 1.1.0
     */
    @Override
    default void socketFocus(ItemStack caster, ItemStack focus) {
        setFocus(caster, focus);
    }

    /**
     * Answers {@link FocusCaster#rememberedBlock} through {@link #getPickedBlock}.
     *
     * @param caster the caster stack
     * @return the result of {@link #getPickedBlock}
     * @since 1.1.0
     */
    @Override
    @Nullable
    default BlockState rememberedBlock(ItemStack caster) {
        return getPickedBlock(caster);
    }

    /**
     * Answers {@link FocusCaster#visCostFactor} through {@link #getConsumptionModifier}.
     *
     * @param caster the caster stack
     * @param player the player paying, possibly {@code null}
     * @param forCrafting {@code true} for an arcane crafting cost
     * @return the result of {@link #getConsumptionModifier}
     * @since 1.1.0
     */
    @Override
    default float visCostFactor(ItemStack caster, @Nullable Player player, boolean forCrafting) {
        return getConsumptionModifier(caster, player, forCrafting);
    }

    /**
     * Answers {@link FocusCaster#drawVis} through {@link #consumeVis}.
     *
     * @param caster the caster stack
     * @param player the player paying
     * @param baseCost the cost before the factor is applied
     * @param forCrafting {@code true} for an arcane crafting cost
     * @param simulate {@code true} to only test the payment
     * @return the result of {@link #consumeVis}
     * @since 1.1.0
     */
    @Override
    default boolean drawVis(ItemStack caster, Player player, float baseCost, boolean forCrafting, boolean simulate) {
        return consumeVis(caster, player, baseCost, forCrafting, simulate);
    }
}
