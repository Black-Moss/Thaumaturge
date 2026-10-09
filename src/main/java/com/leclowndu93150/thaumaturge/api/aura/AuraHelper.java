package com.leclowndu93150.thaumaturge.api.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/**
 * Static facade over the per-chunk aura: vis, flux and the chunk's base capacity.
 *
 * <p>The facade holds no aura logic. Every method forwards to the {@link Bindings} implementation installed once during
 * mod initialisation, and passes arguments and results through without clamping or caching. Calling any method before
 * the implementation is bound throws {@link IllegalStateException}.
 *
 * <p>All methods are meant for the logical server. Clients read aura through the synced attachment and never call the
 * mutators.
 *
 * @since 1.0.0
 */
public final class AuraHelper {
    private static final String ALREADY_BOUND = "AuraHelper already bound";
    private static final String NOT_BOUND = "AuraHelper accessed before binding";

    private static Bindings bindings;

    private AuraHelper() {}

    /**
     * Installs the implementation behind this facade. Called once by the mod during initialisation.
     *
     * @param implementation the implementation to forward to
     * @throws IllegalStateException when an implementation is already bound
     */
    public static void bind(Bindings implementation) {
        if (bindings == null) {
            bindings = implementation;
            return;
        }
        throw new IllegalStateException(ALREADY_BOUND);
    }

    private static Bindings target() {
        if (bindings == null) {
            throw new IllegalStateException(NOT_BOUND);
        }
        return bindings;
    }

    /**
     * Looks up the aura record of a chunk by chunk position.
     *
     * @param level the server level
     * @param pos the chunk position
     * @return the aura record, never null; an empty record stamped with {@code pos} when the chunk has none yet
     * @throws IllegalStateException when the facade is not yet bound
     */
    public static IAuraChunk of(ServerLevel level, ChunkPos pos) {
        Bindings impl = target();
        return impl.chunkLookup(level, pos);
    }

    /**
     * Looks up the aura record of the chunk containing a block position.
     *
     * @param level the level
     * @param pos the block position
     * @return the aura record, never null; an empty record when the chunk has none yet
     * @throws IllegalStateException when the facade is not yet bound
     */
    public static IAuraChunk of(Level level, BlockPos pos) {
        Bindings impl = target();
        return impl.blockLookup(level, pos);
    }

    /**
     * Reads the vis of the chunk containing a position.
     *
     * @param level the level
     * @param pos the block position
     * @return the vis amount, 0 when the chunk has no record
     * @throws IllegalStateException when the facade is not yet bound
     */
    public static float getVis(Level level, BlockPos pos) {
        Bindings impl = target();
        return impl.getVis(level, pos);
    }

    /**
     * Reads the flux of the chunk containing a position.
     *
     * @param level the level
     * @param pos the block position
     * @return the flux amount, 0 when the chunk has no record
     * @throws IllegalStateException when the facade is not yet bound
     */
    public static float getFlux(Level level, BlockPos pos) {
        Bindings impl = target();
        return impl.getFlux(level, pos);
    }

    /**
     * Reads the base (maximum) aura of the chunk containing a position.
     *
     * @param level the level
     * @param pos the block position
     * @return the base aura in the range 0 to 500, 0 when the chunk has no record
     * @throws IllegalStateException when the facade is not yet bound
     */
    public static int getAuraBase(Level level, BlockPos pos) {
        Bindings impl = target();
        return impl.getAuraBase(level, pos);
    }

    /**
     * Reads the combined vis and flux of the chunk containing a position.
     *
     * @param level the level
     * @param pos the block position
     * @return vis plus flux
     * @throws IllegalStateException when the facade is not yet bound
     */
    public static float getTotalAura(Level level, BlockPos pos) {
        Bindings impl = target();
        return impl.getTotalAura(level, pos);
    }

    /**
     * Reads the flux divided by the base aura of the chunk containing a position.
     *
     * @param level the level
     * @param pos the block position
     * @return the ratio, which may exceed 1; 0 when the chunk has no record or its base is 0
     * @throws IllegalStateException when the facade is not yet bound
     */
    public static float getFluxSaturation(Level level, BlockPos pos) {
        Bindings impl = target();
        return impl.getFluxSaturation(level, pos);
    }

    /**
     * Reads how much more aura the chunk containing a position can hold.
     *
     * @param level the level
     * @param pos the block position
     * @return base minus total aura, floored at 0; 0 when the chunk has no record
     * @throws IllegalStateException when the facade is not yet bound
     */
    public static float capacityRemaining(Level level, BlockPos pos) {
        Bindings impl = target();
        return impl.capacityRemaining(level, pos);
    }

