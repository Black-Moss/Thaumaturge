package com.leclowndu93150.thaumcraft.client.render.research;

import com.leclowndu93150.thaumcraft.content.recipe.workbench.ArcaneCraftingRecipeDisplay;
import com.leclowndu93150.thaumcraft.client.screen.TCScreenTextures;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.jspecify.annotations.Nullable;

public final class RecipeDisplayWidget {
    public static final int PANEL_SIZE = 104;
    public static final int CENTER_OFFSET = 52;

    private static final int WORKBENCH_PANEL_U = 60;
    private static final int WORKBENCH_PANEL_V = 15;
    private static final int WORKBENCH_PANEL_W = 51;
    private static final int WORKBENCH_PANEL_H = 52;
    private static final int WORKBENCH_PANEL_OFFSET_X = -26;
    private static final int WORKBENCH_PANEL_OFFSET_Y = -26;

    private static final int ARCANE_PANEL_U = 112;
    private static final int ARCANE_PANEL_V = 15;
    private static final int ARCANE_PANEL_W = 52;
    private static final int ARCANE_PANEL_H = 52;
    private static final int ARCANE_PANEL_OFFSET_X = -26;
    private static final int ARCANE_PANEL_OFFSET_Y = -26;

    private static final int SLOT_FRAME_U = 20;
    private static final int SLOT_FRAME_V = 3;
    private static final int SLOT_FRAME_W = 16;
    private static final int SLOT_FRAME_H = 16;
    private static final int SLOT_FRAME_OFFSET_X = -8;
    private static final int SLOT_FRAME_OFFSET_Y = -46;

    private static final int VIS_COST_U = 68;
    private static final int VIS_COST_V = 76;
    private static final int VIS_COST_W = 12;
    private static final int VIS_COST_H = 12;
    private static final int VIS_COST_OFFSET_X = -6;
    private static final int VIS_COST_OFFSET_Y = 40;

    private static final float PANEL_SCALE = 2.0F;

    private static final int OUTPUT_OFFSET_X = -8;
    private static final int OUTPUT_OFFSET_Y = -84;

    private static final int GRID_ANCHOR_X = -40;
    private static final int GRID_ANCHOR_Y = -40;
    private static final int GRID_STRIDE = 32;
    private static final int GRID_DIM_MAX = 3;

    private static final int CRYSTAL_BASE_OFFSET_X = 4;
    private static final int CRYSTAL_STRIDE = 20;
    private static final int CRYSTAL_HALF_STRIDE = 10;
    private static final int CRYSTAL_OFFSET_Y = 59;

    private static final int LABEL_OFFSET_Y = -104;
    private static final int VIS_TEXT_OFFSET_Y = 90;

    private static final int VIS_POPUP_OFFSET_X = -15;
    private static final int VIS_POPUP_OFFSET_Y = 75;
    private static final int VIS_POPUP_W = 30;
    private static final int VIS_POPUP_H = 30;

    private static final int LABEL_COLOR = 0xFF504E50;

    private static final int VIS_OVERLAY_TINT = 0x66FFFFFF;

    private static final int ITEM_HIT_SIZE = 16;

    private static final long CYCLE_SECONDS = 1000L;

    private RecipeDisplayWidget() {}

    public static int width() {
        return PANEL_SIZE;
    }

    public static int height() {
        return PANEL_SIZE;
    }

    public static void renderCrafting(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            RecipeDisplay display,
            long gameTime
    ) {
        int cx = x + CENTER_OFFSET;
        int cy = y + CENTER_OFFSET;
        ContextMap context = SlotDisplayContext.fromLevel(Minecraft.getInstance().level);
        Layout layout = collect(display, context);
        Font font = Minecraft.getInstance().font;
        if (layout.kind == Kind.ARCANE_SHAPED || layout.kind == Kind.ARCANE_SHAPELESS) {
            drawArcanePanel(graphics, cx, cy);
            drawVisOverlay(graphics, cx, cy);
            drawVisCostText(graphics, font, cx, cy, layout.visCost);
            drawCrystals(graphics, cx, cy, layout.crystals);
        } else {
            drawWorkbenchPanel(graphics, cx, cy);
        }
        drawSlotFrame(graphics, cx, cy);
        drawLabel(graphics, font, cx, cy, layout.kind);
        drawOutput(graphics, cx, cy, layout.output);
        drawInputs(graphics, cx, cy, layout);
    }

