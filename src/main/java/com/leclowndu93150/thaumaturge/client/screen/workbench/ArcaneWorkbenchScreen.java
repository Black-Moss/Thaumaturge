package com.leclowndu93150.thaumaturge.client.screen.workbench;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneRecipe;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTRenderPipelines;
import com.leclowndu93150.thaumaturge.client.screen.AbstractTTContainerScreen;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.TTTooltips;
import com.leclowndu93150.thaumaturge.content.recipe.ThaumaturgeCraftingManager;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandTooltips;
import com.leclowndu93150.thaumaturge.content.workbench.MenuArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.WorkbenchPayment;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class ArcaneWorkbenchScreen extends AbstractTTContainerScreen<MenuArcaneWorkbench> {
    private static final Identifier WAND_SLOT_TEXTURE = TTIds.rl("textures/gui/workbench_wand_slot.png");
    private static final int WAND_SLOT_TEX_W = 38;
    private static final int WAND_SLOT_TEX_H = 34;
    private static final int WAND_SLOT_TEX_OFFSET_X = 10;
    private static final int WAND_SLOT_TEX_OFFSET_Y = 6;
    private static final int WAND_COST_Y_OFFSET = 129;
    private static final int TEXT_HOVER_HEIGHT = 6;
    private static final int WAND_COST_HOVER_MIN_HALF_WIDTH = 10;
    private static final int SLOT_SIZE = 16;
    private static final DecimalFormat WAND_COST_FORMAT = new DecimalFormat("#.##");

    private static final int GLOW_U = 192;
    private static final int GLOW_V = 0;
    private static final int GLOW_SOURCE_SIZE = 64;
    private static final int GLOW_SIZE = GLOW_SOURCE_SIZE / 2;
    private static final int GLOW_ORIGIN = -GLOW_SIZE / 2;
    private static final float GLOW_ALPHA = 0.55F;
    private static final int GLOW_TURN_TICKS = 1440;
    private static final float GLOW_SLOT_TURN = Mth.TWO_PI / MenuArcaneWorkbench.PRIMAL_ORDER.size();
    private static final float SLOT_CENTER = SLOT_SIZE / 2.0F;

    private static final int VIS_TEXT_X = 168;
    private static final int VIS_AVAILABLE_Y = 46;
    private static final int VIS_COST_Y = 38;
    private static final float TEXT_SCALE = 0.5F;
    private static final int VIS_SHORT_COLOR = 0xFFE88A99;
    private static final int VIS_ENOUGH_COLOR = 0xFFA29CF0;
    private static final int VIS_COST_COLOR = 0xFFB2EEF0;
    private static final int WAND_COST_COLOR = 0xFFFFFFFF;
    private static final int PERCENT = 100;
    private static final String WAND_COST_SEPARATOR = " | ";

    public ArcaneWorkbenchScreen(MenuArcaneWorkbench menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TTScreenTextures.ARCANE_WORKBENCH, 190, 234);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {}

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, WAND_SLOT_TEXTURE, leftPos + MenuArcaneWorkbench.WAND_X - WAND_SLOT_TEX_OFFSET_X, topPos + MenuArcaneWorkbench.WAND_Y - WAND_SLOT_TEX_OFFSET_Y,
                0.0F, 0.0F, WAND_SLOT_TEX_W, WAND_SLOT_TEX_H, WAND_SLOT_TEX_W, WAND_SLOT_TEX_H);
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null) {
            return;
        }
        IArcaneRecipe recipe = currentRecipe(level, player);
        WorkbenchPayment.Plan plan = recipe == null ? null : WorkbenchPayment.plan(recipe, menu.getCraftingInventory(), player);
        int available = menu.getCachedVis();
        int required = plan == null ? 0 : plan.auraVis();
        if (recipe != null) {
            drawCrystalGlows(graphics, recipe, level.getGameTime(), partialTick);
        }
        boolean visShort = available < required;
        Component availableLine = TTTooltips.workbenchVisAvailable(available);
        drawHalfText(graphics, availableLine, VIS_TEXT_X, VIS_AVAILABLE_Y, visShort ? VIS_SHORT_COLOR : VIS_ENOUGH_COLOR);
        if (isOverHalfText(availableLine, VIS_TEXT_X, VIS_AVAILABLE_Y, 0, mouseX, mouseY)) {
            graphics.setComponentTooltipForNextFrame(font, TTTooltips.workbenchVisAvailableTooltip(visShort), mouseX, mouseY);
        }
        if (recipe != null && plan != null && required > 0) {
            Component costLine = costLine(recipe, plan);
            drawHalfText(graphics, costLine, VIS_TEXT_X, VIS_COST_Y, VIS_COST_COLOR);
            if (isOverHalfText(costLine, VIS_TEXT_X, VIS_COST_Y, 0, mouseX, mouseY)) {
                graphics.setComponentTooltipForNextFrame(font, costTooltip(recipe, plan), mouseX, mouseY);
            }
        }
        if (recipe != null) {
            showCrystalTooltip(graphics, recipe, mouseX, mouseY);
        }
        if (plan != null && !plan.wandCentivis().isEmpty()) {
            drawWandPayment(graphics, level.registryAccess(), plan.wandCentivis(), mouseX, mouseY);
        }
    }

    private @Nullable IArcaneRecipe currentRecipe(ClientLevel level, LocalPlayer player) {
        ArcaneCraftingInput input = menu.getCraftingInventory().asPositionedArcaneCraftInput().input();
        if (input.width() == 0) {
            return null;
        }
        return ThaumaturgeCraftingManager.findMatchingArcaneRecipe(level, input.withPlayer(player), player);
    }

    private void drawCrystalGlows(GuiGraphicsExtractor graphics, IArcaneRecipe recipe, long gameTime, float partialTick) {
        float spin = (Math.floorMod(gameTime, GLOW_TURN_TICKS) + partialTick) / GLOW_TURN_TICKS * Mth.TWO_PI;
        for (AspectInstance entry : recipe.crystalCost().entries()) {
            int slot = crystalSlot(entry);
            if (slot < 0) {
                continue;
            }
            int color = ARGB.color(GLOW_ALPHA, entry.aspect().value().color());
            graphics.pose().pushMatrix();
            graphics.pose().translate(leftPos + MenuArcaneWorkbench.CRYSTAL_X[slot] + SLOT_CENTER, topPos + MenuArcaneWorkbench.CRYSTAL_Y[slot] + SLOT_CENTER);
            graphics.pose().rotate(spin + slot * GLOW_SLOT_TURN);
            graphics.blit(TTRenderPipelines.GUI_TEXTURED_ADDITIVE, background(), GLOW_ORIGIN, GLOW_ORIGIN, GLOW_U, GLOW_V, GLOW_SIZE, GLOW_SIZE, GLOW_SOURCE_SIZE, GLOW_SOURCE_SIZE,
                    TTScreenTextures.TEX_SIZE, TTScreenTextures.TEX_SIZE, color);
            graphics.pose().popMatrix();
        }
    }

    private void showCrystalTooltip(GuiGraphicsExtractor graphics, IArcaneRecipe recipe, int mouseX, int mouseY) {
        for (AspectInstance entry : recipe.crystalCost().entries()) {
            int slot = crystalSlot(entry);
            if (slot < 0 || !isHovering(MenuArcaneWorkbench.CRYSTAL_X[slot], MenuArcaneWorkbench.CRYSTAL_Y[slot], SLOT_SIZE, SLOT_SIZE, mouseX, mouseY)) {
                continue;
            }
            List<Component> lines = new ArrayList<>();
            ItemStack crystal = menu.getCraftingInventory().getItem(MenuArcaneWorkbench.CRYSTAL_SLOT_START + slot);
            if (!crystal.isEmpty()) {
                lines.add(crystal.getHoverName());
            }
            lines.add(TTTooltips.workbenchCrystalNeeded(WandEconomy.CRYSTAL_SUBSTITUTE_VIS));
            graphics.setComponentTooltipForNextFrame(font, lines, mouseX, mouseY);
            return;
        }
    }

    private static int crystalSlot(AspectInstance entry) {
        ResourceKey<IAspect> key = entry.aspect().getKey();
        return key == null ? -1 : MenuArcaneWorkbench.PRIMAL_ORDER.indexOf(key);
    }

    private static Component costLine(IArcaneRecipe recipe, WorkbenchPayment.Plan plan) {
        int vis = plan.auraVis();
        if (!plan.crystalsToConsume().isEmpty()) {
            return TTTooltips.workbenchVisCostUnfocused(vis);
        }
        int discount = discountPercent(recipe, plan);
        return discount > 0 ? TTTooltips.workbenchVisCostDiscounted(vis, discount) : TTTooltips.workbenchVisCost(vis);
    }

    private static List<Component> costTooltip(IArcaneRecipe recipe, WorkbenchPayment.Plan plan) {
        if (!plan.crystalsToConsume().isEmpty()) {
            return TTTooltips.workbenchVisCostUnfocusedTooltip(Math.round((WandEconomy.CRAFT_AURA_SURCHARGE - 1.0F) * PERCENT));
        }
        int discount = discountPercent(recipe, plan);
        return discount > 0 ? TTTooltips.workbenchVisCostDiscountedTooltip(discount) : TTTooltips.workbenchVisCostTooltip();
    }

    private static int discountPercent(IArcaneRecipe recipe, WorkbenchPayment.Plan plan) {
        return recipe.visCost() <= 0 ? 0 : Math.round((1.0F - (float) plan.auraVis() / recipe.visCost()) * PERCENT);
    }

    private void drawWandPayment(GuiGraphicsExtractor graphics, HolderLookup.Provider registries, Map<ResourceKey<IAspect>, Integer> wandCentivis, int mouseX, int mouseY) {
        MutableComponent line = Component.empty();
        boolean first = true;
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : wandCentivis.entrySet()) {
            if (!first) {
                line.append(Component.literal(WAND_COST_SEPARATOR).withStyle(ChatFormatting.GRAY));
            }
            String amount = WAND_COST_FORMAT.format(entry.getValue() / (double) WandEconomy.CENTIVIS_PER_VIS);
            line.append(Component.literal(amount).withStyle(WandTooltips.primalColor(registries, entry.getKey())));
            first = false;
        }
        int centreX = MenuArcaneWorkbench.WAND_X + SLOT_SIZE / 2;
        drawHalfText(graphics, line, centreX, WAND_COST_Y_OFFSET, WAND_COST_COLOR);
        if (isOverHalfText(line, centreX, WAND_COST_Y_OFFSET, WAND_COST_HOVER_MIN_HALF_WIDTH, mouseX, mouseY)) {
            graphics.setComponentTooltipForNextFrame(font, TTTooltips.workbenchWandPayment(WandEconomy.CRYSTAL_SUBSTITUTE_VIS), mouseX, mouseY);
        }
    }

    private boolean isOverHalfText(Component text, int centreX, int top, int minHalfWidth, int mouseX, int mouseY) {
        int halfWidth = Math.max(Math.round(font.width(text) * TEXT_SCALE / 2.0F), minHalfWidth);
        return isHovering(centreX - halfWidth, top, halfWidth * 2, TEXT_HOVER_HEIGHT, mouseX, mouseY);
    }

    private void drawHalfText(GuiGraphicsExtractor graphics, Component text, int centreX, int top, int color) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(leftPos + centreX, topPos + top);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        graphics.centeredText(font, text, 0, 0, color);
        graphics.pose().popMatrix();
    }
}
