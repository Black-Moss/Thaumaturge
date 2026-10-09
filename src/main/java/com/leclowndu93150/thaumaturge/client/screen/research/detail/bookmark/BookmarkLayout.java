package com.leclowndu93150.thaumaturge.client.screen.research.detail.bookmark;

import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.DetailFrame;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.EntryDetailModel;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.Rect;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;

public final class BookmarkLayout {
    public static final int SIDE_WIDTH = 25;
    public static final int SIDE_HEIGHT = 16;

    private static final int SIDE_OFFSET_X = -48;
    private static final int ASPECT_HOVER_Y = 9;
    private static final int ASPECT_CLICK_Y = 8;
    private static final int KNOWLEDGE_HOVER_Y = 32;
    private static final int KNOWLEDGE_CLICK_Y = 31;
    private static final int RECIPE_OFFSET_X = 280;
    private static final int RECIPE_FIRST_Y = -8;
    private static final int RECIPE_MAX_STEP = 25;
    private static final int RECIPE_TOTAL_BUDGET = 200;
    private static final int RECIPE_WIDTH = 30;
    private static final int RECIPE_HEIGHT = 16;
    private static final int RECIPE_TOP_INSET = 1;

    private BookmarkLayout() {}

    public static List<BookmarkSlot> slots(DetailFrame frame, EntryDetailModel model, IResearchStage stage) {
        List<BookmarkSlot> slots = new ArrayList<>();
        if (EntryDetailModel.knowsResearch(frame.player(), EntryDetailModel.FIRST_STEPS_RESEARCH)) {
            slots.add(sideSlot(BookmarkKind.ASPECTS, frame, ASPECT_HOVER_Y, ASPECT_CLICK_Y));
        }
        if (EntryDetailModel.knowsResearch(frame.player(), EntryDetailModel.KNOWLEDGE_TYPES_RESEARCH) && !model.isKnowledgeTypesEntry()) {
            slots.add(sideSlot(BookmarkKind.KNOWLEDGE, frame, KNOWLEDGE_HOVER_Y, KNOWLEDGE_CLICK_Y));
        }
        List<Identifier> recipes = model.displayRecipes(stage, frame.player());
        boolean hasConstruct = stage.construct().isPresent();
        int total = recipes.size() + (hasConstruct ? 1 : 0);
        if (total == 0) {
            return slots;
        }
        int step = Math.min(RECIPE_MAX_STEP, RECIPE_TOTAL_BUDGET / total);
        for (int ordinal = 0; ordinal < total; ordinal++) {
            boolean constructSlot = ordinal == recipes.size();
            Identifier recipeId = constructSlot ? null : recipes.get(ordinal);
            Rect area = new Rect(frame.left() + RECIPE_OFFSET_X, frame.top() + RECIPE_FIRST_Y + step * ordinal - RECIPE_TOP_INSET, RECIPE_WIDTH, RECIPE_HEIGHT);
            slots.add(new BookmarkSlot(constructSlot ? BookmarkKind.CONSTRUCT : BookmarkKind.RECIPE, recipeId, ordinal, area, area));
        }
        return slots;
    }

    public static List<BookmarkSlot> ofKind(List<BookmarkSlot> slots, BookmarkKind kind) {
        return slots.stream().filter(slot -> slot.kind() == kind).toList();
    }

    private static BookmarkSlot sideSlot(BookmarkKind kind, DetailFrame frame, int hoverY, int clickY) {
        int x = frame.left() + SIDE_OFFSET_X;
        return new BookmarkSlot(kind, null, -1, new Rect(x, frame.top() + hoverY, SIDE_WIDTH, SIDE_HEIGHT), new Rect(x, frame.top() + clickY, SIDE_WIDTH, SIDE_HEIGHT));
    }
}
