package com.leclowndu93150.thaumcraft.client.screen.workbench;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.items.GogglesAccess;
import com.leclowndu93150.thaumcraft.api.recipe.IArcaneRecipe;
import com.leclowndu93150.thaumcraft.client.screen.AbstractTCContainerScreen;
import com.leclowndu93150.thaumcraft.client.screen.TCScreenTextures;
import com.leclowndu93150.thaumcraft.content.recipe.ThaumcraftCraftingManager;
import com.leclowndu93150.thaumcraft.content.workbench.MenuArcaneWorkbench;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ArcaneWorkbenchScreen extends AbstractTCContainerScreen<MenuArcaneWorkbench> {
    public ArcaneWorkbenchScreen(MenuArcaneWorkbench menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TCScreenTextures.ARCANE_WORKBENCH, 190,234);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {}

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int availableVis = menu.getCachedVis();
        int requiredVis = 0;
        IArcaneRecipe recipe = ThaumcraftCraftingManager.findMatchingArcaneRecipe(minecraft.level,menu.getCraftingInventory().asArcaneCraftInput(), minecraft.player);
        AspectList aspects = AspectList.EMPTY;
        int discountPercentage = 0;
        if (recipe != null && recipe.doesPassGate(minecraft.player)) {
            requiredVis = recipe.getReducedVis(minecraft.player);
            aspects = recipe.getCrystals();
            discountPercentage = GogglesAccess.totalVisDiscount(minecraft.player);
        }
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        if (!aspects.isEmpty()){
            for (AspectInstance instance : aspects.entries()){
                int color = instance.aspect().value().color();
                int index = MenuArcaneWorkbench.PRIMAL_ORDER.indexOf(instance.aspect().getKey());
                graphics.pose().pushMatrix();
                graphics.pose().translate(x+MenuArcaneWorkbench.CRYSTAL_X[index] + 7.5F, y+MenuArcaneWorkbench.CRYSTAL_Y[index] + 8F);
                graphics.pose().rotate(index * 60 + ((float) minecraft.getCameraEntity().tickCount / 75) % 360);
                graphics.pose().scale(0.5f);
                graphics.blit(RenderPipelines.GUI_TEXTURED,TCScreenTextures.ARCANE_WORKBENCH,-32,-32,192,0,64,64,256,256, ARGB.color(128,color));
                graphics.pose().popMatrix();

            }
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(x+168,y+46);
        graphics.pose().scale(0.5f);
        Component available = Component.translatable("gui.thaumcraft.arcane_workbench.vis_available",availableVis);
        int availableWidth = font.width(available) / 2;
        graphics.text(font,available,-availableWidth,0,0xFF000000 | (requiredVis > availableVis ? 15625838 : 7237358),false);
        graphics.pose().popMatrix();

        if (requiredVis > 0) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(x + 168, y + 38);
            graphics.pose().scale(0.5f);
            Component required = discountPercentage > 0 ? Component.translatable("gui.thaumcraft.arcane_workbench.required_vis_discount",requiredVis,discountPercentage) : Component.translatable("gui.thaumcraft.arcane_workbench.required_vis", requiredVis);
            int requiredWidth = font.width(required) / 2;
            graphics.text(font, required, -requiredWidth, 0, 0xFF000000 | 12648447, false);
            graphics.pose().popMatrix();

        }

    }
}