    /**
     * Tests whether the chunk containing a position can take an amount of vis.
     *
     * @param level the level
     * @param pos the block position
     * @param amount the amount to test
     * @return true when the remaining capacity is at least {@code amount}; non-positive amounts always fit
     * @throws IllegalStateException when the facade is not yet bound
     */
    public static boolean canAcceptVis(Level level, BlockPos pos, float amount) {
        Bindings impl = target();
        return impl.canAcceptVis(level, pos, amount);
    }

    /**
     * Tests whether vis draining should stop to protect the chunk's aura.
     *
     * <p>The result is false when the base is 0. Otherwise it is true only when the player is null or has completed the
     * aura preservation research, and vis divided by base is strictly below ten percent.
     *
     * @param level the level
     * @param player the acting player, or null for a non-player actor
     * @param pos the block position
     * @return true when the caller should stop draining vis
     * @throws IllegalStateException when the facade is not yet bound
     */
    public static boolean shouldPreserveAura(Level level, @Nullable Player player, BlockPos pos) {
        Bindings impl = target();
        return impl.shouldPreserveAura(level, player, pos);
    }

    /**
     * Adds vis to the chunk containing a position.
     *
     * <p>Negative amounts are ignored. The implementation clamps to its limits and does nothing when the chunk has no
     * record.
     *
     * @param level the level
     * @param pos the block position
     * @param amount the amount to add
     * @throws IllegalStateException when the facade is not yet bound
     */
    public static void addVis(Level level, BlockPos pos, float amount) {
        Bindings impl = target();
        impl.addVis(level, pos, amount);
    }

    /**
     * Adds flux to the chunk containing a position.
     *
     * <p>Negative amounts are ignored. The implementation clamps to its limits and does nothing when the chunk has no
     * record.
     *
     * @param level the level
     * @param pos the block position
     * @param amount the amount to add
     * @throws IllegalStateException when the facade is not yet bound
     */
    public static void addFlux(Level level, BlockPos pos, float amount) {
        Bindings impl = target();
        impl.addFlux(level, pos, amount);
    }

    /**
     * Adds flux to the chunk containing a position, optionally with a visual cue.
     *
     * <p>Negative amounts are ignored. When {@code withCue} is true the implementation also broadcasts a small
     * flux-fume cue at the position to nearby players.
     *
     * @param level the level
     * @param pos the block position
     * @param fluxAmount the flux amount to add
     * @param withCue whether to broadcast the visual cue
     * @throws IllegalStateException when the facade is not yet bound
     */
    public static void polluteAura(Level level, BlockPos pos, float fluxAmount, boolean withCue) {
        Bindings impl = target();
        impl.polluteAura(level, pos, fluxAmount, withCue);
    }

    /**
     * Removes flux from the chunk containing a position.
     *
     * @param level the level
     * @param pos the block position
     * @param requested the amount requested
     * @param dryRun when true, nothing is changed and the result reports what would be drained
     * @return the amount drained, never more than requested or available
     * @throws IllegalStateException when the facade is not yet bound
     */
    public static float drainFlux(Level level, BlockPos pos, float requested, boolean dryRun) {
        Bindings impl = target();
        return impl.drainFlux(level, pos, requested, dryRun);
    }

    /**
     * Removes vis from the chunk containing a position.
     *
     * @param level the level
     * @param pos the block position
     * @param requested the amount requested
     * @param dryRun when true, nothing is changed and the result reports what would be drained
     * @return the amount drained, never more than requested or available
     * @throws IllegalStateException when the facade is not yet bound
     */
    public static float drainVis(Level level, BlockPos pos, float requested, boolean dryRun) {
        Bindings impl = target();
        return impl.drainVis(level, pos, requested, dryRun);
    }

    /**
     * Removes vis from the chunk containing a position as part of a transaction.
     *
     * <p>The removal is undone if the given transaction or any enclosing one aborts. The chunk is marked for saving
     * when the outermost transaction commits. Non-positive amounts drain 0.
     *
     * @param level the level
     * @param pos the block position
     * @param amount the amount requested
     * @param transaction the transaction that owns the removal
     * @return the amount drained, never more than requested or available
     * @throws IllegalStateException when the facade is not yet bound
     * @since 1.0.0
     */
    public static float drainVis(Level level, BlockPos pos, float amount, TransactionContext transaction) {
        Bindings impl = target();
        return impl.drainVis(level, pos, amount, transaction);
    }

    /**
     * Implementation contract fulfilled by the mod and bound once through {@link AuraHelper#bind(Bindings)}.
     *
     * <p>Each method corresponds to the facade method of the same name; {@link #chunkLookup} and {@link #blockLookup}
     * back the two {@code of} overloads.
     *
     * @since 1.0.0
     */
    public interface Bindings {

