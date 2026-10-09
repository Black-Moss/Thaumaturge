package com.leclowndu93150.thaumaturge.api.golems.seals;

/**
 * The pages of the seal configuration screen.
 *
 * @apiNote the ordinals are written into menu transfer data and select the lang keys
 *          {@code gui.thaumaturge.seal.category.<ordinal>} and {@code gui.thaumaturge.seal.category.<ordinal>.desc}.
 *          Constants are never reordered or inserted.
 * @since 1.0.0
 */
public enum SealPanel {
    /** Priority, golem colour, locking and redstone control. */
    PRIORITY,
    /** The item filter. */
    FILTER,
    /** The size of the work area. */
    AREA,
    /** The behaviour options of the seal. */
    TOGGLES,
    /** The traits a golem must have or must lack. */
    TAGS
}
