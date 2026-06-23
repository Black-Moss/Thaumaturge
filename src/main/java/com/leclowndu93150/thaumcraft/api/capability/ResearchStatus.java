package com.leclowndu93150.thaumcraft.api.capability;

/**
 * Status of a research entry for a particular player.
 *
 * @since 1.0.0
 */
public enum ResearchStatus {
    /** The player has not unlocked this entry. */
    UNKNOWN,
    /** The player has unlocked this entry and finished every stage. */
    COMPLETE,
    /** The player has unlocked this entry but not yet finished every stage. */
    IN_PROGRESS
}
