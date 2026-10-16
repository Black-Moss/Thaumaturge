package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import com.leclowndu93150.thaumaturge.client.screen.widget.TTGauge;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockEntityThaumatorium;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.MenuThaumatorium;
import com.leclowndu93150.thaumaturge.network.ClientboundThaumatoriumRecipesPayload.Entry;
import com.leclowndu93150.thaumaturge.network.ServerboundThaumatoriumTogglePayload;
import java.time.Instant;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

public final class ThaumatoriumScreen extends AbstractTTContainerScreen<MenuThaumatorium> {
    private static final Identifier SHEET = TTIds.rl("textures/gui/gui_thaumatorium.png");
    private static final int SHEET_SIZE = 256;
    private static final int IMAGE_WIDTH = 175;
    private static final int IMAGE_HEIGHT = 216;

    private static final int GRID_COLUMNS = 2;
    private static final int GRID_ROWS = 3;
    private static final int GRID_VISIBLE = GRID_COLUMNS * GRID_ROWS;
    private static final int GRID_X = 48;
    private static final int GRID_Y = 56;
    private static final int CELL_SIZE = 16;

    private static final TTGauge HIGHLIGHT = new TTGauge(SHEET, SHEET_SIZE, 176, 8, CELL_SIZE, CELL_SIZE);

    private static final int ARROW_X = 82;
    private static final int ARROW_UP_Y = 56;
    private static final int ARROW_DOWN_Y = 93;
    private static final int ARROW_WIDTH = 8;
    private static final int ARROW_HEIGHT = 11;
    private static final TTGauge ARROW_UP = new TTGauge(SHEET, SHEET_SIZE, 176, ARROW_UP_Y, ARROW_WIDTH, ARROW_HEIGHT);
    private static final TTGauge ARROW_DOWN = new TTGauge(SHEET, SHEET_SIZE, 176, ARROW_DOWN_Y, ARROW_WIDTH, ARROW_HEIGHT);

    private static final int COUNTER_X = 64;
    private static final int COUNTER_Y = 48;
    private static final float COUNTER_SCALE = 0.5F;
    private static final int NO_TINT = 0xFFFFFFFF;
    private static final int COUNTER_COLOR = NO_TINT;
    private static final int COUNTER_MIN_QUEUE = 1;

    private static final int PANEL_MAX_ASPECTS = 8;
    private static final int PANEL_COLUMNS = 2;
    private static final int PANEL_BAR_X = 98;
    private static final int PANEL_BAR_Y = 40;
    private static final int PANEL_BAR_PITCH_X = 16;
    private static final int PANEL_BAR_PITCH_Y = 20;
    private static final int PANEL_BAR_WIDTH = 12;
    private static final int PANEL_BAR_HEIGHT = 3;
    private static final int PANEL_BAR_BACK_U = 176;
    private static final int PANEL_BAR_BACK_V = 4;
    private static final int PANEL_BAR_FILL_U = 176;
    private static final int PANEL_BAR_FILL_V = 0;
    private static final int PANEL_CHIP_X = 96;
    private static final int PANEL_CHIP_Y = 24;

    private static final long FEATURE_SECONDS = 1L;

    private int scrollIndex;

