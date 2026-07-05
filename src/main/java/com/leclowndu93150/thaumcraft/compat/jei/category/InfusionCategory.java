package com.leclowndu93150.thaumcraft.compat.jei.category;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
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

    private static final Identifier OVERLAY_TEXTURE =
            Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/gui/gui_researchbook_overlay.png");
    private static final int TEXTURE_SIZE = 512;

    private static final int WIDTH = 124;
    private static final int HEIGHT = 210;
    private static final int CENTER_X = 58;
    private static final int CENTER_Y = 106;

    private static final int HEADER_Y = CENTER_Y - 104;
    private static final int OUTPUT_X = CENTER_X - 8;
    private static final int OUTPUT_Y = CENTER_Y - 85;
    private static final int CATALYST_X = CENTER_X - 8;
    private static final int CATALYST_Y = CENTER_Y - 16;
    private static final int RING_CENTER_Y = CENTER_Y - 8;
    private static final float RING_RADIUS = 40.0F;
    private static final float RING_START_DEGREES = -90.0F;
    private static final int ICON_HALF = 8;

    private static final int BACKDROP_SCALE = 2;
    private static final int BACKDROP_X = CENTER_X - 28 * BACKDROP_SCALE;
    private static final int BACKDROP_TOP_Y = CENTER_Y + 20 - 56 * BACKDROP_SCALE;
    private static final int BACKDROP_TOP_U = 0;
    private static final int BACKDROP_TOP_V = 3;
    private static final int BACKDROP_TOP_W = 56;
    private static final int BACKDROP_TOP_H = 17;
    private static final int BACKDROP_ALTAR_Y = CENTER_Y + 20 + (19 - 55) * BACKDROP_SCALE;
    private static final int BACKDROP_ALTAR_U = 200;
    private static final int BACKDROP_ALTAR_V = 77;
    private static final int BACKDROP_ALTAR_W = 60;
    private static final int BACKDROP_ALTAR_H = 44;

    private static final int ASPECTS_PER_ROW = 5;
    private static final int ASPECT_GRID_STEP = 20;
    private static final int ASPECT_ROW_LIFT = 10;
    private static final int ASPECT_CENTER_SHIFT = 10;
    private static final int ASPECT_BASE_X = CENTER_X - 48;
    private static final int ASPECT_BASE_Y = CENTER_Y + 50;

    private static final int INSTABILITY_Y = CENTER_Y + 94;
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
        List<Ingredient> components = recipe.components();
        float pieSlice = 360 / components.size();
        float rot = RING_START_DEGREES;
        for (Ingredient component : components) {
            int x = (int) (Mth.cos(rot / 180.0F * (float) Math.PI) * RING_RADIUS) - ICON_HALF;
            int y = (int) (Mth.sin(rot / 180.0F * (float) Math.PI) * RING_RADIUS) - ICON_HALF;
            rot += pieSlice;
            builder.addInputSlot(CENTER_X + x, RING_CENTER_Y + y).add(component);
        }
        List<AspectInstance> aspects = recipe.aspects().sortedByTag();
        int rows = (aspects.size() - 1) / ASPECTS_PER_ROW;
        int shift = (ASPECTS_PER_ROW - aspects.size() % ASPECTS_PER_ROW) * ASPECT_CENTER_SHIFT;
        int sy = ASPECT_BASE_Y - ASPECT_ROW_LIFT * rows;
        int total = 0;
        for (AspectInstance instance : aspects) {
            int m = 0;
            if (total / ASPECTS_PER_ROW >= rows && (rows > 1 || aspects.size() < ASPECTS_PER_ROW)) {
                m = 1;
            }
            int vx = ASPECT_BASE_X + total % ASPECTS_PER_ROW * ASPECT_GRID_STEP + shift * m;
            int vy = sy + total / ASPECTS_PER_ROW * ASPECT_GRID_STEP;
            builder.addInputSlot(vx, vy)
                    .setCustomRenderer(AspectIngredientType.INSTANCE, AspectIngredientRenderer.INSTANCE)
                    .add(AspectIngredientType.INSTANCE, instance);
            total++;
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
        Font font = Minecraft.getInstance().font;
        Component header = Component.translatable("recipe.type.infusion");
        guiGraphics.text(font, header, CENTER_X - font.width(header) / 2, HEADER_Y, PAGE_TEXT_COLOR, false);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, OVERLAY_TEXTURE,
                BACKDROP_X, BACKDROP_TOP_Y, BACKDROP_TOP_U, BACKDROP_TOP_V,
                BACKDROP_TOP_W * BACKDROP_SCALE, BACKDROP_TOP_H * BACKDROP_SCALE,
                BACKDROP_TOP_W, BACKDROP_TOP_H, TEXTURE_SIZE, TEXTURE_SIZE);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, OVERLAY_TEXTURE,
                BACKDROP_X, BACKDROP_ALTAR_Y, BACKDROP_ALTAR_U, BACKDROP_ALTAR_V,
                BACKDROP_ALTAR_W * BACKDROP_SCALE, BACKDROP_ALTAR_H * BACKDROP_SCALE,
                BACKDROP_ALTAR_W, BACKDROP_ALTAR_H, TEXTURE_SIZE, TEXTURE_SIZE);
        int level = Math.min(INSTABILITY_LEVEL_CAP, recipe.value().instability() / 2);
        Component levelName = Component.translatable("gui.thaumcraft.infusion.instability." + level)
                .withStyle(INSTABILITY_COLORS[level]);
        Component label = Component.translatable("gui.thaumcraft.infusion.instability", levelName);
        guiGraphics.text(font, label, CENTER_X - font.width(label) / 2, INSTABILITY_Y, PAGE_TEXT_COLOR, false);
        boolean doesPassGate = recipe.value().doesPassGate(Minecraft.getInstance().player);
        if (!doesPassGate) {
            guiGraphics.item(Items.BARRIER.getDefaultInstance(), 2, 2);
        }
    }
}
