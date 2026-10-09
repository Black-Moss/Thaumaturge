package com.leclowndu93150.thaumaturge.client.screen.research.detail.requirement;

import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.KnowledgeReward;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.Rect;
import java.util.List;

public record RequirementLayout(boolean hasResearch, boolean hasObtain, boolean hasCraft, boolean hasKnowledge, int researchY, int obtainY, int craftY, int knowledgeY, int dividerY) {
    public static final int SLOT_FIRST_SHIFT = 24;
    public static final int SLOT_DEFAULT_SPACING = 18;
    public static final int SLOT_CROWDED_BUDGET = 110;
    public static final int SLOT_CROWDED_THRESHOLD = 6;
    public static final int SLOT_INNER_OFFSET_X = -15;
    public static final int COMPLETE_BUTTON_OFFSET_X = 20;
    public static final int COMPLETE_BUTTON_Y_OFFSET = -6;
    public static final int COMPLETE_BUTTON_WIDTH = 64;
    public static final int COMPLETE_BUTTON_HEIGHT = 12;

    private static final int BOTTOM_Y_OFFSET = 210 - 25;
    private static final int ROW_STEP = 18;
    private static final int DIVIDER_GAP = 12;

    public static RequirementLayout of(IResearchStage stage, int top) {
        int cursorY = top + BOTTOM_Y_OFFSET;
        boolean research = !stage.requiredResearch().isEmpty();
        boolean obtain = !stage.obtain().isEmpty();
        boolean craft = !stage.craft().isEmpty();
        boolean knowledge = !stage.requiredKnowledge().isEmpty();
        int researchY = research ? (cursorY -= ROW_STEP) : cursorY;
        int obtainY = obtain ? (cursorY -= ROW_STEP) : cursorY;
        int craftY = craft ? (cursorY -= ROW_STEP) : cursorY;
        int knowledgeY = knowledge ? (cursorY -= ROW_STEP) : cursorY;
        return new RequirementLayout(research, obtain, craft, knowledge, researchY, obtainY, craftY, knowledgeY, cursorY - DIVIDER_GAP);
    }

    public static int spacingFor(int slotCount) {
        return slotCount > SLOT_CROWDED_THRESHOLD ? SLOT_CROWDED_BUDGET / slotCount : SLOT_DEFAULT_SPACING;
    }

    public static int slotX(int left, int index, int spacing) {
        return left + SLOT_INNER_OFFSET_X + SLOT_FIRST_SHIFT + index * spacing;
    }

    public static int[] knowledgeSlotXs(int left, List<KnowledgeReward> rewards, int spacing, int observationChips) {
        int[] positions = new int[rewards.size()];
        int innerLeft = left + SLOT_INNER_OFFSET_X;
        int offset = SLOT_FIRST_SHIFT;
        boolean observationPlaced = false;
        for (int index = 0; index < rewards.size(); index++) {
            positions[index] = innerLeft + offset;
            if (rewards.get(index).type() != KnowledgeType.THEORY) {
                if (observationPlaced) {
                    continue;
                }
                observationPlaced = true;
                if (observationChips > 1) {
                    offset += (observationChips - 1) * spacing;
                }
            }
            offset += spacing;
        }
        return positions;
    }

    public boolean any() {
        return hasResearch || hasObtain || hasCraft || hasKnowledge;
    }

    public Rect completeButton(int left) {
        return new Rect(left + COMPLETE_BUTTON_OFFSET_X, dividerY + COMPLETE_BUTTON_Y_OFFSET, COMPLETE_BUTTON_WIDTH, COMPLETE_BUTTON_HEIGHT);
    }
}