    public ThaumatoriumScreen(MenuThaumatorium menu, Inventory inventory, Component title) {
        super(menu, inventory, title, SHEET, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {}

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        List<Entry> recipes = menu.clientRecipes;
        clampScroll(recipes.size());
        for (int cell = 0; cell < GRID_VISIBLE; cell++) {
            int position = scrollIndex * GRID_COLUMNS + cell;
            if (position < recipes.size() && inside(event.x(), event.y(), cellX(cell), cellY(cell), CELL_SIZE, CELL_SIZE)) {
                BlockEntityThaumatorium machine = menu.blockEntity();
                if (machine != null) {
                    ClientPacketDistributor.sendToServer(new ServerboundThaumatoriumTogglePayload(machine.getBlockPos(), recipes.get(position).id()));
                }
                return true;
            }
        }
        if (showsScrollArrows(recipes.size())) {
            if (canScrollUp() && inside(event.x(), event.y(), leftPos + ARROW_X, topPos + ARROW_UP_Y, ARROW_WIDTH, ARROW_HEIGHT)) {
                scrollIndex--;
                return true;
            }
            if (canScrollDown(recipes.size()) && inside(event.x(), event.y(), leftPos + ARROW_X, topPos + ARROW_DOWN_Y, ARROW_WIDTH, ARROW_HEIGHT)) {
                scrollIndex++;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        List<Entry> recipes = menu.clientRecipes;
        clampScroll(recipes.size());
        BlockEntityThaumatorium machine = menu.blockEntity();
        for (int cell = 0; cell < GRID_VISIBLE; cell++) {
            int position = scrollIndex * GRID_COLUMNS + cell;
            if (position >= recipes.size()) {
                break;
            }
            Entry recipe = recipes.get(position);
            extractCell(graphics, recipe, cellX(cell), cellY(cell), mouseX, mouseY);
        }
        if (showsScrollArrows(recipes.size())) {
            if (canScrollUp()) {
                ARROW_UP.extractFull(graphics, leftPos + ARROW_X, topPos + ARROW_UP_Y);
            }
            if (canScrollDown(recipes.size())) {
                ARROW_DOWN.extractFull(graphics, leftPos + ARROW_X, topPos + ARROW_DOWN_Y);
            }
        }
        if (machine != null) {
            extractQueueCounter(graphics, machine);
            extractAspectPanel(graphics, machine, recipes);
        }
    }

    private void extractAspectPanel(GuiGraphicsExtractor graphics, BlockEntityThaumatorium machine, List<Entry> recipes) {
        Identifier featured = featuredRecipe(machine);
        if (featured == null) {
            return;
        }
        for (Entry recipe : recipes) {
            if (recipe.id().equals(featured)) {
                extractRequirements(graphics, machine, recipe.aspects().sortedByTag());
                return;
            }
        }
    }

    private void extractRequirements(GuiGraphicsExtractor graphics, BlockEntityThaumatorium machine, List<AspectInstance> required) {
        int shown = Math.min(required.size(), PANEL_MAX_ASPECTS);
        for (int index = 0; index < shown; index++) {
            AspectInstance need = required.get(index);
            if (!need.aspect().isBound()) {
                continue;
            }
            int offsetX = PANEL_BAR_PITCH_X * (index % PANEL_COLUMNS);
            int offsetY = PANEL_BAR_PITCH_Y * (index / PANEL_COLUMNS);
            AspectTagRenderer.render(graphics, font, leftPos + PANEL_CHIP_X + offsetX, topPos + PANEL_CHIP_Y + offsetY, need.aspect(), need.amount());
            int barX = leftPos + PANEL_BAR_X + offsetX;
            int barY = topPos + PANEL_BAR_Y + offsetY;
            blit(graphics, barX, barY, PANEL_BAR_BACK_U, PANEL_BAR_BACK_V, PANEL_BAR_WIDTH, PANEL_BAR_HEIGHT, NO_TINT);
            int stored = machine.essentia().amountOf(need.aspect());
            int filled = need.amount() <= 0 ? PANEL_BAR_WIDTH : Mth.clamp(stored * PANEL_BAR_WIDTH / need.amount(), 0, PANEL_BAR_WIDTH);
            if (filled > 0) {
                blit(graphics, barX, barY, PANEL_BAR_FILL_U, PANEL_BAR_FILL_V, filled, PANEL_BAR_HEIGHT, ARGB.opaque(need.aspect().value().color()));
            }
        }
    }

    private static @Nullable Identifier featuredRecipe(BlockEntityThaumatorium machine) {
        List<Identifier> queue = machine.queue();
        if (queue.isEmpty()) {
            return null;
        }
        long step = Instant.now().getEpochSecond() / FEATURE_SECONDS;
        return queue.get((int) Math.floorMod(step, (long) queue.size()));
    }

    private void clampScroll(int recipeCount) {
        scrollIndex = Mth.clamp(scrollIndex, 0, hiddenRows(recipeCount));
    }

    private boolean canScrollDown(int recipeCount) {
        return scrollIndex < hiddenRows(recipeCount);
    }

    private static boolean showsScrollArrows(int recipeCount) {
        return recipeCount > GRID_VISIBLE;
    }

    private static int hiddenRows(int recipeCount) {
        return Math.max(0, Math.ceilDiv(recipeCount, GRID_COLUMNS) - GRID_ROWS);
    }

    private void extractCell(GuiGraphicsExtractor graphics, Entry recipe, int x, int y, int mouseX, int mouseY) {
        if (recipe.queued()) {
            HIGHLIGHT.extractFull(graphics, x, y);
        }
        graphics.item(recipe.output(), x, y);
        if (inside(mouseX, mouseY, x, y, CELL_SIZE, CELL_SIZE)) {
            graphics.setTooltipForNextFrame(font, recipe.output(), mouseX, mouseY);
        }
    }

    private void extractQueueCounter(GuiGraphicsExtractor graphics, BlockEntityThaumatorium machine) {
        int maximum = machine.maxRecipes();
        if (maximum <= COUNTER_MIN_QUEUE) {
            return;
        }
        Component counter = Component.translatable("gui.thaumaturge.fraction", machine.queue().size(), maximum);
        graphics.pose().pushMatrix();
        graphics.pose().translate((float) (leftPos + COUNTER_X), (float) (topPos + COUNTER_Y));
        graphics.pose().scale(COUNTER_SCALE, COUNTER_SCALE);
        graphics.text(font, counter, -font.width(counter) / 2, 0, COUNTER_COLOR, false);
        graphics.pose().popMatrix();
    }

    private boolean canScrollUp() {
        return scrollIndex > 0;
    }

    private int cellX(int cell) {
        return leftPos + GRID_X + CELL_SIZE * (cell % GRID_COLUMNS);
    }

    private int cellY(int cell) {
        return topPos + GRID_Y + CELL_SIZE * (cell / GRID_COLUMNS);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    private static void blit(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int width, int height, int color) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, SHEET, x, y, (float) u, (float) v, width, height, width, height, SHEET_SIZE, SHEET_SIZE, color);
    }
}
