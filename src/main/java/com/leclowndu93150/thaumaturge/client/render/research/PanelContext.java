package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.jspecify.annotations.Nullable;

final class PanelContext {
    static final int ITEM_SIZE = 16;
    static final int WHITE = 0xFFFFFFFF;
    static final int OUTPUT_X = -8;
    static final int OUTPUT_Y = -84;

    private static final int PANEL_SCALE = 2;
    private static final int TEXT_COLOR = 0xFF504E50;
    private static final int LABEL_OFFSET_Y = -104;
    private static final int FRAME_U = 20;
    private static final int FRAME_V = 3;
    private static final int FRAME_X = -8;
    private static final int FRAME_Y = -46;
    private static final long CYCLE_MILLIS = 1000L;

    final int cx;
    final int cy;
    final Font font;
    private final @Nullable ContextMap slots;
    private @Nullable List<ItemStack> discovered;

    private PanelContext(int cx, int cy, Font font, @Nullable ContextMap slots) {
        this.cx = cx;
        this.cy = cy;
        this.font = font;
        this.slots = slots;
    }

    static PanelContext at(int x, int y) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        return new PanelContext(x + RecipeDisplayWidget.CENTER_OFFSET, y + RecipeDisplayWidget.CENTER_OFFSET, minecraft.font, level == null ? null : SlotDisplayContext.fromLevel(level));
    }

    static boolean inside(double mouseX, double mouseY, int x, int y, int size) {
        return mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;
    }

    ItemStack first(SlotDisplay display) {
        return slots == null ? ItemStack.EMPTY : display.resolveForFirstStack(slots);
    }

    ItemStack pick(SlotDisplay display, int counter) {
        List<ItemStack> stacks = alternatives(display);
        if (stacks.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return stacks.get((int) Math.floorMod(counter + Util.getMillis() / CYCLE_MILLIS, (long) stacks.size()));
    }

    ItemStack pickFirst(SlotDisplay display) {
        List<ItemStack> stacks = alternatives(display);
        return stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0);
    }

    RecipeDisplayWidget.ItemHit hit(ItemStack stack, int offsetX, int offsetY) {
        return new RecipeDisplayWidget.ItemHit(stack, cx + offsetX, cy + offsetY);
    }

    void collect(List<RecipeDisplayWidget.ItemHit> hits, ItemStack stack, int offsetX, int offsetY) {
        if (!stack.isEmpty()) {
            hits.add(hit(stack, offsetX, offsetY));
        }
    }

    void beginScaled(GuiGraphicsExtractor graphics, int shiftY) {
        graphics.pose().pushMatrix();
        graphics.pose().translate((float) cx, (float) (cy + shiftY));
        graphics.pose().scale((float) PANEL_SCALE, (float) PANEL_SCALE);
    }

    void endScaled(GuiGraphicsExtractor graphics) {
        graphics.pose().popMatrix();
    }

    void blitScaled(GuiGraphicsExtractor graphics, int u, int v, int w, int h, int offsetX, int offsetY, int tint) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.RESEARCH_BOOK_OVERLAY, offsetX, offsetY, (float) u, (float) v, w, h, w, h, TTScreenTextures.TEX_SIZE, TTScreenTextures.TEX_SIZE,
                tint);
    }

    void drawOutputFrame(GuiGraphicsExtractor graphics) {
        beginScaled(graphics, 0);
        blitScaled(graphics, FRAME_U, FRAME_V, ITEM_SIZE, ITEM_SIZE, FRAME_X, FRAME_Y, WHITE);
        endScaled(graphics);
    }

    void drawLabel(GuiGraphicsExtractor graphics, String key) {
        drawCentered(graphics, Component.translatable(key), LABEL_OFFSET_Y);
    }

    void drawCentered(GuiGraphicsExtractor graphics, Component text, int offsetY) {
        graphics.text(font, text, cx - font.width(text) / 2, cy + offsetY, TEXT_COLOR, false);
    }

    void drawItem(GuiGraphicsExtractor graphics, RecipeDisplayWidget.ItemHit hit) {
        graphics.item(hit.stack(), hit.x(), hit.y());
        graphics.itemDecorations(font, hit.stack(), hit.x(), hit.y());
    }

    private List<ItemStack> alternatives(SlotDisplay display) {
        if (slots == null) {
            return List.of();
        }
        List<ItemStack> stacks = display.resolveForStacks(slots);
        Player player = Minecraft.getInstance().player;
        if (player == null || stacks.stream().noneMatch(PanelContext::bareCrystal)) {
            return stacks;
        }
        if (discovered == null) {
            discovered = EssentiaCrystalFactory.discoveredCrystals(player);
        }
        List<ItemStack> expanded = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (bareCrystal(stack)) {
                expanded.addAll(discovered);
            } else {
                expanded.add(stack);
            }
        }
        return expanded.isEmpty() ? stacks : expanded;
    }

    private static boolean bareCrystal(ItemStack stack) {
        return stack.is(TTItems.ESSENTIA_CRYSTAL.get()) && !stack.has(TTDataComponents.CRYSTAL_ASPECT.get());
    }
}
