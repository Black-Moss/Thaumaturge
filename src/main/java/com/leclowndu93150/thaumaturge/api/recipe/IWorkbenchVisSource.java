package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * An external supplier of primal vis toward an arcane craft. Registered sources are consulted by
 * the workbench payment planner after the wand in the wand slot and before the loaded crystals, so
 * that an addon (for example a networked storage system near the workbench) can pay a primal's
 * share of a craft instead of a crystal.
 *
 * <p>Supply runs inside a NeoForge transaction. A source changes its own state directly and
 * records the change with a {@code SnapshotJournal} so that aborting the transaction restores it. Side effects that must only happen once the craft
 * is final, such as syncing to clients, playing sounds or spawning particles, belong in the
 * journal's root commit callback. The planner sizes a craft by calling sources inside a
 * transaction it then aborts, so a source must not act on a supply before the root commit.
 *
 * <p>Register sources through {@link RegisterWorkbenchVisSourcesEvent}.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface IWorkbenchVisSource {
    /**
     * Supplies up to {@code need} centivis of the given aspect toward a craft.
     *
     * @param context     where and for whom the craft runs
     * @param player      the crafting player
     * @param workbench   the workbench inventory
     * @param aspect      the primal aspect required
     * @param need        the centivis still required for this aspect
     * @param transaction the open transaction the supply belongs to
     * @return the centivis supplied, never more than {@code need}; larger or negative values are
     *         clamped by the caller
     */
    int supply(ArcaneWorkbenchContext context, ServerPlayer player, IArcaneWorkbench workbench, Holder<IAspect> aspect, int need, TransactionContext transaction);
}
