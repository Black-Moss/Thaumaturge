package com.leclowndu93150.thaumaturge.client.screen.construct;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.screen.AbstractTTContainerScreen;
import com.leclowndu93150.thaumaturge.content.device.bore.ArcaneBoreHost;
import com.leclowndu93150.thaumaturge.content.device.bore.ArcaneBoreTool;
import com.leclowndu93150.thaumaturge.content.device.bore.MenuArcaneBore;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public final class ArcaneBoreScreen extends AbstractTTContainerScreen<MenuArcaneBore> {
    private static final Identifier TEXTURE = TTIds.rl("textures/gui/gui_arcanebore.png");
    private static final int IMAGE_WIDTH = 175;
    private static final int IMAGE_HEIGHT = 232;
    private static final int HEALTH_BAR_X = 68;
    private static final int HEALTH_BAR_Y = 59;
    private static final int HEALTH_BAR_U = 192;
    private static final int HEALTH_BAR_V = 48;
    private static final int HEALTH_BAR_WIDTH = 39;
    private static final int HEALTH_BAR_HEIGHT = 6;
    private static final int BROKEN_X = 80;
    private static final int BROKEN_Y = 29;
    private static final int BROKEN_U = 240;
    private static final int BROKEN_V = 0;
    private static final int BROKEN_SIZE = 16;
    private static final int STATS_X = 124;
    private static final int STATS_Y = 18;
    private static final float STATS_SCALE = 0.5F;
    private static final int STATS_COLUMN_2 = 64;
    private static final int STATS_LINE_2 = 10;
    private static final int PROPS_HEADER_Y = 24;
    private static final int PROPS_FIRST_Y = 34;
    private static final int PROPS_INDENT = 4;
    private static final int PROPS_LINE_STEP = 9;
    private static final int PROPERTY_COUNT = 3;
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_REFINING = 0xFFC0C0C0;
    private static final int COLOR_FORTUNE = 0xFFEEC64A;
    private static final int COLOR_SILK = 0xFF8080FF;

    public ArcaneBoreScreen(MenuArcaneBore menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {}

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ArcaneBoreHost bore = menu.bore();
        if (bore == null) {
            return;
        }
        ItemStack held = menu.getSlot(0).getItem();
        drawHealth(graphics, bore.boreHealth() / bore.boreMaxHealth());
        if (isNearlyBroken(held)) {
            drawSprite(graphics, BROKEN_X, BROKEN_Y, BROKEN_U, BROKEN_V, BROKEN_SIZE, BROKEN_SIZE);
        }
        graphics.pose().pushMatrix();
        graphics.pose().translate(leftPos + STATS_X, topPos + STATS_Y);
        graphics.pose().scale(STATS_SCALE, STATS_SCALE);
        drawStats(graphics, bore.boreLevel(), held);
        graphics.pose().popMatrix();
    }

    private void drawSprite(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int width, int height) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + x, topPos + y, u, v, width, height, 256, 256);
    }

    private void drawHealth(GuiGraphicsExtractor graphics, float ratio) {
        drawSprite(graphics, HEALTH_BAR_X, HEALTH_BAR_Y, HEALTH_BAR_U, HEALTH_BAR_V, (int) (HEALTH_BAR_WIDTH * ratio), HEALTH_BAR_HEIGHT);
    }

    private static boolean isNearlyBroken(ItemStack stack) {
        return !stack.isEmpty() && stack.isDamageableItem() && stack.getDamageValue() + 1 >= stack.getMaxDamage();
    }

    private void drawLine(GuiGraphicsExtractor graphics, Component text, int x, int y, int color) {
        graphics.text(font, text, x, y, color, true);
    }

    private void drawStats(GuiGraphicsExtractor graphics, Level level, ItemStack held) {
        drawLine(graphics, Component.translatable("gui.thaumaturge.bore.width", 1 + ArcaneBoreTool.digRadius(held) * 2), 0, 0, COLOR_WHITE);
        drawLine(graphics, Component.translatable("gui.thaumaturge.bore.depth", ArcaneBoreTool.digDepth(held)), STATS_COLUMN_2, 0, COLOR_WHITE);
        drawLine(graphics, Component.translatable("gui.thaumaturge.bore.speed", ArcaneBoreTool.digSpeed(level, held, Blocks.STONE.defaultBlockState())), 0, STATS_LINE_2, COLOR_WHITE);
        int refining = ArcaneBoreTool.refining(held);
        int fortune = ArcaneBoreTool.fortune(level, held);
        boolean silk = ArcaneBoreTool.silkTouch(level, held);
        List<PropertyLine> properties = new ArrayList<>(PROPERTY_COUNT);
        if (refining > 0) {
            properties.add(new PropertyLine(Component.translatable("gui.thaumaturge.bore.refining", refining), COLOR_REFINING));
        }
        if (fortune > 0) {
            properties.add(new PropertyLine(Component.translatable("gui.thaumaturge.bore.fortune", fortune), COLOR_FORTUNE));
        }
        if (silk) {
            properties.add(new PropertyLine(Component.translatable("gui.thaumaturge.bore.silktouch"), COLOR_SILK));
        }
        if (properties.isEmpty()) {
            return;
        }
        drawLine(graphics, Component.translatable("gui.thaumaturge.bore.properties"), 0, PROPS_HEADER_Y, COLOR_WHITE);
        for (int i = 0; i < properties.size(); i++) {
            PropertyLine line = properties.get(i);
            drawLine(graphics, line.text(), PROPS_INDENT, PROPS_FIRST_Y + i * PROPS_LINE_STEP, line.color());
        }
    }

    private record PropertyLine(Component text, int color) {
    }
}
