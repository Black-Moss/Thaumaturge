package com.leclowndu93150.thaumaturge.api.capability;

import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.AttachmentType;

/**
 * Per-player record of completed research, in-progress stages, per-entry flags, and accumulated
 * category knowledge.
 *
 * <p>The instance lives on the player as a NeoForge {@link AttachmentType data attachment}
 * and is the authoritative source for research progression. The server owns the value; clients
 * receive a copy on login, on dimension change, and whenever the server publishes a change.
 *
 * <p>All mutating methods are server-side; they return {@code true} when the call produced an
 * observable change, {@code false} otherwise. Callers that mutate must invoke {@link #sync(ServerPlayer)}
 * to publish the new state to the owner.
 *
 * <p>Research is identified by {@link Identifier}. Stage gating uses {@link
 * #researchStage(Identifier)} queries; see {@link #isResearchKnown(Identifier, int)}.
 *
 * <p>Since 1.1.0 this interface extends {@link PlayerResearch} and answers each of its methods through the matching
 * method here, so an object written against 1.0.0 is a {@link PlayerResearch} as well. Thaumaturge's own record
 * implements {@link PlayerResearch} only; {@link KnowledgeAccess#of} and {@link #viewOf} wrap it in a view that
 * implements this interface.
 *
 * @since 1.0.0
 * @deprecated use {@link PlayerResearch}, obtained from {@link PlayerResearchAccess#of}; this interface is removed in
 *     2.0.0.
 */
@Deprecated(since = "1.1.0", forRemoval = true)
public interface IPlayerKnowledge extends PlayerResearch {
    /**
     * Removes every recorded research, stage, flag, and knowledge counter.
     * @deprecated use {@link PlayerResearch#wipe}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    void clear();

    /**
     * Returns the current status of a given research entry.
     *
     * @param research the research entry identifier; must not be null
     * @return the status, never null; {@link ResearchStatus#UNKNOWN} when the entry has not been added
     * @deprecated use {@link PlayerResearch#status}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    ResearchStatus researchStatus(Identifier research);

    /**
     * Returns whether the entry is known and every stage is complete.
     *
     * @param research the research entry identifier
     * @return {@code true} when the entry is complete
     * @deprecated use {@link PlayerResearch#hasCompleted}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    boolean isResearchComplete(Identifier research);

    /**
     * Returns whether the entry has been added to this player's record.
     *
     * @param research the research entry identifier
     * @return {@code true} when the entry is known regardless of stage
     * @deprecated use {@link PlayerResearch#knows}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    boolean isResearchKnown(Identifier research);

    /**
     * Returns whether the entry is known and the player has reached at least the given stage.
     *
     * @param research the research entry identifier
     * @param minimumStage the minimum stage required
     * @return {@code true} when the entry is known and the player is at or beyond {@code minimumStage}
     * @deprecated use {@link PlayerResearch#hasReachedStage}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    boolean isResearchKnown(Identifier research, int minimumStage);

    /**
     * Returns the current stage of the given research, or {@code -1} when the entry is not known.
     *
     * @param research the research entry identifier
     * @return the stage index, or {@code -1} when unknown
     * @deprecated use {@link PlayerResearch#stageOf}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    int researchStage(Identifier research);

    /**
     * Adds the research to the player's record. Has no effect when already added.
     *
     * @param research the research entry identifier
     * @return {@code true} when the entry was newly added
     * @deprecated use {@link PlayerResearch#learn}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    boolean addResearch(Identifier research);

    /**
     * Sets the current stage of the given research. Has no effect when the entry is not known or
     * when {@code stage} is non-positive.
     *
     * @param research the research entry identifier
     * @param stage the new stage; must be positive
     * @return {@code true} when the stored stage changed
     * @deprecated use {@link PlayerResearch#setStage}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    boolean setResearchStage(Identifier research, int stage);

    /**
     * Removes the research and any associated stage or flags from the player's record.
     *
     * @param research the research entry identifier
     * @return {@code true} when an entry was removed
     * @deprecated use {@link PlayerResearch#forget}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    boolean removeResearch(Identifier research);

    /**
     * The unmodifiable set of every research entry currently in the player's record.
     *
     * @return an unmodifiable set, never null
     * @deprecated use {@link PlayerResearch#knownEntries}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    Set<Identifier> researchList();

    /**
     * Adds a flag to the given research entry.
     *
     * @param research the research entry identifier
     * @param flag the flag to set
     * @return {@code true} when the flag was newly set
     * @deprecated use {@link PlayerResearch#addFlag}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    boolean setResearchFlag(Identifier research, ResearchFlag flag);

    /**
     * Removes a flag from the given research entry.
     *
     * @param research the research entry identifier
     * @param flag the flag to clear
     * @return {@code true} when the flag was present and removed
     * @deprecated use {@link PlayerResearch#removeFlag}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    boolean clearResearchFlag(Identifier research, ResearchFlag flag);

    /**
     * Returns whether the given research entry has the given flag set.
     *
     * @param research the research entry identifier
     * @param flag the flag to test
     * @return {@code true} when set
     * @deprecated use {@link PlayerResearch#hasFlag}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    boolean hasResearchFlag(Identifier research, ResearchFlag flag);

    /**
     * Adds raw progress for the given knowledge type and category. The raw counter is scaled by
     * {@link KnowledgeType#progression()} when read through {@link #knowledge(KnowledgeType, ResourceKey)}.
     * Negative {@code amount} is permitted; the counter clamps at zero.
     *
     * @param type the knowledge type
     * @param category the category key; may be the synthetic global category when {@link KnowledgeType}
     *                 does not partition by category
     * @param amount the raw amount to add, may be negative
     * @return {@code true} when the stored counter changed
     * @deprecated use {@link PlayerResearch#addKnowledgePoints}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    boolean addKnowledge(KnowledgeType type, ResourceKey<IResearchCategory> category, int amount);

    /**
     * Returns the visible level of accumulated category knowledge, computed as
     * {@code rawKnowledge / type.progression()}.
     *
     * @param type the knowledge type
     * @param category the category key
     * @return the visible level, never negative
     * @deprecated use {@link PlayerResearch#knowledgeLevel}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    int knowledge(KnowledgeType type, ResourceKey<IResearchCategory> category);

    /**
     * Returns the raw accumulated counter for the given category and knowledge type before scaling.
     *
     * @param type the knowledge type
     * @param category the category key
     * @return the raw counter, never negative
     * @deprecated use {@link PlayerResearch#knowledgePoints}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    int rawKnowledge(KnowledgeType type, ResourceKey<IResearchCategory> category);

    /**
     * Publishes this record to the given player. Call after any mutation that must be reflected
     * in the client's mirror.
     *
     * @implNote Since 1.1.0 the update is sent once, to that player only, at the end of the player's
     *           tick, so several calls in one tick produce a single packet.
     *
     * @param player the player to sync; must not be null
     * @deprecated use {@link PlayerResearch#queueSync}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    void sync(ServerPlayer player);

    /**
     * Returns research progress as this interface. Progress that already implements it is returned as it is; any
     * other progress is wrapped in a view whose methods forward to it.
     *
     * @param research the progress to view
     * @return the progress as this interface, never {@code null}
     * @since 1.1.0
     * @deprecated exists only to hand progress to code written against 1.0.0; use {@link PlayerResearch} directly
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    static IPlayerKnowledge viewOf(PlayerResearch research) {
        return research instanceof IPlayerKnowledge legacy ? legacy : new LegacyKnowledgeView(research);
    }

    /**
     * Answers {@link PlayerResearch#status} through {@link #researchStatus}.
     *
     * @param entry see {@link PlayerResearch#status}
     * @return the result of {@link #researchStatus}
     * @since 1.1.0
     */
    @Override
    default ResearchStatus status(Identifier entry) {
        return researchStatus(entry);
    }

