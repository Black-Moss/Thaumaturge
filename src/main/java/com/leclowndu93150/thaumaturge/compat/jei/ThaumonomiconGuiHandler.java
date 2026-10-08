package com.leclowndu93150.thaumaturge.compat.jei;

import com.leclowndu93150.thaumaturge.client.screen.research.EntryDetailScreen;
import com.leclowndu93150.thaumaturge.compat.jei.ingredient.AspectIngredientType;
import java.util.Optional;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.handlers.IScreenHandler;
import mezz.jei.api.runtime.IClickableIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ThaumonomiconGuiHandler implements IScreenHandler<EntryDetailScreen>, IGlobalGuiHandler {
    @Override
    public IGuiProperties apply(EntryDetailScreen screen) {
        return new BookGuiProperties(screen.getClass(), 0, 0, screen.width, screen.height, screen.width, screen.height);
    }

    @Override
    public Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(IClickableIngredientFactory factory, double mouseX, double mouseY) {
        if (!(Minecraft.getInstance().screen instanceof EntryDetailScreen screen)) {
            return Optional.empty();
        }
        var item = screen.itemUnderMouse(mouseX, mouseY);
        if (item != null && !item.stack().isEmpty()) {
            return factory.createBuilder(item.stack()).buildWithArea(item.x(), item.y(), 16, 16);
        }
        var aspect = screen.aspectUnderMouse(mouseX, mouseY);
        return aspect == null ? Optional.empty() : factory.createBuilder(AspectIngredientType.INSTANCE, aspect.aspect()).buildWithArea(aspect.x(), aspect.y(), 16, 16);
    }

    private record BookGuiProperties(Class<? extends Screen> screenClass, int guiLeft, int guiTop, int guiXSize, int guiYSize, int screenWidth, int screenHeight) implements IGuiProperties {
    }
}
