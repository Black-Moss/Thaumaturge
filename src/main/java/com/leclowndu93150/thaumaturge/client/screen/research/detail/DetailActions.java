package com.leclowndu93150.thaumaturge.client.screen.research.detail;

import net.minecraft.resources.Identifier;

public interface DetailActions {
    void turnPageBack();

    void turnPageForward();

    void navigateBack();

    void showStage(int stageIndex);

    void toggleAspectsInsert();

    void toggleKnowledgeInsert();

    void turnAspectsPage(int delta);

    void turnRecipeVariant(int delta);

    void toggleRecipeInsert(Identifier recipeId);

    void toggleConstructInsert();

    void obtainNote(int ordinal);

    void submitStageCompletion();
}