    /**
     * Answers {@link PlayerResearch#knows} through {@link #isResearchKnown(Identifier)}.
     *
     * @param entry see {@link PlayerResearch#knows}
     * @return the result of {@link #isResearchKnown(Identifier)}
     * @since 1.1.0
     */
    @Override
    default boolean knows(Identifier entry) {
        return isResearchKnown(entry);
    }

    /**
     * Answers {@link PlayerResearch#hasCompleted} through {@link #isResearchComplete}.
     *
     * @param entry see {@link PlayerResearch#hasCompleted}
     * @return the result of {@link #isResearchComplete}
     * @since 1.1.0
     */
    @Override
    default boolean hasCompleted(Identifier entry) {
        return isResearchComplete(entry);
    }

    /**
     * Answers {@link PlayerResearch#hasReachedStage} through {@link #isResearchKnown(Identifier, int)}.
     *
     * @param entry see {@link PlayerResearch#hasReachedStage}
     * @param stage see {@link PlayerResearch#hasReachedStage}
     * @return the result of {@link #isResearchKnown(Identifier, int)}
     * @since 1.1.0
     */
    @Override
    default boolean hasReachedStage(Identifier entry, int stage) {
        return isResearchKnown(entry, stage);
    }

    /**
     * Answers {@link PlayerResearch#stageOf} through {@link #researchStage}.
     *
     * @param entry see {@link PlayerResearch#stageOf}
     * @return the result of {@link #researchStage}
     * @since 1.1.0
     */
    @Override
    default int stageOf(Identifier entry) {
        return researchStage(entry);
    }