        /**
         * Backs {@link AuraHelper#of(ServerLevel, ChunkPos)}.
         *
         * @param level the server level
         * @param pos the chunk position
         * @return the aura record, never null
         */
        IAuraChunk chunkLookup(ServerLevel level, ChunkPos pos);

        /**
         * Backs {@link AuraHelper#of(Level, BlockPos)}.
         *
         * @param level the level
         * @param pos the block position
         * @return the aura record, never null
         */
        IAuraChunk blockLookup(Level level, BlockPos pos);

        /**
         * Backs {@link AuraHelper#getVis(Level, BlockPos)}.
         *
         * @param level the level
         * @param pos the block position
         * @return the vis amount
         */
        float getVis(Level level, BlockPos pos);

        /**
         * Backs {@link AuraHelper#getFlux(Level, BlockPos)}.
         *
         * @param level the level
         * @param pos the block position
         * @return the flux amount
         */
        float getFlux(Level level, BlockPos pos);

        /**
         * Backs {@link AuraHelper#getAuraBase(Level, BlockPos)}.
         *
         * @param level the level
         * @param pos the block position
         * @return the base aura
         */
        int getAuraBase(Level level, BlockPos pos);

        /**
         * Backs {@link AuraHelper#getTotalAura(Level, BlockPos)}.
         *
         * @param level the level
         * @param pos the block position
         * @return vis plus flux
         */
        float getTotalAura(Level level, BlockPos pos);

        /**
         * Backs {@link AuraHelper#getFluxSaturation(Level, BlockPos)}.
         *
         * @param level the level
         * @param pos the block position
         * @return the flux to base ratio
         */
        float getFluxSaturation(Level level, BlockPos pos);

        /**
         * Backs {@link AuraHelper#shouldPreserveAura(Level, Player, BlockPos)}.
         *
         * @param level the level
         * @param player the acting player, or null
         * @param pos the block position
         * @return true when vis draining should stop
         */
        boolean shouldPreserveAura(Level level, @Nullable Player player, BlockPos pos);

        /**
         * Backs {@link AuraHelper#addVis(Level, BlockPos, float)}.
         *
         * @param level the level
         * @param pos the block position
         * @param amount the amount to add
         */
        void addVis(Level level, BlockPos pos, float amount);

        /**
         * Backs {@link AuraHelper#addFlux(Level, BlockPos, float)}.
         *
         * @param level the level
         * @param pos the block position
         * @param amount the amount to add
         */
        void addFlux(Level level, BlockPos pos, float amount);

        /**
         * Backs {@link AuraHelper#drainVis(Level, BlockPos, float, boolean)}.
         *
         * @param level the level
         * @param pos the block position
         * @param amount the amount requested
         * @param simulate whether to leave the chunk unchanged
         * @return the amount drained
         */
        float drainVis(Level level, BlockPos pos, float amount, boolean simulate);

        /**
         * Backs {@link AuraHelper#drainVis(Level, BlockPos, float, TransactionContext)}.
         *
         * @param level the level
         * @param pos the block position
         * @param amount the amount requested
         * @param transaction the owning transaction
         * @return the amount drained
         */
        float drainVis(Level level, BlockPos pos, float amount, TransactionContext transaction);

        /**
         * Backs {@link AuraHelper#drainFlux(Level, BlockPos, float, boolean)}.
         *
         * @param level the level
         * @param pos the block position
         * @param amount the amount requested
         * @param simulate whether to leave the chunk unchanged
         * @return the amount drained
         */
        float drainFlux(Level level, BlockPos pos, float amount, boolean simulate);

        /**
         * Backs {@link AuraHelper#polluteAura(Level, BlockPos, float, boolean)}.
         *
         * @param level the level
         * @param pos the block position
         * @param amount the flux amount to add
         * @param showEffect whether to broadcast the visual cue
         */
        void polluteAura(Level level, BlockPos pos, float amount, boolean showEffect);

        /**
         * Backs {@link AuraHelper#capacityRemaining(Level, BlockPos)}.
         *
         * @param level the level
         * @param pos the block position
         * @return the remaining capacity
         */
        float capacityRemaining(Level level, BlockPos pos);

        /**
         * Backs {@link AuraHelper#canAcceptVis(Level, BlockPos, float)}.
         *
         * @param level the level
         * @param pos the block position
         * @param amount the amount to test
         * @return true when the amount fits
         */
        boolean canAcceptVis(Level level, BlockPos pos, float amount);
    }
}
