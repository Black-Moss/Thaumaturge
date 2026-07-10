package com.leclowndu93150.thaumcraft.api.research;

import java.util.List;
import net.minecraft.resources.Identifier;

/**
 * A single step within an {@link IResearchEntry}. Stages list the recipes shown, the things the
 * player must obtain or craft, the knowledge or research rewards required to advance, and any
 * warp inflicted on completion.
 *
 * @since 1.0.0
 */
public interface IResearchStage {
    /**
     * Translation key for the stage body text.
     *
     * @return the translation key
     */
    String textKey();

    /**
     * Recipe identifiers displayed in this stage's UI panel.
     *
     * @return the recipe identifiers, never null
     */
    List<Identifier> recipes();

    /**
     * Items the player must collect to advance.
     *
     * @return the collection requirements, never null
     */
    List<ResearchRequirement> obtain();

    /**
     * Items the player must craft to advance.
     *
     * @return the crafting requirements, never null
     */
    List<ResearchRequirement> craft();

    /**
     * Knowledge rewards granted when this stage completes.
     *
     * @return the knowledge rewards, never null
     */
    List<KnowledgeReward> knowledge();

    /**
     * Knowledge the player must hold to advance past this stage. The listed amounts are consumed
     * when the stage completes.
     *
     * @return the knowledge costs, never null
     */
    default List<KnowledgeReward> requiredKnowledge() {
        return List.of();
    }

    /**
     * Other research entries required to be complete before this stage will advance.
     *
     * @return the prerequisite research identifiers, never null
     */
    List<Identifier> requiredResearch();

    /**
     * Amount of warp inflicted on the player when this stage completes.
     *
     * @return the warp amount, never negative
     */
    int warp();
}