    public static @Nullable ItemStack hoverStackForDisplay(
            int x,
            int y,
            RecipeDisplay display,
            long gameTime,
            double mouseX,
            double mouseY
    ) {
        int cx = x + CENTER_OFFSET;
        int cy = y + CENTER_OFFSET;
        ContextMap context = SlotDisplayContext.fromLevel(Minecraft.getInstance().level);
        Layout layout = collect(display, context);
        ItemStack inputHover = hoverInput(cx, cy, layout, mouseX, mouseY);
        if (inputHover != null && !inputHover.isEmpty()) {
            return inputHover;
        }
        if (!layout.output.isEmpty()
                && mouseX >= cx + OUTPUT_OFFSET_X
                && mouseX < cx + OUTPUT_OFFSET_X + ITEM_HIT_SIZE
                && mouseY >= cy + OUTPUT_OFFSET_Y
                && mouseY < cy + OUTPUT_OFFSET_Y + ITEM_HIT_SIZE) {
            return layout.output;
        }
        if (layout.kind == Kind.ARCANE_SHAPED || layout.kind == Kind.ARCANE_SHAPELESS) {
            ItemStack crystalHover = hoverCrystal(cx, cy, layout.crystals, mouseX, mouseY);
            if (crystalHover != null && !crystalHover.isEmpty()) {
                return crystalHover;
            }
        }
        return null;
    }

    public static @Nullable Component hoverPopupForDisplay(
            int x,
            int y,
            RecipeDisplay display,
            double mouseX,
            double mouseY
    ) {
        int cx = x + CENTER_OFFSET;
        int cy = y + CENTER_OFFSET;
        ContextMap context = SlotDisplayContext.fromLevel(Minecraft.getInstance().level);
        Layout layout = collect(display, context);
        if (layout.kind != Kind.ARCANE_SHAPED && layout.kind != Kind.ARCANE_SHAPELESS) {
            return null;
        }
        Font font = Minecraft.getInstance().font;
        int costWidth = font.width(Integer.toString(layout.visCost));
        int popupX = cx - costWidth / 2 + VIS_POPUP_OFFSET_X;
        int popupY = cy + VIS_POPUP_OFFSET_Y;
        if (mouseX >= popupX && mouseX < popupX + VIS_POPUP_W && mouseY >= popupY && mouseY < popupY + VIS_POPUP_H) {
            return Component.translatable("wandtable.text1");
        }
        return null;
    }

