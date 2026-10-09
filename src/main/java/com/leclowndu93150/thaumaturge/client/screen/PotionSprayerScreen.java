package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import com.leclowndu93150.thaumaturge.content.device.sprayer.BlockEntityPotionSprayer;
import com.leclowndu93150.thaumaturge.content.device.sprayer.MenuPotionSprayer;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class PotionSprayerScreen extends AbstractTTContainerScreen<MenuPotionSprayer> {
    private static final Identifier SHEET = TTIds.rl("textures/gui/gui_potion_sprayer.png");
    private static final int SHEET_SIZE = 256;
    private static final int IMAGE_WIDTH = 192;
    private static final int IMAGE_HEIGHT = 233;

    private static final int CHARGE_X = 128;
    private static final int CHARGE_Y = 36;
    private static final int CHARGE_WIDTH = 8;
    private static final int CHARGE_STEP = 9;
    private static final int CHARGE_U = 232;
    private static final int CHARGE_SCROLL_PERIOD = 256;

    private static final int TAG_COLUMNS = 2;
    private static final int TAG_X = 79;
    private static final int TAG_Y = 31;
    private static final int TAG_PITCH_X = 22;
    private static final int TAG_PITCH_Y = 16;

    private static final int BAR_X = 96;
    private static final int BAR_BOTTOM_Y = 46;
    private static final int BAR_WIDTH = 2;
    private static final int BAR_HEIGHT = 14;
    private static final int BAR_U = 192;
    private static final int BAR_V = 56;
    private static final int BAR_TRACK_COLOR = 0xFF333333;

    private static final int GLASS_X = 125;
    private static final int GLASS_Y = 28;
    private static final int GLASS_U = 205;
    private static final int GLASS_V = 28;
    private static final int GLASS_WIDTH = 14;
    private static final int GLASS_HEIGHT = 88;
    private static final int NO_TINT = -1;

    public PotionSprayerScreen(MenuPotionSprayer menu, Inventory inventory, Component title) {
        super(menu, inventory, title, SHEET, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {}

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (minecraft == null || minecraft.level == null || minecraft.player == null) {
            return;
        }
        BlockEntity found = minecraft.level.getBlockEntity(menu.sprayerPos());
        if (!(found instanceof BlockEntityPotionSprayer sprayer)) {
            return;
        }
        extractChargeColumn(graphics, sprayer);
        List<AspectInstance> recipe = sprayer.essentiaNeeded().entries();
        AspectList progress = sprayer.essentiaStored();
        for (int n = 0; n < recipe.size(); n++) {
            extractProgressBar(graphics, recipe.get(n), progress, n);
        }
        for (int n = 0; n < recipe.size(); n++) {
            AspectInstance entry = recipe.get(n);
            AspectTagRenderer.render(graphics, font, leftPos + TAG_X + TAG_PITCH_X * column(n), topPos + TAG_Y + TAG_PITCH_Y * row(n), entry.aspect(), entry.amount());
        }
        paint(graphics, leftPos + GLASS_X, topPos + GLASS_Y, new Region(GLASS_U, GLASS_V, GLASS_WIDTH, GLASS_HEIGHT), NO_TINT);
    }

    private void extractChargeColumn(GuiGraphicsExtractor graphics, BlockEntityPotionSprayer sprayer) {
        int charges = Math.min(sprayer.sprayShots(), BlockEntityPotionSprayer.MAX_CHARGES);
        if (charges < 1) {
            return;
        }
        int height = charges * CHARGE_STEP;
        int top = CHARGE_Y + (BlockEntityPotionSprayer.MAX_CHARGES - charges) * CHARGE_STEP;
        int scroll = minecraft.player.tickCount % CHARGE_SCROLL_PERIOD;
        paint(graphics, leftPos + CHARGE_X, topPos + top, new Region(CHARGE_U, scroll, CHARGE_WIDTH, height), ARGB.opaque(sprayer.color()));
    }

    private void extractProgressBar(GuiGraphicsExtractor graphics, AspectInstance entry, AspectList progress, int n) {
        int x = leftPos + BAR_X + TAG_PITCH_X * column(n);
        int bottom = topPos + BAR_BOTTOM_Y + TAG_PITCH_Y * row(n);
        paint(graphics, x, bottom - BAR_HEIGHT, new Region(BAR_U, BAR_V, BAR_WIDTH, BAR_HEIGHT), BAR_TRACK_COLOR);
        int fill = Math.min(BAR_HEIGHT, (int) ((float) progress.amountOf(entry.aspect()) / entry.amount() * BAR_HEIGHT));
        if (fill > 0) {
            paint(graphics, x, bottom - fill, new Region(BAR_U, BAR_V, BAR_WIDTH, fill), ARGB.opaque(entry.aspect().value().color()));
        }
    }

    private static int column(int n) {
        return n % TAG_COLUMNS;
    }

    private static int row(int n) {
        return n / TAG_COLUMNS;
    }

    private static void paint(GuiGraphicsExtractor graphics, int x, int y, Region region, int tint) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, SHEET, x, y, (float) region.u(), (float) region.v(), region.width(), region.height(), region.width(), region.height(), SHEET_SIZE, SHEET_SIZE,
                tint);
    }

    private record Region(int u, int v, int width, int height) {
    }
}
