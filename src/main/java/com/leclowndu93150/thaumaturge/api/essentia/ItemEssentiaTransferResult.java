package com.leclowndu93150.thaumaturge.api.essentia;

import net.minecraft.world.item.ItemStack;

/**
 * The outcome of an {@link IEssentiaItemStorage} transfer.
 *
 * @param amountMoved    how much essentia moves, zero when nothing does
 * @param resultingStack the stack that replaces the source stack when the transfer is committed
 * @since 1.0.0
 */
public record ItemEssentiaTransferResult(int amountMoved, ItemStack resultingStack) {
    /**
     * Copies the stack so the record owns it.
     *
     * @throws IllegalArgumentException when {@code amountMoved} is negative
     */
    public ItemEssentiaTransferResult {
        if (amountMoved < 0) {
            throw new IllegalArgumentException("amountMoved must not be negative");
        }
        resultingStack = resultingStack.copy();
    }

    /**
     * The stack to put in the source slot.
     *
     * @return a copy of the resulting stack
     */
    @Override
    public ItemStack resultingStack() {
        return resultingStack.copy();
    }
}
