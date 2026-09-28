package com.leclowndu93150.thaumaturge.api.recipe;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A supplier of aura vis for arcane crafts that do not run at a Thaumaturge arcane workbench.
 * A placed arcane workbench always pays the aura part of a craft from the chunk aura around it;
 * every other host, placed or virtual, pays it from the registered aura sources in registration
 * order. A craft with an aura cost fails when the sources together cannot cover it.
 *
 * <p>Supply runs inside a NeoForge transaction with the same contract as
 * {@link IWorkbenchVisSource}: state changes are journaled so an abort restores them, and side
 * effects wait for the root commit.
 *
 * <p>Register sources through {@link RegisterWorkbenchAuraSourcesEvent}.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface IWorkbenchAuraSource {
    /**
     * Supplies up to {@code need} aura vis toward a craft.
     *
     * @param context     where and for whom the craft runs
     * @param player      the crafting player
     * @param workbench   the workbench inventory
     * @param need        the aura vis still required
     * @param transaction the open transaction the supply belongs to
     * @return the vis supplied, never more than {@code need}; larger or negative values are
     *         clamped by the caller
     */
    int supply(ArcaneWorkbenchContext context, ServerPlayer player, IArcaneWorkbench workbench, int need, TransactionContext transaction);
}
