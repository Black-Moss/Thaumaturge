package com.leclowndu93150.thaumaturge.api.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * Static facade over the taint spread engine. Addons can seed, query, and drive taint without
 * touching internals.
 *
 * <p>All mutating calls are server side; passing a client level is a no-op for queries and
 * ignored for mutators.
 *
 * @since 1.0.0
 */
public final class TaintApi {
    private static Bindings impl;

    private TaintApi() {}

    /**
     * Registers a taint seed anchor. Spread checks treat registered positions as live seeds as
     * long as a taint seed entity is present within one block of the position; stale entries are
     * pruned automatically during spread checks.
     *
     * @param level the level owning the seed
     * @param pos   the seed anchor position
     */
    public static void addTaintSeed(ServerLevel level, BlockPos pos) {
        bindingOrThrow().addTaintSeed(level, pos);
    }

    /**
     * Removes a taint seed anchor previously registered via {@link #addTaintSeed}.
     *
     * @param level the level owning the seed
     * @param pos   the seed anchor position
     */
    public static void removeTaintSeed(ServerLevel level, BlockPos pos) {
        bindingOrThrow().removeTaintSeed(level, pos);
    }

    /**
     * Returns whether the position lies within the configured spread radius of a live taint
     * seed. A registered seed whose entity has died is pruned and reported as absent.
     *
     * @param level the level to query
     * @param pos   the position to test
     * @return {@code true} when a live seed is in range
     */
    public static boolean isNearTaintSeed(Level level, BlockPos pos) {
        return bindingOrThrow().isNearTaintSeed(level, pos);
    }

    /**
     * Returns whether the position lies on the outer fringe of a seed's spread zone, the ring
     * where new taint seeds may sprout naturally.
     *
     * @param level the level to query
     * @param pos   the position to test
     * @return {@code true} when the position is between 80% and 100% of the spread radius
     */
    public static boolean isAtTaintSeedEdge(Level level, BlockPos pos) {
        return bindingOrThrow().isAtTaintSeedEdge(level, pos);
    }

    /**
     * Runs one taint spread attempt from the given position, identical to the random-tick spread
     * performed by taint blocks. Honours the wuss-mode and spread-rate configuration unless
     * {@code force} is set.
     *
     * @param level the level to mutate
     * @param pos   the spread origin
     * @param force when true, bypasses the configured rate and wuss-mode gates
     */
    public static void spreadFibres(ServerLevel level, BlockPos pos, boolean force) {
        bindingOrThrow().spreadFibres(level, pos, force);
    }

    /**
     * Returns the ecological taint pressure of the chunk containing the position, from 0 to 1.
     * Pressure decays slowly over time, and more slowly where the aura holds a lot of flux.
     * Unloaded chunks and client levels report 0.
     *
     * @param level the level to query
     * @param pos   any position inside the chunk
     * @return the current pressure, from 0 to 1
     */
    public static float getEcologicalPressure(Level level, BlockPos pos) {
        return bindingOrThrow().getEcologicalPressure(level, pos);
    }

    /**
     * Returns whether the position counts as tainted land: its biome is tagged
     * {@code thaumaturge:is_tainted}, or its chunk's pressure has reached the tainted threshold.
     * Client levels report {@code false}.
     *
     * @param level the level to query
     * @param pos   the position to test
     * @return {@code true} when the position is tainted
     */
    public static boolean isTainted(Level level, BlockPos pos) {
        return bindingOrThrow().isTainted(level, pos);
    }

    /**
     * Returns whether an active taint source, currently a live taint seed, is close enough to
     * the position to drive spread there.
     *
     * @param level the level to query
     * @param pos   the position to test
     * @return {@code true} when an active source is in range
     */
    public static boolean hasActiveSource(Level level, BlockPos pos) {
        return bindingOrThrow().hasActiveSource(level, pos);
    }

    /**
     * Adds ecological taint pressure to the chunk containing the position, capped at 1. Does
     * nothing in wuss mode or when the chunk is not loaded.
     *
     * @param level  the level to mutate
     * @param pos    any position inside the chunk
     * @param amount the pressure to add; zero or negative amounts are ignored
     */
    public static void addEcologicalPressure(ServerLevel level, BlockPos pos, float amount) {
        bindingOrThrow().addEcologicalPressure(level, pos, amount);
    }

    /**
     * Removes ecological taint pressure from the chunk containing the position, down to 0. The
     * stored pressure is discarded once it reaches 0. Does nothing when the chunk is not loaded.
     *
     * @param level  the level to mutate
     * @param pos    any position inside the chunk
     * @param amount the pressure to remove; zero or negative amounts are ignored
     */
    public static void cleanEcologicalPressure(ServerLevel level, BlockPos pos, float amount) {
        bindingOrThrow().cleanEcologicalPressure(level, pos, amount);
    }

    /**
     * Binds the facade's implementation. Called once at mod init by the implementation; addons
     * must not call this.
     *
     * @param bindings the implementation
     * @throws IllegalStateException when already bound
     */
    public static void bind(Bindings bindings) {
        if (impl != null) {
            throw new IllegalStateException("TaintApi already bound");
        }
        impl = bindings;
    }

    private static Bindings bindingOrThrow() {
        if (impl == null) {
            throw new IllegalStateException("TaintApi accessed before binding");
        }
        return impl;
    }

    /**
     * Implementation hook supplied by Thaumaturge at mod init. Each method on this interface
     * corresponds to a public static on {@link TaintApi}. Addons must not implement this
     * interface.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        void addTaintSeed(ServerLevel level, BlockPos pos);

        void removeTaintSeed(ServerLevel level, BlockPos pos);

        boolean isNearTaintSeed(Level level, BlockPos pos);

        boolean isAtTaintSeedEdge(Level level, BlockPos pos);

        void spreadFibres(ServerLevel level, BlockPos pos, boolean force);

        float getEcologicalPressure(Level level, BlockPos pos);

        boolean isTainted(Level level, BlockPos pos);

        boolean hasActiveSource(Level level, BlockPos pos);

        void addEcologicalPressure(ServerLevel level, BlockPos pos, float amount);

        void cleanEcologicalPressure(ServerLevel level, BlockPos pos, float amount);
    }
}
