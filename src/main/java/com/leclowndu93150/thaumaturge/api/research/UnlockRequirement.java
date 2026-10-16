package com.leclowndu93150.thaumaturge.api.research;

import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/**
 * An extra requirement a player must meet before a research entry can be unlocked, registered with
 * {@link ResearchUnlockConditions#require}.
 *
 * @implNote Requirements are tested on the server when an entry is unlocked, when sibling entries are completed
 * automatically and when entries unlock on login, and on the client when the Thaumonomicon decides what to show. They
 * must only read state and must be cheap.
 * @since 1.1.0
 */
@FunctionalInterface
public interface UnlockRequirement {
    /**
     * Tests whether the player meets this requirement.
     *
     * @param player the player, on either logical side
     * @param research the player's research progress
     * @return {@code true} when the player may unlock the entry as far as this requirement is concerned
     * @since 1.1.0
     */
    boolean isMet(Player player, PlayerResearch research);

    /**
     * Returns a requirement met once the player has completed an entry.
     *
     * @param entry the research entry that must be complete
     * @return the requirement
     * @since 1.1.0
     */
    static UnlockRequirement completed(Identifier entry) {
        return (player, research) -> research.hasCompleted(entry);
    }

    /**
     * Returns a requirement met only when both this one and another are met. The other one is not tested when this one
     * already fails.
     *
     * @param other the second requirement
     * @return the combined requirement
     * @since 1.1.0
     */
    default UnlockRequirement and(UnlockRequirement other) {
        return (player, research) -> isMet(player, research) && other.isMet(player, research);
    }
}