    /**
     * Answers {@link PlayerResearch#knownEntries} through {@link #researchList}.
     *
     * @return the result of {@link #researchList}
     * @since 1.1.0
     */
    @Override
    default Set<Identifier> knownEntries() {
        return researchList();
    }

    /**
     * Answers {@link PlayerResearch#learn} through {@link #addResearch}.
     *
     * @param entry see {@link PlayerResearch#learn}
     * @return the result of {@link #addResearch}
     * @since 1.1.0
     */
    @Override
    default boolean learn(Identifier entry) {
        return addResearch(entry);
    }

    /**
     * Answers {@link PlayerResearch#setStage} through {@link #setResearchStage}.
     *
     * @param entry see {@link PlayerResearch#setStage}
     * @param stage see {@link PlayerResearch#setStage}
     * @return the result of {@link #setResearchStage}
     * @since 1.1.0
     */
    @Override
    default boolean setStage(Identifier entry, int stage) {
        return setResearchStage(entry, stage);
    }

    /**
     * Answers {@link PlayerResearch#forget} through {@link #removeResearch}.
     *
     * @param entry see {@link PlayerResearch#forget}
     * @return the result of {@link #removeResearch}
     * @since 1.1.0
     */
    @Override
    default boolean forget(Identifier entry) {
        return removeResearch(entry);
    }

    /**
     * Answers {@link PlayerResearch#hasFlag} through {@link #hasResearchFlag}.
     *
     * @param entry see {@link PlayerResearch#hasFlag}
     * @param flag see {@link PlayerResearch#hasFlag}
     * @return the result of {@link #hasResearchFlag}
     * @since 1.1.0
     */
    @Override
    default boolean hasFlag(Identifier entry, ResearchFlag flag) {
        return hasResearchFlag(entry, flag);
    }

    /**
     * Answers {@link PlayerResearch#addFlag} through {@link #setResearchFlag}.
     *
     * @param entry see {@link PlayerResearch#addFlag}
     * @param flag see {@link PlayerResearch#addFlag}
     * @return the result of {@link #setResearchFlag}
     * @since 1.1.0
     */
    @Override
    default boolean addFlag(Identifier entry, ResearchFlag flag) {
        return setResearchFlag(entry, flag);
    }

    /**
     * Answers {@link PlayerResearch#removeFlag} through {@link #clearResearchFlag}.
     *
     * @param entry see {@link PlayerResearch#removeFlag}
     * @param flag see {@link PlayerResearch#removeFlag}
     * @return the result of {@link #clearResearchFlag}
     * @since 1.1.0
     */
    @Override
    default boolean removeFlag(Identifier entry, ResearchFlag flag) {
        return clearResearchFlag(entry, flag);
    }

    /**
     * Answers {@link PlayerResearch#knowledgeLevel} through {@link #knowledge}.
     *
     * @param type see {@link PlayerResearch#knowledgeLevel}
     * @param category see {@link PlayerResearch#knowledgeLevel}
     * @return the result of {@link #knowledge}
     * @since 1.1.0
     */
    @Override
    default int knowledgeLevel(KnowledgeType type, ResourceKey<IResearchCategory> category) {
        return knowledge(type, category);
    }

    /**
     * Answers {@link PlayerResearch#knowledgePoints} through {@link #rawKnowledge}.
     *
     * @param type see {@link PlayerResearch#knowledgePoints}
     * @param category see {@link PlayerResearch#knowledgePoints}
     * @return the result of {@link #rawKnowledge}
     * @since 1.1.0
     */
    @Override
    default int knowledgePoints(KnowledgeType type, ResourceKey<IResearchCategory> category) {
        return rawKnowledge(type, category);
    }

    /**
     * Answers {@link PlayerResearch#addKnowledgePoints} through {@link #addKnowledge}.
     *
     * @param type see {@link PlayerResearch#addKnowledgePoints}
     * @param category see {@link PlayerResearch#addKnowledgePoints}
     * @param amount see {@link PlayerResearch#addKnowledgePoints}
     * @return the result of {@link #addKnowledge}
     * @since 1.1.0
     */
    @Override
    default boolean addKnowledgePoints(KnowledgeType type, ResourceKey<IResearchCategory> category, int amount) {
        return addKnowledge(type, category, amount);
    }

    /**
     * Answers {@link PlayerResearch#wipe} through {@link #clear}.
     *
     * @since 1.1.0
     */
    @Override
    default void wipe() {
        clear();
    }

    /**
     * Answers {@link PlayerResearch#queueSync} through {@link #sync}.
     *
     * @param owner see {@link PlayerResearch#queueSync}
     * @since 1.1.0
     */
    @Override
    default void queueSync(ServerPlayer owner) {
        sync(owner);
    }
}
