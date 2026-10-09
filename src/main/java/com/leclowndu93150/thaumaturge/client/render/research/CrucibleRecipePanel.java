package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

final class CrucibleRecipePanel implements RecipePanel {
    private static final String LABEL_KEY = "gui.thaumaturge.recipe_type.crucible";
    private static final int HEADER_U = 0;
    private static final int HEADER_V = 3;
    private static final int HEADER_W = 56;
    private static final int HEADER_H = 17;
    private static final int HEADER_X = -28;
    private static final int HEADER_Y = -29;
    private static final int BODY_U = 0;
    private static final int BODY_V = 20;
    private static final int BODY_W = 56;
    private static final int BODY_H = 48;
    private static final int BODY_X = -28;
    private static final int BODY_Y = -12;
    private static final int DRIP_U = 100;
    private static final int DRIP_V = 84;
    private static final int DRIP_W = 11;
    private static final int DRIP_H = 13;
    private static final int DRIP_X = -25;
    private static final int DRIP_Y = -26;
    private static final int CHIPS_PER_ROW = 3;
    private static final int CHIPS_X = -28;
    private static final int CHIPS_Y = 8;
    private static final int RESULT_X = -8;
    private static final int RESULT_Y = -50;
    private static final int CATALYST_X = -64;
    private static final int CATALYST_Y = -56;

    private final SlotDisplay catalyst;
    private final AspectList aspects;
    private final SlotDisplay result;

    CrucibleRecipePanel(SlotDisplay catalyst, AspectList aspects, SlotDisplay result) {
        this.catalyst = catalyst;
        this.aspects = aspects;
        this.result = result;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, PanelContext context, float rotation, int layer) {
        context.drawLabel(graphics, LABEL_KEY);
        context.beginScaled(graphics, 0);
        context.blitScaled(graphics, HEADER_U, HEADER_V, HEADER_W, HEADER_H, HEADER_X, HEADER_Y, PanelContext.WHITE);
        context.blitScaled(graphics, BODY_U, BODY_V, BODY_W, BODY_H, BODY_X, BODY_Y, PanelContext.WHITE);
        context.blitScaled(graphics, DRIP_U, DRIP_V, DRIP_W, DRIP_H, DRIP_X, DRIP_Y, PanelContext.WHITE);
        context.endScaled(graphics);
        grid(context).render(graphics);
        for (RecipeDisplayWidget.ItemHit hit : hits(context)) {
            context.drawItem(graphics, hit);
        }
    }

    @Override
    public List<RecipeDisplayWidget.ItemHit> hits(PanelContext context) {
        List<RecipeDisplayWidget.ItemHit> hits = new ArrayList<>();
        context.collect(hits, context.first(result), RESULT_X, RESULT_Y);
        context.collect(hits, context.pickFirst(catalyst), CATALYST_X, CATALYST_Y);
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
