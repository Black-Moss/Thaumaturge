package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

final class InfusionRecipePanel implements RecipePanel {
    private static final String LABEL_KEY = "gui.thaumaturge.recipe_type.infusion";
    private static final String INSTABILITY_KEY = "gui.thaumaturge.recipe.instability";
    private static final int PANEL_SHIFT = 20;
    private static final int HEADER_U = 0;
    private static final int HEADER_V = 3;
    private static final int HEADER_W = 56;
    private static final int HEADER_H = 17;
    private static final int HEADER_X = -28;
    private static final int HEADER_Y = -56;
    private static final int BODY_U = 200;
    private static final int BODY_V = 77;
    private static final int BODY_W = 56;
    private static final int BODY_H = 44;
    private static final int BODY_X = -28;
    private static final int BODY_Y = -36;
    private static final int CHIPS_PER_ROW = 5;
    private static final int CHIPS_X = -48;
    private static final int CHIPS_Y = 50;
    private static final int RESULT_X = -8;
    private static final int RESULT_Y = -85;
    private static final int CATALYST_X = -8;
    private static final int CATALYST_Y = -16;
    private static final int RING_CENTER_Y = -8;
    private static final int RING_RADIUS = 40;
    private static final int RING_ITEM_HALF = 8;
    private static final double RING_START_DEGREES = -90.0;
    private static final double FULL_TURN_DEGREES = 360.0;
    private static final int INSTABILITY_OFFSET_Y = 94;
    private static final int INSTABILITY_PER_LEVEL = 2;
    private static final int MAX_INSTABILITY_LEVEL = 5;

    private final SlotDisplay catalyst;
    private final List<SlotDisplay> components;
    private final AspectList aspects;
    private final int instability;
    private final SlotDisplay result;

    InfusionRecipePanel(SlotDisplay catalyst, List<SlotDisplay> components, AspectList aspects, int instability, SlotDisplay result) {
        this.catalyst = catalyst;
        this.components = components;
        this.aspects = aspects;
        this.instability = instability;
        this.result = result;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, PanelContext context, float rotation, int layer) {
        context.drawLabel(graphics, LABEL_KEY);
        context.beginScaled(graphics, PANEL_SHIFT);
        context.blitScaled(graphics, HEADER_U, HEADER_V, HEADER_W, HEADER_H, HEADER_X, HEADER_Y, PanelContext.WHITE);
        context.blitScaled(graphics, BODY_U, BODY_V, BODY_W, BODY_H, BODY_X, BODY_Y, PanelContext.WHITE);
        context.endScaled(graphics);
        grid(context).render(graphics);
        for (RecipeDisplayWidget.ItemHit hit : hits(context)) {
            context.drawItem(graphics, hit);
        }
        int level = Math.min(MAX_INSTABILITY_LEVEL, instability / INSTABILITY_PER_LEVEL);
        Component line = Component.translatable(INSTABILITY_KEY).append(Component.translatable(INSTABILITY_KEY + "." + level));
        context.drawCentered(graphics, line, INSTABILITY_OFFSET_Y);
    }

    @Override
    public List<RecipeDisplayWidget.ItemHit> hits(PanelContext context) {
        List<RecipeDisplayWidget.ItemHit> hits = new ArrayList<>();
        context.collect(hits, context.first(result), RESULT_X, RESULT_Y);
        context.collect(hits, context.pick(catalyst, 0), CATALYST_X, CATALYST_Y);
        int count = components.size();
        for (int i = 0; i < count; i++) {
            double angle = Math.toRadians(RING_START_DEGREES + FULL_TURN_DEGREES / count * i);
            int offsetX = (int) (Math.cos(angle) * RING_RADIUS) - RING_ITEM_HALF;
            int offsetY = RING_CENTER_Y + (int) (Math.sin(angle) * RING_RADIUS) - RING_ITEM_HALF;
            context.collect(hits, context.pick(components.get(i), i + 1), offsetX, offsetY);
        }
        return hits;
    }

    @Override
    public @Nullable List<Component> popup(PanelContext context, double mouseX, double mouseY) {
        return grid(context).popup(mouseX, mouseY);
    }

    private AspectChipGrid grid(PanelContext context) {
        return new AspectChipGrid(aspects, CHIPS_PER_ROW, context.cx + CHIPS_X, context.cy + CHIPS_Y);
    }
}
