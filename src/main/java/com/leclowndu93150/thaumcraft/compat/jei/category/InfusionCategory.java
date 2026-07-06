package com.leclowndu93150.thaumcraft.compat.jei.category;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.leclowndu93150.thaumcraft.compat.jei.drawables.AlphaDrawable;
import com.leclowndu93150.thaumcraft.compat.jei.ingredient.AspectIngredientRenderer;
import com.leclowndu93150.thaumcraft.compat.jei.ingredient.AspectIngredientType;
import com.leclowndu93150.thaumcraft.compat.jei.utils.ResearchUtils;
import com.leclowndu93150.thaumcraft.content.infusion.InfusionRecipe;
import com.leclowndu93150.thaumcraft.content.item.PhialItem;
import com.leclowndu93150.thaumcraft.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.registry.TCRecipeTypes;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class InfusionCategory implements IRecipeCategory<RecipeHolder<InfusionRecipe>> {
    public static final IRecipeHolderType<InfusionRecipe> RECIPE_TYPE = IRecipeHolderType.create(TCRecipeTypes.INFUSION.get());

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/gui/gui_researchbook_overlay.png");
    private static final int TEXTURE_SIZE = 512;

    private static final int WIDTH = 146;
    private static final int HEIGHT = 170;
    public static final int ASPECT_Y = 135;
    public static final int ASPECT_X = 46;
    public static final int SPACE = 22;

    private static final int OUTPUT_X = 65;
    private static final int OUTPUT_Y = 7;
    private static final int CATALYST_X = 65;
    private static final int CATALYST_Y = 75;

    private static final int PAGE_TEXT_COLOR = 0xFF504030;
    private static final int INSTABILITY_LEVEL_CAP = 5;
    private static final ChatFormatting[] INSTABILITY_COLORS = {
            ChatFormatting.DARK_BLUE,
            ChatFormatting.BLUE,
            ChatFormatting.DARK_PURPLE,
            ChatFormatting.YELLOW,
            ChatFormatting.GOLD,
            ChatFormatting.DARK_RED
    };

    private static final IDrawable BACKGROUND = new AlphaDrawable(TEXTURE, 413, 154, 86, 86, 40, 44, 30, 30);
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
        builder.addOutputSlot(OUTPUT_X, OUTPUT_Y).add(recipe.rawResult());
        builder.addInputSlot(CATALYST_X, CATALYST_Y).add(recipe.catalyst());
        float currentRotation = -90.0F;
        for (Ingredient ingredient : holder.value().components()) {
            builder.addInputSlot( 30 + (int) (Mth.cos((float) (currentRotation / 180.0F * Math.PI)) * 40.0F) + 35, (int) (Mth.sin(currentRotation / 180.0F * 3.1415927F) * 40.0F) + 75)
                    .add(ingredient);
            currentRotation += (360f / holder.value().components().size());
        }

        int center = (recipe.aspects().size() * SPACE) / 2;
        int x = 0;
        for (AspectInstance aspect : recipe.aspects().sortedByAmount()){
            builder.addInputSlot(30 + ASPECT_X - center + x * SPACE, ASPECT_Y)
                    .setCustomRenderer(AspectIngredientType.INSTANCE,AspectIngredientRenderer.INSTANCE)
                    .add(AspectIngredientType.INSTANCE,aspect);

            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).add(PhialItem.makeFilled(aspect.aspect()));
            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).add(EssentiaCrystalFactory.of(aspect.aspect()));
            ++x;
        }

    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<InfusionRecipe> recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        Optional<ResearchGate> gate = recipe.value().researchGate();
        boolean doesPassGate = recipe.value().doesPassGate(Minecraft.getInstance().player);
        if (!doesPassGate && gate.isPresent() && mouseX > 92 && mouseX < 108 && mouseY > 9 && mouseY < 25) {
            tooltip.addAll(ResearchUtils.generateMissingResearchList(gate.get()));
        }
    }

    @Override
    public void draw(RecipeHolder<InfusionRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        Component header = Component.translatable("recipe.type.infusion");
        BACKGROUND.draw(guiGraphics);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 57, 0, 40, 6, 32, 32, 512, 512);
        int level = Math.min(INSTABILITY_LEVEL_CAP, recipe.value().instability() / 2);
        Component levelName = Component.translatable("gui.thaumcraft.infusion.instability." + level)
                .withStyle(INSTABILITY_COLORS[level]);
        Component label = Component.translatable("gui.thaumcraft.infusion.instability", levelName);
        guiGraphics.text(font, label, WIDTH / 2 - font.width(label) / 2, 158, PAGE_TEXT_COLOR, false);
        boolean doesPassGate = recipe.value().doesPassGate(Minecraft.getInstance().player);
        if (!doesPassGate) {
            guiGraphics.item(Items.BARRIER.getDefaultInstance(), 92, 9);
        }
    }
}
