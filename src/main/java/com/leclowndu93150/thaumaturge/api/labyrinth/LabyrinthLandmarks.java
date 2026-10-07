package com.leclowndu93150.thaumaturge.api.labyrinth;

import net.minecraft.resources.Identifier;

/**
 * Standard landmark ids recorded from {@link MarkerPhase#LANDMARK} markers. Addons may record their own ids next to these.
 *
 * @since 1.0.0
 */
public final class LabyrinthLandmarks {
    /**
     * Where players appear when they enter the maze.
     */
    public static final Identifier ARRIVAL = Identifier.fromNamespaceAndPath("thaumaturge", "arrival");
    /**
     * The portal inside the maze that leads back to the origin.
     */
    public static final Identifier ENTRY_PORTAL = Identifier.fromNamespaceAndPath("thaumaturge", "entry_portal");
    /**
     * The key room's reliquary.
     */
    public static final Identifier KEY = Identifier.fromNamespaceAndPath("thaumaturge", "key");
    /**
     * The centre of the boss hall floor.
     */
    public static final Identifier BOSS_CENTER = Identifier.fromNamespaceAndPath("thaumaturge", "boss_center");
    /**
     * The lock beside the boss hall door.
     */
    public static final Identifier BOSS_DOOR = Identifier.fromNamespaceAndPath("thaumaturge", "boss_door");
    /**
     * Where the exit rift opens once the encounter is beaten.
     */
    public static final Identifier EXIT = Identifier.fromNamespaceAndPath("thaumaturge", "exit");
    /**
     * Where the boss reliquary appears once the encounter is beaten.
     */
    public static final Identifier REWARD = Identifier.fromNamespaceAndPath("thaumaturge", "reward");
    /**
     * Path prefix for encounter spawn points in a boss hall. Landmarks named {@code <namespace>:spawn/<n>} are handed to encounters through
     * {@link EncounterContext#spawnPoints()} in id order.
     */
    public static final String SPAWN_PREFIX = "spawn/";

    private LabyrinthLandmarks() {}
}
