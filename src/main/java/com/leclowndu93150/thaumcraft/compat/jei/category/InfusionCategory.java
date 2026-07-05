package com.leclowndu93150.thaumcraft.compat.jei.category;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.leclowndu93150.thaumcraft.compat.jei.drawables.AlphaDrawable;
import com.leclowndu93150.thaumcraft.compat.jei.ingredient.AspectIngredientRenderer;
import com.leclowndu93150.thaumcraft.compat.jei.ingredient.AspectIngredientType;
import com.leclowndu93150.thaumcraft.compat.jei.utils.ResearchUtils;
import com.leclowndu93150.thaumcraft.content.infusion.InfusionRecipe;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.registry.TCRecipeTypes;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class InfusionCategory implements IRecipeCategory<RecipeHolder<InfusionRecipe>> {
    public static final IRecipeHolderType<InfusionRecipe> RECIPE_TYPE = IRecipeHolderType.create(TCRecipeTypes.INFUSION.get());

    private static final int WIDTH = 129;
    private static final int HEIGHT = 150;
    private static final int CENTER_X = 56;
    private static final int CENTER_Y = 60;
    private static final int RING_RADIUS = 40;
    private static final int ASPECT_Y = 132;
    private static final int ASPECT_SPACE = 22;

    private static final IDrawable background = new AlphaDrawable(
            Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/gui/gui_researchbook_overlay.png"),
            2, 5, 109, 129, 0, 0, 9, 10);

    private final IDrawable icon;

    public InfusionCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(TCItems.INFUSION_MATRIX.get()));
    }

    @Override
    public IRecipeType<RecipeHolder<InfusionRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("recipe.type.infusion");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<InfusionRecipe> holder, IFocusGroup focuses) {
        InfusionRecipe recipe = holder.value();
        builder.addInputSlot(CENTER_X, CENTER_Y).add(recipe.catalyst());
        List<Ingredient> components = recipe.components();
        for (int i = 0; i < components.size(); i++) {
            double angle = (Math.PI * 2.0 * i) / components.size() - Math.PI / 2.0;
            int x = CENTER_X + (int) Math.round(Math.cos(angle) * RING_RADIUS);
            int y = CENTER_Y + (int) Math.round(Math.sin(angle) * RING_RADIUS);
            builder.addInputSlot(x, y).add(components.get(i));
        }
        builder.addOutputSlot(CENTER_X, 4).add(recipe.rawResult());

        int center = (recipe.aspects().size() * ASPECT_SPACE) / 2;
        int x = 0;
        for (AspectInstance instance : recipe.aspects().sortedByAmount()) {
            builder.addInputSlot(CENTER_X + 10 - center + x * ASPECT_SPACE, ASPECT_Y)
                    .setCustomRenderer(AspectIngredientType.INSTANCE, AspectIngredientRenderer.INSTANCE)
                    .add(AspectIngredientType.INSTANCE, instance);
            x += 1;
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<InfusionRecipe> recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        Optional<ResearchGate> gate = recipe.value().researchGate();
        boolean doesPassGate = recipe.value().doesPassGate(Minecraft.getInstance().player);
        if (!doesPassGate && gate.isPresent() && mouseX > 2 && mouseX < 20 && mouseY > 2 && mouseY < 20) {
            tooltip.addAll(ResearchUtils.generateMissingResearchList(gate.get()));
        }
    }

    @Override
    public void draw(RecipeHolder<InfusionRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
        Component instability = Component.translatable("gui.thaumcraft.infusion.instability", recipe.value().instability());
        guiGraphics.text(Minecraft.getInstance().font, instability, 4, HEIGHT - 10, 0xFF404040, false);
        boolean doesPassGate = recipe.value().doesPassGate(Minecraft.getInstance().player);
        if (!doesPassGate) {
            guiGraphics.item(Items.BARRIER.getDefaultInstance(), 2, 2);
        }
    }
}
