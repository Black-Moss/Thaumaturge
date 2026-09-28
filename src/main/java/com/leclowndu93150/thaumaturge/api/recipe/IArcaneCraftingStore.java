package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * The item side of an arcane craft: the storage that holds the crafting grid, the crystals and
 * the wand that an {@link IArcaneCraftingInput} was read from. The craft transaction pays the vis
 * and aura, then hands the store a {@link Consumption} to apply inside the same transaction.
 *
 * <p>The store changes its storage directly and journals the change so that an abort of the
 * transaction, or of any enclosing transaction, restores it. The crafted output is not part of the
 * consumption; the caller receives it in {@link ArcaneCraftingTransaction.Result#output()} and
 * places it itself.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface IArcaneCraftingStore {
    /**
     * Applies one craft to the storage.
     *
     * <p>For each grid position the store removes one item and puts the matching remainder in its
     * place, or elsewhere when that position still holds items. It removes the listed crystals and
     * replaces its wand with {@link Consumption#wand()}.
     *
     * @param consumption what the craft uses up
     * @param transaction the open transaction the change belongs to
     * @return true when the storage still matched {@link Consumption#grid()} and the change was
     *         applied; false when the storage changed since the input was read, in which case the
     *         craft fails and the transaction is aborted
     */
    boolean consume(Consumption consumption, TransactionContext transaction);

    /**
     * What one craft uses up. All stacks are copies owned by the receiver.
     *
     * @param grid       the crafting grid the recipe matched, trimmed to the pattern's bounding box
     *                   and indexed {@code x + y * width} like {@link IArcaneCraftingInput#getItem(int, int)}
     * @param remainders the item left behind at each grid position, indexed like {@code grid}
     * @param crystals   the essentia crystals to remove, one per aspect unit
     * @param wand       the wand as it must be after the craft; equal to the input's wand when the
     *                   craft draws no wand vis, and empty when there is no wand
     * @since 1.0.0
     */
    record Consumption(List<ItemStack> grid, List<ItemStack> remainders, AspectList crystals, ItemStack wand) {
        /**
         * Copies every stack so the receiver owns them.
         *
         * @throws NullPointerException when a component is null
         */
        public Consumption {
            grid = grid.stream().map(ItemStack::copy).toList();
            remainders = remainders.stream().map(ItemStack::copy).toList();
            Objects.requireNonNull(crystals, "crystals");
            wand = wand.copy();
        }
    }
}