    private static void drawWorkbenchPanel(GuiGraphicsExtractor graphics, int cx, int cy) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(cx, cy);
        graphics.pose().scale(PANEL_SCALE, PANEL_SCALE);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TCScreenTextures.RESEARCH_BOOK_OVERLAY,
                WORKBENCH_PANEL_OFFSET_X, WORKBENCH_PANEL_OFFSET_Y,
                (float) WORKBENCH_PANEL_U, (float) WORKBENCH_PANEL_V,
                WORKBENCH_PANEL_W, WORKBENCH_PANEL_H,
                WORKBENCH_PANEL_W, WORKBENCH_PANEL_H,
                TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE
        );
        graphics.pose().popMatrix();
    }

    private static void drawArcanePanel(GuiGraphicsExtractor graphics, int cx, int cy) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(cx, cy);
        graphics.pose().scale(PANEL_SCALE, PANEL_SCALE);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TCScreenTextures.RESEARCH_BOOK_OVERLAY,
                ARCANE_PANEL_OFFSET_X, ARCANE_PANEL_OFFSET_Y,
                (float) ARCANE_PANEL_U, (float) ARCANE_PANEL_V,
                ARCANE_PANEL_W, ARCANE_PANEL_H,
                ARCANE_PANEL_W, ARCANE_PANEL_H,
                TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE
        );
        graphics.pose().popMatrix();
    }

    private static void drawSlotFrame(GuiGraphicsExtractor graphics, int cx, int cy) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(cx, cy);
        graphics.pose().scale(PANEL_SCALE, PANEL_SCALE);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TCScreenTextures.RESEARCH_BOOK_OVERLAY,
                SLOT_FRAME_OFFSET_X, SLOT_FRAME_OFFSET_Y,
                (float) SLOT_FRAME_U, (float) SLOT_FRAME_V,
                SLOT_FRAME_W, SLOT_FRAME_H,
                SLOT_FRAME_W, SLOT_FRAME_H,
                TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE
        );
        graphics.pose().popMatrix();
    }

    private static void drawVisOverlay(GuiGraphicsExtractor graphics, int cx, int cy) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(cx, cy);
        graphics.pose().scale(PANEL_SCALE, PANEL_SCALE);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TCScreenTextures.RESEARCH_BOOK_OVERLAY,
                VIS_COST_OFFSET_X, VIS_COST_OFFSET_Y,
                (float) VIS_COST_U, (float) VIS_COST_V,
                VIS_COST_W, VIS_COST_H,
                VIS_COST_W, VIS_COST_H,
                TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE,
                VIS_OVERLAY_TINT
        );
        graphics.pose().popMatrix();
    }

    private static void drawVisCostText(GuiGraphicsExtractor graphics, Font font, int cx, int cy, int visCost) {
        String text = Integer.toString(visCost);
        int offset = font.width(text);
        graphics.text(font, Component.literal(text), cx - offset / 2, cy + VIS_TEXT_OFFSET_Y, LABEL_COLOR, false);
    }

    private static void drawLabel(GuiGraphicsExtractor graphics, Font font, int cx, int cy, Kind kind) {
        String key = labelKey(kind);
        if (key == null) return;
        Component text = Component.translatable(key);
        int offset = font.width(text);
        graphics.text(font, text, cx - offset / 2, cy + LABEL_OFFSET_Y, LABEL_COLOR, false);
    }

    private static @Nullable String labelKey(Kind kind) {
        return switch (kind) {
            case WORKBENCH_SHAPED -> "recipe.type.workbench";
            case WORKBENCH_SHAPELESS -> "recipe.type.workbenchshapeless";
            case ARCANE_SHAPED -> "recipe.type.arcane";
            case ARCANE_SHAPELESS -> "recipe.type.arcane.shapeless";
            case UNKNOWN -> null;
        };
    }

    private static void drawOutput(GuiGraphicsExtractor graphics, int cx, int cy, ItemStack output) {
        if (output.isEmpty()) return;
        graphics.item(output, cx + OUTPUT_OFFSET_X, cy + OUTPUT_OFFSET_Y);
    }

    private static void drawInputs(GuiGraphicsExtractor graphics, int cx, int cy, Layout layout) {
        for (Slot slot : layout.slots) {
            ItemStack stack = pickRotating(slot.cycle, slot.counter);
            if (!stack.isEmpty()) {
                graphics.item(stack, cx + GRID_ANCHOR_X + slot.col * GRID_STRIDE, cy + GRID_ANCHOR_Y + slot.row * GRID_STRIDE);
            }
        }
    }

    private static void drawCrystals(GuiGraphicsExtractor graphics, int cx, int cy, List<ItemStack> crystals) {
        if (crystals.isEmpty()) return;
        int sz = crystals.size();
        for (int a = 0; a < sz; a++) {
            ItemStack stack = crystals.get(a);
            if (stack.isEmpty()) continue;
            graphics.item(stack, cx + CRYSTAL_BASE_OFFSET_X - sz * CRYSTAL_HALF_STRIDE + a * CRYSTAL_STRIDE, cy + CRYSTAL_OFFSET_Y);
        }
    }

    private static @Nullable ItemStack hoverInput(int cx, int cy, Layout layout, double mouseX, double mouseY) {
        for (Slot slot : layout.slots) {
            int slotX = cx + GRID_ANCHOR_X + slot.col * GRID_STRIDE;
            int slotY = cy + GRID_ANCHOR_Y + slot.row * GRID_STRIDE;
            if (mouseX < slotX || mouseX >= slotX + ITEM_HIT_SIZE) continue;
            if (mouseY < slotY || mouseY >= slotY + ITEM_HIT_SIZE) continue;
            ItemStack stack = pickRotating(slot.cycle, slot.counter);
            if (!stack.isEmpty()) return stack;
        }
        return null;
    }

    private static @Nullable ItemStack hoverCrystal(int cx, int cy, List<ItemStack> crystals, double mouseX, double mouseY) {
        if (crystals.isEmpty()) return null;
        int sz = crystals.size();
        for (int a = 0; a < sz; a++) {
            ItemStack stack = crystals.get(a);
            if (stack.isEmpty()) continue;
            int slotX = cx + CRYSTAL_BASE_OFFSET_X - sz * CRYSTAL_HALF_STRIDE + a * CRYSTAL_STRIDE;
            int slotY = cy + CRYSTAL_OFFSET_Y;
            if (mouseX < slotX || mouseX >= slotX + ITEM_HIT_SIZE) continue;
            if (mouseY < slotY || mouseY >= slotY + ITEM_HIT_SIZE) continue;
            return stack;
        }
        return null;
    }

    private static Layout collect(RecipeDisplay display, ContextMap context) {
        if (display instanceof ArcaneCraftingRecipeDisplay arcane) {
            return collectArcane(arcane, context);
        }
        if (display instanceof ShapedCraftingRecipeDisplay shaped) {
            return collectShaped(shaped, context);
        }
        if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
            return collectShapeless(shapeless, context);
        }
        ItemStack result = ItemStack.EMPTY;
        if (display.result() instanceof SlotDisplay slot) {
            result = slot.resolveForFirstStack(context);
        }
        return new Layout(Kind.UNKNOWN, new ArrayList<>(), result, 0, List.of());
    }

    private static Layout collectShaped(ShapedCraftingRecipeDisplay shaped, ContextMap context) {
        int rw = shaped.width();
        int rh = shaped.height();
        List<SlotDisplay> ingredients = shaped.ingredients();
        List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < rw && i < GRID_DIM_MAX; i++) {
            for (int j = 0; j < rh && j < GRID_DIM_MAX; j++) {
                int index = i + j * rw;
                if (index >= ingredients.size()) continue;
                List<ItemStack> cycle = ingredients.get(index).resolveForStacks(context);
                if (cycle.isEmpty()) continue;
                slots.add(new Slot(i, j, index, cycle));
            }
        }
        return new Layout(Kind.WORKBENCH_SHAPED, slots, shaped.result().resolveForFirstStack(context), 0, List.of());
    }

    private static Layout collectArcane(ArcaneCraftingRecipeDisplay arcane, ContextMap context) {
        List<SlotDisplay> ingredients = arcane.ingredients();
        List<Slot> slots = new ArrayList<>();
        List<ItemStack> crystals = new ArrayList<>();
        for (SlotDisplay crystal : arcane.crystals()) {
            ItemStack stack = crystal.resolveForFirstStack(context);
            if (!stack.isEmpty()) crystals.add(stack);
        }
        if (arcane.shapeless()) {
            int cap = Math.min(ingredients.size(), 9);
            for (int i = 0; i < cap; i++) {
                List<ItemStack> cycle = ingredients.get(i).resolveForStacks(context);
                if (cycle.isEmpty()) continue;
                slots.add(new Slot(i % GRID_DIM_MAX, i / GRID_DIM_MAX, i, cycle));
            }
            return new Layout(Kind.ARCANE_SHAPELESS, slots,
                    arcane.result().resolveForFirstStack(context), arcane.visCost(), crystals);
        }
        int rw = arcane.width();
        int rh = arcane.height();
        for (int i = 0; i < rw && i < GRID_DIM_MAX; i++) {
            for (int j = 0; j < rh && j < GRID_DIM_MAX; j++) {
                int index = i + j * rw;
                if (index >= ingredients.size()) continue;
                List<ItemStack> cycle = ingredients.get(index).resolveForStacks(context);
                if (cycle.isEmpty()) continue;
                slots.add(new Slot(i, j, index, cycle));
            }
        }
        return new Layout(Kind.ARCANE_SHAPED, slots,
                arcane.result().resolveForFirstStack(context), arcane.visCost(), crystals);
    }

    private static Layout collectShapeless(ShapelessCraftingRecipeDisplay shapeless, ContextMap context) {
        List<SlotDisplay> ingredients = shapeless.ingredients();
        List<Slot> slots = new ArrayList<>();
        int cap = Math.min(ingredients.size(), 9);
        for (int i = 0; i < cap; i++) {
            List<ItemStack> cycle = ingredients.get(i).resolveForStacks(context);
            if (cycle.isEmpty()) continue;
            slots.add(new Slot(i % GRID_DIM_MAX, i / GRID_DIM_MAX, i, cycle));
        }
        return new Layout(Kind.WORKBENCH_SHAPELESS, slots, shapeless.result().resolveForFirstStack(context), 0, List.of());
    }

    private static ItemStack pickRotating(List<ItemStack> stacks, int counter) {
        if (stacks.isEmpty()) return ItemStack.EMPTY;
        long wall = System.currentTimeMillis() / CYCLE_SECONDS;
        int index = (int) Math.floorMod((long) counter + wall, (long) stacks.size());
        return stacks.get(index);
    }

    private enum Kind {
        WORKBENCH_SHAPED,
        WORKBENCH_SHAPELESS,
        ARCANE_SHAPED,
        ARCANE_SHAPELESS,
        UNKNOWN
    }

    private record Slot(int col, int row, int counter, List<ItemStack> cycle) {}

    private record Layout(Kind kind, List<Slot> slots, ItemStack output, int visCost, List<ItemStack> crystals) {}
}
