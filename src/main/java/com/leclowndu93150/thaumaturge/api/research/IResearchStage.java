package com.leclowndu93150.thaumaturge.api.research;

import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;

/**
 * One step of a research entry.
 *
 * <p>Implementations are immutable. Every list-returning member is non-null and an empty list
 * means none. Stages complete in order once every applicable requirement holds.
 *
 * @since 1.0.0
 */
public interface IResearchStage {
    /**
     * The translation key of the stage body text.
     *
     * @return the text key
     */
    String textKey();

    /**
     * The recipe identifiers shown in the right-hand panel of the stage.
     *
     * @return the recipe identifiers, possibly empty
     */
    List<Identifier> recipes();

    /**
     * The item requirements consumed from the player inventory when the stage completes.
     *
     * @return the obtain requirements, possibly empty
     */
    List<ResearchRequirement> obtain();

    /**
     * The item requirements satisfied by a previous craft or by holding at least one matching item.
     *
     * @return the craft requirements, possibly empty
     */
    List<ResearchRequirement> craft();

    /**
     * The knowledge granted to the player when the stage completes.
     *
     * @return the knowledge rewards, possibly empty
     */
    List<KnowledgeReward> knowledge();

    /**
     * The knowledge the player must pay or hold for the stage to complete.
     *
     * @return the knowledge costs, empty by default
     */
    default List<KnowledgeReward> requiredKnowledge() {
        return List.of();
    }

    /**
     * The optional multiblock diagram shown with its activation vis cost.
     *
     * @return the construct, empty by default
     */
    default Optional<ResearchConstruct> construct() {
        return Optional.empty();
    }

    /**
     * The research entries that must be complete before the stage advances.
     *
     * @return the prerequisite entry identifiers, possibly empty
     */
    List<Identifier> requiredResearch();

    /**
     * The warp inflicted when the stage completes.
     *
     * @return the warp amount, zero or higher
     */
    int warp();
}
