package com.leclowndu93150.thaumaturge.compat.jei.ingredient;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectDiscoveryView;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

public final class AspectIngredientRenderer implements IIngredientRenderer<AspectInstance> {
    public static final AspectIngredientRenderer INSTANCE = new AspectIngredientRenderer();

    private AspectIngredientRenderer() {}

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, AspectInstance ingredient) {
        if (!AspectDiscoveryView.isDiscovered(ingredient.aspect())) {
            AspectTagRenderer.renderUnknownChip(guiGraphics, 0, 0, ingredient.aspect());
            return;
        }
        if (ingredient.amount() > 1){
            AspectTagRenderer.render(guiGraphics, Minecraft.getInstance().font, 0, 0, ingredient.aspect(),ingredient.amount());
        } else {
            AspectTagRenderer.render(guiGraphics, 0, 0, ingredient.aspect());
        }
    }

    @Override
    public List<Component> getTooltip(AspectInstance instance, TooltipFlag tooltipFlag) {
        Holder<IAspect> ingredient = instance.aspect();
        IAspect value = ingredient.value();
        int color = value.color() | 0xFF000000;
        if (!AspectDiscoveryView.isDiscovered(ingredient)) {
            List<Component> lines = new ArrayList<>(1);
            lines.add(Component.translatable("tc.aspect.unknown").withStyle(style -> style.withColor(color)));
            return lines;
        }
        List<Component> lines = new ArrayList<>(2);
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
