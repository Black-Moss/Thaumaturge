package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.api.research.ResearchRequirement;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.TTTooltips;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ItemRequirementWidget {
    public static final int ICON_SIZE = 16;

    private static final int LIST_LEFT = -15;
    private static final int LIST_TOP = 24;
    private static final int ROOMY_ROWS = 6;
    private static final int ROOMY_STRIDE = 18;
    private static final int SQUEEZE_BUDGET = 110;

    private static final int BANNER_W = 56;
    private static final int BANNER_H = 16;
    private static final int BANNER_U = 200;
    private static final int BANNER_DX = -12;
    private static final int BANNER_DY = -1;
    private static final int BANNER_TINT = 0x40FFFFFF;
    private static final int BANNER_HOVER_DX = -15;

    private static final int TICK_U = 159;
    private static final int TICK_V = 207;
    private static final int TICK_SIZE = 10;
    private static final int TICK_DX = 8;

    private static final long ROTATE_MILLIS = 1000L;
    private static final long WEAR_SWEEP_MILLIS = 5000L;
    private static final int ANY_DAMAGE = 32767;

    private ItemRequirementWidget() {}

    private record Banner(int v, String tooltipKey) {
        static final Banner OBTAIN = new Banner(216, "obtain");
        static final Banner CRAFT = new Banner(200, "craft");

        void draw(GuiGraphicsExtractor graphics, int x, int y) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.RESEARCH_BOOK, x + BANNER_DX, y + BANNER_DY, (float) BANNER_U, (float) v, BANNER_W, BANNER_H, BANNER_W, BANNER_H,
                    TTScreenTextures.TEX_SIZE, TTScreenTextures.TEX_SIZE, BANNER_TINT);
        }

        void hover(GuiGraphicsExtractor graphics, Font font, int x, int y, int mouseX, int mouseY) {
            if (overIcon(x + BANNER_HOVER_DX, y, mouseX, mouseY)) {
                graphics.setTooltipForNextFrame(font, TTTooltips.need(tooltipKey), mouseX, mouseY);
            }
        }
    }

    public static int stride(int count) {
        int squeezed = SQUEEZE_BUDGET / Math.max(count, 1);
        return count > ROOMY_ROWS ? squeezed : ROOMY_STRIDE;
    }

    public static int baseShift() {
        return LIST_TOP;
    }

    public static int slotOffsetX() {
        return LIST_LEFT;
    }

    public static void renderObtainHeader(GuiGraphicsExtractor graphics, int x, int y) {
        Banner.OBTAIN.draw(graphics, x, y);
    }

    public static void renderCraftHeader(GuiGraphicsExtractor graphics, int x, int y) {
        Banner.CRAFT.draw(graphics, x, y);
    }

    public static void renderObtainHeaderTooltip(GuiGraphicsExtractor graphics, Font font, int x, int y, int mouseX, int mouseY) {
        Banner.OBTAIN.hover(graphics, font, x, y, mouseX, mouseY);
    }

    public static void renderCraftHeaderTooltip(GuiGraphicsExtractor graphics, Font font, int x, int y, int mouseX, int mouseY) {
        Banner.CRAFT.hover(graphics, font, x, y, mouseX, mouseY);
    }

    public static void renderSlot(GuiGraphicsExtractor graphics, Font font, int x, int y, ResearchRequirement requirement, int slotIndex, boolean satisfied, int mouseX, int mouseY) {
        ItemStack current = cycleItemStack(requirement, slotIndex);
        boolean hovered = overIcon(x, y, mouseX, mouseY);
        graphics.item(current, x, y);
        if (satisfied) {
            drawTick(graphics, x + TICK_DX, y);
        }
        if (hovered && !current.isEmpty()) {
            graphics.setTooltipForNextFrame(font, current, mouseX, mouseY);
        }
    }

    public static ItemStack cycleItemStack(ResearchRequirement requirement, int slotIndex) {
        HolderSet<Item> candidates = requirement.items();
        if (candidates.size() == 0) {
            return ItemStack.EMPTY;
        }
        long now = Util.getMillis();
        long position = Math.floorMod(slotIndex + now / ROTATE_MILLIS, (long) candidates.size());
        ItemStack stack = new ItemStack(candidates.get((int) position));
        stack.applyComponents(requirement.components());
        sweepWear(stack, now);
        return stack;
    }

    private static void sweepWear(ItemStack stack, long now) {
        int limit = stack.getMaxDamage();
        boolean wildcard = stack.isDamageableItem() && limit > 0 && stack.getOrDefault(DataComponents.DAMAGE, 0) == ANY_DAMAGE;
        if (wildcard) {
            long interval = Math.max(1L, WEAR_SWEEP_MILLIS / limit);
            stack.setDamageValue((int) (now / interval % limit));
        }
    }

    private static void drawTick(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.RESEARCH_BOOK, x, y, (float) TICK_U, (float) TICK_V, TICK_SIZE, TICK_SIZE, TICK_SIZE, TICK_SIZE, TTScreenTextures.TEX_SIZE,
                TTScreenTextures.TEX_SIZE);
    }

    private static boolean overIcon(int x, int y, int mouseX, int mouseY) {
        int dx = mouseX - x;
        int dy = mouseY - y;
        return dx >= 0 && dx < ICON_SIZE && dy >= 0 && dy < ICON_SIZE;
    }
}
