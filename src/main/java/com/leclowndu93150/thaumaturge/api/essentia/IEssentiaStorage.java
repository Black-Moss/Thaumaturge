package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * An essentia storage block seen from one side: its contents can be listed, and essentia can be
 * inserted or extracted inside a NeoForge transaction.
 *
 * <p>Views come from {@link EssentiaCapabilities#STORAGE}. The capability context picks the side,
 * so a view is already bound to it; querying with a {@code null} side returns no view. A view may
 * be extract-only (an alembic) by accepting nothing.
 *
 * <p>Insertions and extractions change the storage at once and are undone when the transaction
 * or an enclosing transaction aborts. Simulate a transfer by opening a transaction and closing it
 * without committing. Saving, client sync and similar effects happen when the outermost
 * transaction commits.
 *
 * <p>All methods are for the server thread. A view follows NeoForge block capability lifetime
 * rules: it stops being valid when its block entity is removed or its capabilities are
 * invalidated, and must not be kept after that.
 *
 * @since 1.0.0
 */
public interface IEssentiaStorage {
    /**
     * An immutable snapshot of everything stored, safe to keep after the storage changes.
     *
     * @return the contents, never {@code null}
     */
    AspectList contents();

    /**
     * The stored amount of one aspect.
     *
     * @param aspect the aspect
     * @return its amount, or zero when absent
     */
    default int amount(Holder<IAspect> aspect) {
        return contents().amountOf(aspect);
    }

    /**
     * Inserts up to {@code amount} of an aspect.
     *
     * @param aspect      the aspect supplied
     * @param amount      the most to insert
     * @param transaction the open transaction the insertion belongs to
     * @return the amount accepted, from zero to {@code amount}
     */
    int insert(Holder<IAspect> aspect, int amount, TransactionContext transaction);

    /**
     * Extracts up to {@code amount} of an aspect.
     *
     * @param aspect      the aspect requested
     * @param amount      the most to extract
     * @param transaction the open transaction the extraction belongs to
     * @return the amount extracted, from zero to {@code amount}
     */
    int extract(Holder<IAspect> aspect, int amount, TransactionContext transaction);

    /**
     * A number that grows every time committed contents change, so a consumer can cache
     * {@link #contents()} while it stays the same.
     *
     * <p>It advances once per committed change to the contents, whether the change came through
     * this view, another side's view or the block's own logic such as tubes. Aborted transactions,
     * loading saved data and client sync do not advance it. It is only meaningful for the life of
     * the block entity and restarts after the chunk reloads.
     *
     * @return the current content revision
     */
    long contentRevision();
}
