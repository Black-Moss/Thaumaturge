package com.leclowndu93150.thaumaturge.api.warp;

/**
 * Names the three independent pools of warp a player holds.
 *
 * <p>The declaration order is {@link #PERMANENT}, {@link #NORMAL}, {@link #TEMPORARY}. Implementations
 * that store one amount per pool index their storage by {@link #ordinal()}, and anything that lists or
 * sums all pools iterates in this order. The constant names are also used to build lang keys and
 * command argument values, so they are stable.
 *
 * <p>The type holds no state, is not serialized by position, and is safe to use from any thread and
 * on either logical side.
 *
 * @since 1.0
 */
public enum WarpType {
    /**
     * Warp that can never be removed by any means.
     */
    PERMANENT,

    /**
     * Warp that persists until cleansed by purification items.
     */
    NORMAL,

    /**
     * Warp that decays by one point on each warp event check.
     */
    TEMPORARY
}
