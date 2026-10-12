package com.leclowndu93150.thaumaturge.api.capability;

import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

/**
 * One player's research progress: which research entries they know, how far they are through each, the interface flags
 * on each entry, and the theory and observation points they have gathered per research category.
 *
 * <p>Obtain it with {@link PlayerResearchAccess#of}. The server holds the authoritative copy, stored with the player
 * and kept through death. The owning client receives a copy on login, on dimension change and after
 * {@link #queueSync}; other players never receive it. On the client the copy is read-only in practice, since changes
 * made there are never sent back.
 *
 * <p>Methods that change progress return {@code true} when the call changed something and {@code false} when it had no
 * effect. Changes are not published on their own: a caller that changes progress on the server calls
 * {@link #queueSync} afterwards.
 *
 * <p>An entry is in one of three {@link ResearchStatus states}: unknown, known and in progress at some stage, or
 * complete. Learning an entry makes it known; completing it is decided by the research system as the player finishes
 * its stages, and has no method here.
 *
 * @apiNote Only Thaumaturge implements this interface. Addons call it and must not implement it; members may be added
 * in minor releases.
 * @since 1.1.0
 */
public interface PlayerResearch {
    /**
     * Returns the state of one research entry.
     *
     * @param entry the research entry id
     * @return the state, never {@code null}; {@link ResearchStatus#UNKNOWN} when the player has not learnt the entry
     * @since 1.1.0
     */
    ResearchStatus status(Identifier entry);

    /**
     * Tests whether the player has learnt an entry, whatever its stage.
     *
     * @param entry the research entry id
     * @return {@code true} when the entry is known or complete
     * @since 1.1.0
     */
    boolean knows(Identifier entry);

    /**
     * Tests whether the player has completed an entry.
     *
     * @param entry the research entry id
     * @return {@code true} when every stage of the entry is finished
     * @since 1.1.0
     */
    boolean hasCompleted(Identifier entry);

    /**
     * Tests whether the player knows an entry and has reached at least a given stage of it.
     *
     * @param entry the research entry id
     * @param stage the lowest stage that passes
     * @return {@code true} when the entry is known and {@link #stageOf} is at least {@code stage}
     * @since 1.1.0
     */
    boolean hasReachedStage(Identifier entry, int stage);

    /**
     * Returns the stage the player has reached in an entry.
     *
     * @param entry the research entry id
     * @return the stage, {@code 0} for a known entry whose stage was never set, or {@code -1} when the entry is unknown
     * @since 1.1.0
     */
    int stageOf(Identifier entry);

    /**
     * Returns every entry the player knows, complete or not.
     *
     * @return a read-only live view of the known entry ids, never {@code null}
     * @since 1.1.0
     */
    Set<Identifier> knownEntries();

    /**
     * Makes an entry known to the player, without completing it or setting a stage. This bypasses the entry's parents
     * and unlock conditions and fires no research event.
     *
     * @param entry the research entry id
     * @return {@code true} when the entry was not known before
     * @since 1.1.0
     */
    boolean learn(Identifier entry);

    /**
     * Sets the stage the player has reached in a known entry.
     *
     * @param entry the research entry id
     * @param stage the new stage; must be positive
     * @return {@code true} when the stored stage changed; {@code false} when the entry is unknown, the stage is not
     *     positive, or the stage was already set to that value
     * @since 1.1.0
     */
    boolean setStage(Identifier entry, int stage);

    /**
     * Removes an entry from the player's progress, together with its stage, completion and flags.
     *
     * @param entry the research entry id
     * @return {@code true} when the entry was known
     * @since 1.1.0
     */
    boolean forget(Identifier entry);

    /**
     * Tests whether an interface flag is set on an entry.
     *
     * @param entry the research entry id
     * @param flag the flag to test
     * @return {@code true} when the flag is set
     * @since 1.1.0
     */
    boolean hasFlag(Identifier entry, ResearchFlag flag);

    /**
     * Sets an interface flag on an entry. The entry does not have to be known.
     *
     * @param entry the research entry id
     * @param flag the flag to set
     * @return {@code true} when the flag was not set before
     * @since 1.1.0
     */
    boolean addFlag(Identifier entry, ResearchFlag flag);

    /**
     * Clears an interface flag from an entry.
     *
     * @param entry the research entry id
     * @param flag the flag to clear
     * @return {@code true} when the flag was set before
     * @since 1.1.0
     */
    boolean removeFlag(Identifier entry, ResearchFlag flag);

    /**
     * Returns the level of one kind of knowledge in a category, which is the point count divided by
     * {@link KnowledgeType#progression()}, rounded down.
     *
     * @param type the kind of knowledge
     * @param category the research category
     * @return the level, never negative
     * @since 1.1.0
     */
    int knowledgeLevel(KnowledgeType type, ResourceKey<IResearchCategory> category);

    /**
     * Returns the raw point count of one kind of knowledge in a category.
     *
     * @param type the kind of knowledge
     * @param category the research category
     * @return the point count, never negative
     * @since 1.1.0
     */
    int knowledgePoints(KnowledgeType type, ResourceKey<IResearchCategory> category);

    /**
     * Adds raw points of one kind of knowledge in a category. A negative amount takes points away; the count never
     * drops below zero. No knowledge gain notification is shown.
     *
     * @param type the kind of knowledge
     * @param category the research category
     * @param amount the points to add, possibly negative
     * @return {@code true} when the stored count changed
     * @since 1.1.0
     */
    boolean addKnowledgePoints(KnowledgeType type, ResourceKey<IResearchCategory> category, int amount);

    /**
     * Removes all progress: every entry, stage, completion, flag and knowledge point.
     *
     * @since 1.1.0
     */
    void wipe();

    /**
     * Schedules this progress to be sent to its owner. The copy is sent once, at the end of the owner's next tick, so
     * several calls in one tick send a single update.
     *
     * @param owner the player this progress belongs to
     * @since 1.1.0
     */
    void queueSync(ServerPlayer owner);
}
