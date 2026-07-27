package com.leclowndu93150.thaumaturge.api.warp;

/**
 * The three warp pools a player accumulates.
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
