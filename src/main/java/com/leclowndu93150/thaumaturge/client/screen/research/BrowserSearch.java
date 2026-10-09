package com.leclowndu93150.thaumaturge.client.screen.research;

import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.ResearchRequirement;
import com.leclowndu93150.thaumaturge.client.render.research.EntryIconRenderer;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.TTTooltips;
import com.leclowndu93150.thaumaturge.client.screen.research.BrowserModel.CategoryRef;
import com.leclowndu93150.thaumaturge.client.screen.research.BrowserModel.EntryNode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

final class BrowserSearch {
    private static final int TEX = TTScreenTextures.TEX_SIZE;
    private static final int FIRST_ROW_Y = 33;
    private static final int ROW_PITCH = 10;
    private static final int TEXT_X = 32;
    private static final int ICON_X = 22;
    private static final int ICON_SIZE = 8;
    private static final int HOVER_LEFT = 22;
    private static final int HOVER_RIGHT_OFFSET = 18;
    private static final int HOVER_HEIGHT = 8;
    private static final int RESERVED_LINES = 1;
    private static final int MORE_X = 22;
    private static final int MORE_Y_GAP = 2;
    private static final int MORE_COLOR = 0xFFAAAAAA;
    private static final int RECIPE_ICON_U = 224;
    private static final int RECIPE_ICON_V = 48;
    private static final int SOURCE_ICON_SIZE = 16;
    private static final int CATEGORY_ICON_TINT = 0xCCA8A8A8;
    private static final int RECIPE_STAGE_LOOKAHEAD = 2;

    private final BrowserModel model;
    private final List<Result> results = new ArrayList<>();

    BrowserSearch(BrowserModel model) {
        this.model = model;
    }

    void clear() {
        results.clear();
    }

    Result get(int index) {
        return results.get(index);
    }

    void rebuild(String query) {
        results.clear();
        IPlayerKnowledge knowledge = model.knowledge();
        String needle = query.toLowerCase(Locale.ROOT);
        for (CategoryRef category : model.shownBuiltIn()) {
            addCategory(category, needle);
        }
        for (CategoryRef category : model.shownAddOn()) {
            addCategory(category, needle);
        }
        for (EntryNode node : model.allEntries()) {
            if (!knowledge.isResearchKnown(node.id())) {
                continue;
            }
            add(Kind.ENTRY, Component.translatable(node.value().nameKey()).getString(), node.id(), needle);
            List<IResearchStage> stages = node.value().stages();
            int index = Math.min(stages.size() - 1, knowledge.researchStage(node.id()) + RECIPE_STAGE_LOOKAHEAD);
            if (index < 0 || index >= stages.size()) {
                continue;
            }
            for (ResearchRequirement requirement : stages.get(index).craft()) {
                Optional<Holder<Item>> first = requirement.items().stream().findFirst();
                if (first.isPresent()) {
                    ItemStack stack = new ItemStack(first.get());
                    stack.applyComponents(requirement.components());
                    if (!stack.isEmpty()) {
                        add(Kind.RECIPE, stack.getHoverName().getString(), node.id(), needle);
                    }
                }
            }
        }
        results.sort(Comparator.comparing(Result::text));
    }

    int rowAt(double mx, double my, int viewWidth, int viewHeight) {
        if (mx <= HOVER_LEFT || mx >= viewWidth + HOVER_RIGHT_OFFSET || my < FIRST_ROW_Y) {
            return -1;
        }
        int row = (int) ((my - FIRST_ROW_Y) / ROW_PITCH);
        boolean inside = my < FIRST_ROW_Y + ROW_PITCH * row + HOVER_HEIGHT;
        return inside && row < visibleRows(viewHeight) && row < results.size() ? row : -1;
    }

    void draw(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, int viewWidth, int viewHeight, long tick) {
        int rows = visibleRows(viewHeight);
        int hoveredRow = rowAt(mouseX, mouseY, viewWidth, viewHeight);
        for (int i = 0; i < results.size() && i < rows; i++) {
            Result result = results.get(i);
            int y = FIRST_ROW_Y + ROW_PITCH * i;
            graphics.text(font, result.text(), TEXT_X, y, i == hoveredRow ? result.kind().hoverColor() : result.kind().color(), false);
            drawIcon(graphics, result, y, tick);
        }
        if (results.size() > rows) {
            graphics.text(font, Component.translatable("gui.thaumaturge.thaumonomicon.search_more"), MORE_X, FIRST_ROW_Y + ROW_PITCH * rows + MORE_Y_GAP, MORE_COLOR, false);
        }
    }

    private static int visibleRows(int viewHeight) {
        return Math.max(0, (viewHeight - FIRST_ROW_Y) / ROW_PITCH - RESERVED_LINES);
    }

    private void addCategory(CategoryRef category, String needle) {
        ResourceKey<IResearchCategory> key = ResourceKey.create(IResearchCategory.REGISTRY_KEY, category.id());
        add(Kind.CATEGORY, TTTooltips.categoryName(key).getString(), category.id(), needle);
    }

    private void add(Kind kind, String text, Identifier id, String needle) {
        if (text.toLowerCase(Locale.ROOT).contains(needle)) {
            results.add(new Result(kind, text, id));
        }
    }

    private void drawIcon(GuiGraphicsExtractor graphics, Result result, int y, long tick) {
        switch (result.kind()) {
            case RECIPE -> graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.RESEARCH_BROWSER, ICON_X, y, (float) RECIPE_ICON_U, (float) RECIPE_ICON_V, ICON_SIZE, ICON_SIZE,
                    SOURCE_ICON_SIZE, SOURCE_ICON_SIZE, TEX, TEX);
            case ENTRY -> {
                EntryNode node = model.entry(result.id());
                if (node != null) {
                    float scale = (float) ICON_SIZE / EntryIconRenderer.ICON_REFERENCE_SIZE;
                    graphics.pose().pushMatrix();
                    graphics.pose().translate(ICON_X, y);
                    graphics.pose().scale(scale, scale);
                    EntryIconRenderer.drawResearchIcon(graphics, 0, 0, EntryIconRenderer.resolveIcon(node.value(), tick), false);
                    graphics.pose().popMatrix();
                }
            }
            case CATEGORY -> {
                CategoryRef category = model.category(result.id());
                if (category != null) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, category.data().icon(), ICON_X, y, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, SOURCE_ICON_SIZE, SOURCE_ICON_SIZE, SOURCE_ICON_SIZE,
                            SOURCE_ICON_SIZE, CATEGORY_ICON_TINT);
                }
            }
        }
    }

    enum Kind {
        CATEGORY(0xFFDDAAAA, 0xFFFFCCCC), ENTRY(0xFFDDDDDD, 0xFFFFFFFF), RECIPE(0xFFAAAADD, 0xFFCCCCFF);

        private final int color;
        private final int hoverColor;

        Kind(int color, int hoverColor) {
            this.color = color;
            this.hoverColor = hoverColor;
        }

        int color() {
            return color;
        }

        int hoverColor() {
            return hoverColor;
        }
    }

    record Result(Kind kind, String text, Identifier id) {
    }
}
