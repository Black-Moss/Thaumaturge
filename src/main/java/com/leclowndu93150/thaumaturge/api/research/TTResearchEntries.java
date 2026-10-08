package com.leclowndu93150.thaumaturge.api.research;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.resources.Identifier;

/**
 * Identifiers of research entries that gameplay systems reference directly.
 *
 * @since 1.0.0
 */
public final class TTResearchEntries {
    /** Unlocks celestial observation through the thaumometer. */
    public static final Identifier CELESTIAL_SCANNING = TTIds.rl("celestial_scanning");

    /** Granted when the thaumometer detects dangerous flux levels in the local aura. */
    public static final Identifier FLUX = TTIds.rl("flux");

    private TTResearchEntries() {}
}
