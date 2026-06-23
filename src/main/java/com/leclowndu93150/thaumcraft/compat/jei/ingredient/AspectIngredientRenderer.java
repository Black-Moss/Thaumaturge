package com.leclowndu93150.thaumcraft.compat.jei.ingredient;

import com.leclowndu93150.thaumcraft.api.aspect.AspectComponents;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.client.render.aspect.AspectTagRenderer;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

public final class AspectIngredientRenderer implements IIngredientRenderer<Holder<IAspect>> {
    public static final AspectIngredientRenderer INSTANCE = new AspectIngredientRenderer();

    private AspectIngredientRenderer() {}

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, Holder<IAspect> ingredient) {
        AspectTagRenderer.render(guiGraphics, 0, 0, ingredient);
    }

    @Override
    public List<Component> getTooltip(Holder<IAspect> ingredient, TooltipFlag tooltipFlag) {
        List<Component> lines = new ArrayList<>(2);
        IAspect value = ingredient.value();
        int color = value.color() | 0xFF000000;
        lines.add(AspectComponents.name(ingredient).withStyle(style -> style.withColor(color)));
        lines.add(AspectComponents.description(ingredient).withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override
    public int getWidth() {
        return AspectTagRenderer.TAG_SIZE;
    }

    @Override
    public int getHeight() {
        return AspectTagRenderer.TAG_SIZE;
    }
}
