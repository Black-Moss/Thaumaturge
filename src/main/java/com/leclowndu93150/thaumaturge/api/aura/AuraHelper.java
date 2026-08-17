package com.leclowndu93150.thaumaturge.api.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Public addon API for per-chunk aura: pure vis, accumulated flux, and the chunk's max base. All methods are
 * server-side; clients receive aura snapshots through the underlying attachment sync and should never call mutators.
 *
 * <p>
 * Every read returns zero when the chunk has no aura record yet (generation runs on first load). Every mutator silently
 * no-ops when the chunk is missing. The intended contract is that addons call freely without null checks; values clamp
 * at the impl boundary.
 *
 * <p>
 * The helper is a static facade. The implementation is bound at mod init by Thaumaturge via {@link #bind(Bindings)};
 * addons must not call {@code bind}.
 *
 * @since 1.0.0
 */
public final class AuraHelper {
    private static Bindings impl;

    private AuraHelper() {}

    /**
     * Returns the aura record for the given chunk on the server side.
     *
     * @param level the server level
     * @param pos the chunk position
     * @return the aura record, never null; freshly-created entries report base/vis/flux as zero until generation has
     *         populated them
     * @throws IllegalStateException when called before the implementation has bound the helper
     */
    public static IAuraChunk of(ServerLevel level, ChunkPos pos) {
        return bindingOrThrow().chunkLookup(level, pos);
    }

    /**
     * Returns the aura record for the chunk containing the given block position. Resolves the chunk by right-shifting the
     * block coordinates by four.
     *
     * @param level the level; must be a server level at runtime
     * @param pos a block position inside the target chunk
     * @return the aura record, never null
     * @throws IllegalStateException when called before the implementation has bound the helper
     */
    public static IAuraChunk of(Level level, BlockPos pos) {
        return bindingOrThrow().blockLookup(level, pos);
    }

    /**
     * Returns the current pure vis in the chunk containing the given block.
     *
     * @param level the level
     * @param pos block position resolving to the chunk
     * @return vis amount; zero when no aura record exists yet
     */
    public static float getVis(Level level, BlockPos pos) {
        return bindingOrThrow().getVis(level, pos);
    }

    /**
     * Returns the current flux in the chunk containing the given block.
     *
     * @param level the level
     * @param pos block position resolving to the chunk
     * @return flux amount; zero when no aura record exists yet
     */
    public static float getFlux(Level level, BlockPos pos) {
        return bindingOrThrow().getFlux(level, pos);
    }

    /**
     * Returns the chunk's maximum aura, sampled at generation time from biome modifiers.
     *
     * @param level the level
     * @param pos block position resolving to the chunk
     * @return base aura, clamped to {@code [0, 500]}; zero when no aura record exists yet
     */
    public static int getAuraBase(Level level, BlockPos pos) {
        return bindingOrThrow().getAuraBase(level, pos);
    }

    /**
     * Returns the sum of vis and flux in the chunk.
     *
     * @param level the level
     * @param pos block position resolving to the chunk
     * @return {@code getVis + getFlux}
     */
    public static float getTotalAura(Level level, BlockPos pos) {
        return bindingOrThrow().getTotalAura(level, pos);
    }

    /**
     * Returns the chunk's flux saturation ratio: {@code flux / base}.
     *
     * @param level the level
     * @param pos block position resolving to the chunk
     * @return saturation in {@code [0, 1+]} when the rift threshold is exceeded; zero when no aura record exists or base is
     *         zero
     */
    public static float getFluxSaturation(Level level, BlockPos pos) {
        return bindingOrThrow().getFluxSaturation(level, pos);
    }

    /**
     * Tests whether aura preservation applies in the chunk. Returns true when the player (or the server context, for
     * {@code player == null}) holds the aura-preservation research and the chunk's vis has dropped below ten percent of
     * base. Callers should bail out of vis-draining operations when this returns true.
     *
     * @param level the level
     * @param player the player whose research gates the check; null bypasses the gate
     * @param pos block position resolving to the chunk
     * @return true when the chunk is in preserve mode and the caller should not drain further vis
     */
    public static boolean shouldPreserveAura(Level level, @Nullable Player player, BlockPos pos) {
        return bindingOrThrow().shouldPreserveAura(level, player, pos);
    }

    /**
     * Adds pure vis to the chunk. Negative amounts are silently ignored; use {@link #drainVis} to remove vis.
     *
     * @param level the level
     * @param pos block position resolving to the chunk
     * @param amount vis to add; must be non-negative
     */
    public static void addVis(Level level, BlockPos pos, float amount) {
        bindingOrThrow().addVis(level, pos, amount);
    }

    /**
     * Adds flux to the chunk. Negative amounts are silently ignored; use {@link #drainFlux} to remove flux.
     *
     * @param level the level
     * @param pos block position resolving to the chunk
     * @param amount flux to add; must be non-negative
     */
    public static void addFlux(Level level, BlockPos pos, float amount) {
        bindingOrThrow().addFlux(level, pos, amount);
    }

    /**
     * Drains up to {@code amount} pure vis from the chunk.
     *
     * @param level the level
     * @param pos block position resolving to the chunk
     * @param amount maximum vis to drain
     * @param simulate when true, the chunk record is not modified and the method only reports how much would have been
     *            drained
     * @return the amount of vis actually drained, never greater than {@code amount} or the chunk's available vis
     */
    public static float drainVis(Level level, BlockPos pos, float amount, boolean simulate) {
        return bindingOrThrow().drainVis(level, pos, amount, simulate);
    }

    /**
     * Drains up to {@code amount} flux from the chunk.
     *
     * @param level the level
     * @param pos block position resolving to the chunk
     * @param amount maximum flux to drain
     * @param simulate when true, the chunk record is not modified and the method only reports how much would have been
     *            drained
     * @return the amount of flux actually drained, never greater than {@code amount} or the chunk's available flux
     */
    public static float drainFlux(Level level, BlockPos pos, float amount, boolean simulate) {
        return bindingOrThrow().drainFlux(level, pos, amount, simulate);
    }

    /**
     * Returns how much more aura the chunk can hold before reaching its base, that is {@code max(0, base - (vis + flux))}.
     * Useful for sizing vis a caller intends to add.
     *
     * @param level the level
     * @param pos block position resolving to the chunk
     * @return the remaining headroom in aura units; zero when the chunk is full or has no record
     */
    public static float capacityRemaining(Level level, BlockPos pos) {
        return bindingOrThrow().capacityRemaining(level, pos);
    }

    /**
     * Tests whether the chunk can absorb the given amount of vis without exceeding its base. Equivalent to
     * {@code capacityRemaining(level, pos) >= amount}.
     *
     * @param level the level
     * @param pos block position resolving to the chunk
     * @param amount the vis amount to test; non-positive amounts always fit
     * @return true when the chunk has room for {@code amount} more aura
     */
    public static boolean canAcceptVis(Level level, BlockPos pos, float amount) {
        return bindingOrThrow().canAcceptVis(level, pos, amount);
    }

    /**
     * Adds flux to the chunk, optionally broadcasting a small visual cue at {@code pos}. Equivalent to {@link #addFlux}
     * when {@code showEffect} is false.
     *
     * @param level the level
     * @param pos block position resolving to the chunk; doubles as the visual effect anchor
     * @param amount flux to add; must be non-negative
     * @param showEffect when true, a flux-fume cue is broadcast to nearby players
     */
    public static void polluteAura(Level level, BlockPos pos, float amount, boolean showEffect) {
        bindingOrThrow().polluteAura(level, pos, amount, showEffect);
    }

    /**
     * Binds the helper's implementation. Called once at mod init by the implementation; addons must not call this.
     *
     * @param bindings the implementation
     * @throws IllegalStateException when already bound
     */
    public static void bind(Bindings bindings) {
        if (impl != null) {
            throw new IllegalStateException("AuraHelper already bound");
        }
        impl = bindings;
    }

    private static Bindings bindingOrThrow() {
        if (impl == null) {
            throw new IllegalStateException("AuraHelper accessed before binding");
        }
        return impl;
    }

    /**
     * Implementation hook supplied by Thaumaturge at mod init. Each method on this interface corresponds to a public static
     * on {@link AuraHelper}. Addons must not implement this interface.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        IAuraChunk chunkLookup(ServerLevel level, ChunkPos pos);

        IAuraChunk blockLookup(Level level, BlockPos pos);

        float getVis(Level level, BlockPos pos);

        float getFlux(Level level, BlockPos pos);

        int getAuraBase(Level level, BlockPos pos);

        float getTotalAura(Level level, BlockPos pos);

        float getFluxSaturation(Level level, BlockPos pos);

        boolean shouldPreserveAura(Level level, @Nullable Player player, BlockPos pos);

        void addVis(Level level, BlockPos pos, float amount);

        void addFlux(Level level, BlockPos pos, float amount);

        float drainVis(Level level, BlockPos pos, float amount, boolean simulate);

        float drainFlux(Level level, BlockPos pos, float amount, boolean simulate);

        void polluteAura(Level level, BlockPos pos, float amount, boolean showEffect);

        float capacityRemaining(Level level, BlockPos pos);

        boolean canAcceptVis(Level level, BlockPos pos, float amount);
    }
}
