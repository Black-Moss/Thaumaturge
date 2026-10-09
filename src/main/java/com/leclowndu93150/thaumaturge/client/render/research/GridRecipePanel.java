package com.leclowndu93150.thaumaturge.client.render.research;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

final class GridRecipePanel implements RecipePanel {
    enum Kind {
        WORKBENCH("gui.thaumaturge.recipe_type.workbench", 60, 51, false, false), WORKBENCH_SHAPELESS("gui.thaumaturge.recipe_type.workbench_shapeless", 60, 51, false,
                true), ARCANE("gui.thaumaturge.recipe_type.arcane", 112, 52, true, false), ARCANE_SHAPELESS("gui.thaumaturge.recipe_type.arcane_shapeless", 112, 52, true, true);

        private final String labelKey;
        private final int u;
        private final int width;
        private final boolean arcane;
        private final boolean shapeless;

        Kind(String labelKey, int u, int width, boolean arcane, boolean shapeless) {
            this.labelKey = labelKey;
            this.u = u;
            this.width = width;
            this.arcane = arcane;
            this.shapeless = shapeless;
        }
    }

    private static final int GRID = 3;
    private static final int SHAPELESS_LIMIT = GRID * GRID;
    private static final int SLOT_ORIGIN = -40;
    private static final int SLOT_STRIDE = 32;
    private static final int BACKGROUND_V = 15;
    private static final int BACKGROUND_HEIGHT = 52;
    private static final int BACKGROUND_OFFSET = -26;
    private static final int OVERLAY_U = 68;
    private static final int OVERLAY_V = 76;
    private static final int OVERLAY_SIZE = 12;
    private static final int OVERLAY_X = -6;
    private static final int OVERLAY_Y = 40;
    private static final int OVERLAY_TINT = 0x66FFFFFF;
    private static final int COST_OFFSET_Y = 90;
    private static final int CRYSTAL_BASE_X = 4;
    private static final int CRYSTAL_SPACING = 10;
    private static final int CRYSTAL_STRIDE = 20;
    private static final int CRYSTAL_Y = 59;
    private static final int POPUP_SIZE = 30;
    private static final int POPUP_HALF = 15;
    private static final int POPUP_TOP = 75;
    private static final String VIS_COST_KEY = "gui.thaumaturge.recipe.vis_cost";

    private final Kind kind;
    private final int width;
    private final int height;
    private final List<SlotDisplay> ingredients;
    private final SlotDisplay result;
    private final List<SlotDisplay> crystals;
    private final int visCost;

    GridRecipePanel(Kind kind, int width, int height, List<SlotDisplay> ingredients, SlotDisplay result, List<SlotDisplay> crystals, int visCost) {
        this.kind = kind;
        this.width = width;
        this.height = height;
        this.ingredients = ingredients;
        this.result = result;
        this.crystals = crystals;
        this.visCost = visCost;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, PanelContext context, float rotation, int layer) {
        context.beginScaled(graphics, 0);
        context.blitScaled(graphics, kind.u, BACKGROUND_V, kind.width, BACKGROUND_HEIGHT, BACKGROUND_OFFSET, BACKGROUND_OFFSET, PanelContext.WHITE);
        if (kind.arcane) {
            context.blitScaled(graphics, OVERLAY_U, OVERLAY_V, OVERLAY_SIZE, OVERLAY_SIZE, OVERLAY_X, OVERLAY_Y, OVERLAY_TINT);
        }
        context.endScaled(graphics);
        if (kind.arcane) {
            context.drawCentered(graphics, Component.literal(Integer.toString(visCost)), COST_OFFSET_Y);
            for (RecipeDisplayWidget.ItemHit crystal : crystalHits(context)) {
                context.drawItem(graphics, crystal);
            }
        }
        context.drawOutputFrame(graphics);
        context.drawLabel(graphics, kind.labelKey);
        RecipeDisplayWidget.ItemHit output = outputHit(context);
        if (output != null) {
            context.drawItem(graphics, output);
        }
        for (RecipeDisplayWidget.ItemHit input : inputHits(context)) {
            context.drawItem(graphics, input);
        }
    }

    @Override
    public List<RecipeDisplayWidget.ItemHit> hits(PanelContext context) {
        List<RecipeDisplayWidget.ItemHit> hits = new ArrayList<>(inputHits(context));
        RecipeDisplayWidget.ItemHit output = outputHit(context);
        if (output != null) {
            hits.add(output);
        }
        if (kind.arcane) {
            hits.addAll(crystalHits(context));
        }
        return hits;
    }

    @Override
    public @Nullable List<Component> popup(PanelContext context, double mouseX, double mouseY) {
        if (!kind.arcane) {
            return null;
        }
        int costWidth = context.font.width(Integer.toString(visCost));
        int left = context.cx - costWidth / 2 - POPUP_HALF;
        int top = context.cy + POPUP_TOP;
        boolean inside = mouseX >= left && mouseX < left + POPUP_SIZE && mouseY >= top && mouseY < top + POPUP_SIZE;
        return inside ? List.of(Component.translatable(VIS_COST_KEY)) : null;
    }

    private RecipeDisplayWidget.@Nullable ItemHit outputHit(PanelContext context) {
        ItemStack stack = context.first(result);
        return stack.isEmpty() ? null : context.hit(stack, PanelContext.OUTPUT_X, PanelContext.OUTPUT_Y);
    }

    private List<RecipeDisplayWidget.ItemHit> inputHits(PanelContext context) {
        List<RecipeDisplayWidget.ItemHit> hits = new ArrayList<>();
        if (kind.shapeless) {
            int count = Math.min(SHAPELESS_LIMIT, ingredients.size());
            for (int i = 0; i < count; i++) {
                addInput(context, hits, i, i % GRID, i / GRID);
            }
            return hits;
        }
        for (int row = 0; row < Math.min(GRID, height); row++) {
            for (int column = 0; column < Math.min(GRID, width); column++) {
                int index = column + row * width;
                if (index < ingredients.size()) {
                    addInput(context, hits, index, column, row);
                }
            }
        }
        return hits;
    }

    private void addInput(PanelContext context, List<RecipeDisplayWidget.ItemHit> hits, int index, int column, int row) {
        ItemStack stack = context.pick(ingredients.get(index), index);
        if (!stack.isEmpty()) {
            hits.add(context.hit(stack, SLOT_ORIGIN + column * SLOT_STRIDE, SLOT_ORIGIN + row * SLOT_STRIDE));
        }
    }

    private List<RecipeDisplayWidget.ItemHit> crystalHits(PanelContext context) {
        List<ItemStack> stacks = new ArrayList<>();
        for (SlotDisplay crystal : crystals) {
            ItemStack stack = context.first(crystal);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        List<RecipeDisplayWidget.ItemHit> hits = new ArrayList<>();
        for (int i = 0; i < stacks.size(); i++) {
            hits.add(context.hit(stacks.get(i), CRYSTAL_BASE_X - CRYSTAL_SPACING * stacks.size() + CRYSTAL_STRIDE * i, CRYSTAL_Y));
        }
        return hits;
    }
}
