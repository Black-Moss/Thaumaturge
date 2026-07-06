package com.leclowndu93150.thaumcraft.mixin.jei;

import com.leclowndu93150.thaumcraft.api.recipe.DustTrigger;
import com.leclowndu93150.thaumcraft.compat.jei.category.MultiblockCategory;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.library.gui.recipes.RecipeLayout;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(RecipeLayout.class)
public class RecipeLayoutMixin<R> {

    @Shadow
    @Final
    private IRecipeCategory<R> recipeCategory;

    @Shadow
    @Final
    private R recipe;

    @Shadow
    private ImmutableRect2i area;

    @Inject(method = "drawRecipe",at= @At(value = "INVOKE", target = "Lmezz/jei/api/recipe/category/IRecipeCategory;draw(Ljava/lang/Object;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/client/gui/GuiGraphicsExtractor;DD)V",shift = At.Shift.AFTER),locals = LocalCapture.CAPTURE_FAILSOFT)
    private void thaumcraft$drawWithArea(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, CallbackInfo ci, double recipeMouseX, double recipeMouseY, IRecipeSlotsView recipeCategorySlotsView, Matrix3x2fStack poseStack){
        if (recipeCategory instanceof MultiblockCategory cat)
            cat.drawExtra((RecipeHolder<DustTrigger>) recipe,area.toMutable(),guiGraphics,recipeMouseX,recipeMouseY);
    }
}
