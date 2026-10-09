package com.leclowndu93150.thaumaturge.client.render.research;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

interface RecipePanel {
    void render(GuiGraphicsExtractor graphics, PanelContext context, float rotation, int layer);

    List<RecipeDisplayWidget.ItemHit> hits(PanelContext context);

    @Nullable
    List<Component> popup(PanelContext context, double mouseX, double mouseY);
}
