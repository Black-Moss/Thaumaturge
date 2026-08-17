package com.leclowndu93150.thaumaturge.api.warp;

/**
 * Per-player warp state. Each pool is clamped to {@code [0, 500]} on every mutation. The counter tracks the total warp
 * held at the moment warp was last gained and drives the frequency of warp events.
 *
 * <p>
 * Mutations through this interface change stored state only. Use {@link WarpHelper#addWarp} to apply warp with the full
 * side effects (counter update, client notification, research triggers).
 *
 * @since 1.0
 */
public interface IPlayerWarp {
    /**
     * @param type the warp pool to read
     * @return the current amount in that pool
     */
    int get(WarpType type);

    /**
     * Sets a pool to an absolute value, clamped to {@code [0, 500]}.
     *
     * @param type the warp pool to write
     * @param amount the new value
     */
    void set(WarpType type, int amount);

    /**
     * Adds to a pool, clamping the result to {@code [0, 500]}.
     *
     * @param type the warp pool to modify
     * @param amount the amount to add
     * @return the value stored after the addition
     */
    int add(WarpType type, int amount);

    /**
     * Subtracts from a pool, clamping the result to {@code [0, 500]}.
     *
     * @param type the warp pool to modify
     * @param amount the amount to remove
     * @return the value stored after the reduction
     */
    int reduce(WarpType type, int amount);

    /**
     * @return the warp event counter
     */
    int getCounter();

    /**
     * @param counter the new counter value; not clamped
     */
    void setCounter(int counter);

    /**
     * Resets all pools and the counter to zero.
     */
    void clear();
}
