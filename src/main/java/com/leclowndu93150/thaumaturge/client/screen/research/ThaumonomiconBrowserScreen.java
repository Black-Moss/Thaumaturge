package com.leclowndu93150.thaumaturge.client.screen.research;

import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.ResearchFlag;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.ResearchEntryMeta;
import com.leclowndu93150.thaumaturge.api.research.ResearchParent;
import com.leclowndu93150.thaumaturge.api.research.ResearchUnlockConditions;
import com.leclowndu93150.thaumaturge.client.render.research.ConnectorRenderer;
import com.leclowndu93150.thaumaturge.client.render.research.EntryIconRenderer;
import com.leclowndu93150.thaumaturge.client.screen.AbstractTTScreen;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.TTTooltips;
import com.leclowndu93150.thaumaturge.client.screen.research.BrowserModel.CategoryRef;
import com.leclowndu93150.thaumaturge.client.screen.research.BrowserModel.EntryNode;
import com.leclowndu93150.thaumaturge.client.screen.tooltip.TTTooltipRenderer;
import com.leclowndu93150.thaumaturge.client.screen.tooltip.TooltipLine;
import com.leclowndu93150.thaumaturge.client.screen.tooltip.TooltipLineScale;
import com.leclowndu93150.thaumaturge.network.ServerboundClearResearchFlagsPayload;
import com.leclowndu93150.thaumaturge.network.ServerboundUnlockResearchPayload;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public final class ThaumonomiconBrowserScreen extends AbstractTTScreen {
    private static final double UNSET_MAP_POSITION = -9999.0;
    private static final double MAP_UPPER_BOUND_INSET = 1.0;
    private static final int TEX = TTScreenTextures.TEX_SIZE;

    private static final int MARGIN = 16;
    private static final int VIEW_TRIM = 2 * MARGIN;
    private static final int CELL = 24;
    private static final int BOUND_NEAR_PAD = 48;
    private static final int BOUND_FAR_PAD = 24;
    private static final float ZOOM_MIN = 1.0F;
    private static final float ZOOM_MAX = 2.0F;
    private static final float ZOOM_STEP = 0.25F;
    private static final double INERTIA_SNAP_SQUARED = 4.0;
    private static final double INERTIA_FACTOR = 0.85;
    private static final int BACKGROUND_GROW = 2;
    private static final int BACKGROUND_OFFSET = MARGIN - BACKGROUND_GROW;
    private static final int BACKGROUND_EXTRA = 2 * BACKGROUND_GROW;
    private static final double PARALLAX_BASE = 2.0;
    private static final double PARALLAX_OVERLAY = 1.5;
    private static final int HOVER_BOX_NEAR = -2;
    private static final int HOVER_BOX_FAR = 18;

    private static final int COLOR_PARENT_DONE = 0xFF999999;
    private static final int COLOR_PARENT_OPEN = 0xFF333333;
    private static final int COLOR_SIBLING_DONE = 0xFF4C4C66;
    private static final int COLOR_SIBLING_OPEN = 0xFF2F2F3F;
    private static final float LAYER_PARENT_DONE = 3.0F;
    private static final float LAYER_PARENT_OPEN = 2.0F;
    private static final float LAYER_SIBLING_DONE = 1.0F;
    private static final float LAYER_SIBLING_OPEN = 0.0F;

    private static final int FRAME_CORNER_U = 13;
    private static final int FRAME_CORNER_V = 13;
    private static final int FRAME_CORNER_SIZE = 22;
    private static final int FRAME_HORIZONTAL_U = 48;
    private static final int FRAME_VERTICAL_V = 48;
    private static final int FRAME_TILE = 64;
    private static final int FRAME_NEAR = -2;
    private static final int FRAME_FAR_OFFSET = 20;

    private static final int TAB_ICON_SIZE = 16;
    private static final int TAB_BUILT_IN_X = 1;
    private static final int TAB_ADD_ON_X_OFFSET = 17;
    private static final int TAB_Y_BASE = 10;
    private static final int TAB_PITCH = 24;
    private static final int TAB_PAGE_MARGIN = 28;
    private static final int TAB_FRAME_PAD = 3;
    private static final int TAB_FRAME_ACTIVE_TINT = 0xFF99FFFF;
    private static final int TINT_WHITE = 0xFFFFFFFF;
    private static final int TAB_ICON_DIM_TINT = 0xCCA8A8A8;
    private static final int BADGE_SIZE = 32;
    private static final float BADGE_SCALE = 0.25F;
    private static final int BADGE_TINT = 0xB3FFFFFF;
    private static final int BADGE_RESEARCH_U = 176;
    private static final int BADGE_PAGE_U = 208;
    private static final int BADGE_V = 16;
    private static final int BADGE_X = -2;
    private static final int BADGE_RESEARCH_Y = -2;
    private static final int BADGE_PAGE_Y = 9;
    private static final int LABEL_TEXT_Y = 4;
    private static final int LABEL_BUILT_IN_X = 22;
    private static final int LABEL_ADD_ON_RIGHT = 9;
    private static final int LABEL_LINE_HEIGHT = 9;
    private static final int PERCENT_SCALE = 100;

    private static final int SEARCH_BUTTON_X = 1;
    private static final int SEARCH_BUTTON_SIZE = 16;
    private static final int SEARCH_BUTTON_Y_OFFSET = 17;
    private static final int SEARCH_BUTTON_U = 160;
    private static final int SEARCH_BUTTON_V = 16;
    private static final int SEARCH_BUTTON_IDLE_TINT = 0xFFCCCCCC;
    private static final int SEARCH_LABEL_X = 19;
    private static final int SEARCH_LABEL_Y = 4;
    private static final int SEARCH_BOX_X = 20;
    private static final int SEARCH_BOX_Y = 20;
    private static final int SEARCH_BOX_WIDTH = 89;
    private static final int SEARCH_BOX_HEIGHT = 12;
    private static final int SEARCH_MAX_LENGTH = 15;

    private static final int SCROLL_BUTTON_WIDTH = 10;
    private static final int SCROLL_BUTTON_HEIGHT = 11;
    private static final int SCROLL_BUTTON_X_OFFSET = 14;
    private static final int SCROLL_UP_Y = 20;
    private static final int SCROLL_DOWN_Y_OFFSET = 1;
    private static final int SCROLL_BUTTON_U = 51;
    private static final int SCROLL_UP_V = 55;
    private static final int SCROLL_DOWN_V = 71;
    private static final int SCROLL_IDLE_TINT = 0xFFB3B3B3;

    private static final float CLACK_VOLUME = 0.4F;
    private static final float PAGE_VOLUME = 0.66F;
    private static final float SOUND_PITCH = 1.0F;

    private static final int TOOLTIP_OFFSET_X = 3;
    private static final int TOOLTIP_OFFSET_Y = -3;
    private static final int STAGE_BASE = 1;

    private static final List<ResearchFlag> FLAG_NOTE_ORDER = List.of(ResearchFlag.RESEARCH, ResearchFlag.PAGE);

    private static double rememberedX = UNSET_MAP_POSITION;
    private static double rememberedY = UNSET_MAP_POSITION;
    private static @Nullable Identifier rememberedCategory;
    private static int rememberedScroll;
    private static boolean rememberedSearch;
    private static @Nullable Identifier rememberedEntry;

    private @Nullable BrowserModel model;
    private @Nullable BrowserSearch search;
    private @Nullable CategoryRef active;
    private @Nullable EditBox searchBox;
    private @Nullable EntryNode hovered;
    private final DragTracker drag = new DragTracker();
    private final MapAxis axisX = new MapAxis(rememberedX);
    private final MapAxis axisY = new MapAxis(rememberedY);
    private int boundLeft;
    private int boundTop;
    private int boundRight;
    private int boundBottom;
    private boolean searching;
    private float zoom = ZOOM_MIN;
    private int tickCounter;
    private int scroll;

    public ThaumonomiconBrowserScreen() {
        super(Component.empty());
    }

    public static Screen reopen() {
        ThaumonomiconBrowserScreen browser = new ThaumonomiconBrowserScreen();
        Identifier remembered = rememberedEntry;
        return Optional.ofNullable(Minecraft.getInstance().player).filter(player -> remembered != null).map(player -> detailFor(player, remembered, browser)).orElse(browser);
    }

    private static Screen detailFor(Player player, Identifier id, Screen parent) {
        ResourceKey<IResearchEntry> key = ResourceKey.create(IResearchEntry.REGISTRY_KEY, id);
        return player.registryAccess().lookup(IResearchEntry.REGISTRY_KEY).flatMap(entries -> entries.get(key)).<Screen>map(found -> new EntryDetailScreen(found, id, parent)).orElse(parent);
    }

    public static void rememberEntry(@Nullable Identifier id) {
        rememberedEntry = id;
    }

    @Override
    protected void init() {
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        model = new BrowserModel(minecraft.player);
        search = new BrowserSearch(model);
        searchBox = addRenderableWidget(createSearchBox());
        active = resolveActive();
        scroll = Math.min(rememberedScroll, Math.max(0, maxScroll()));
        computeBounds();
        centreIfStray(axisX, boundLeft, boundRight);
        centreIfStray(axisY, boundTop, boundBottom);
        setSearchMode(rememberedSearch);
    }

    private EditBox createSearchBox() {
        EditBox box = new EditBox(font, SEARCH_BOX_X, SEARCH_BOX_Y, SEARCH_BOX_WIDTH, SEARCH_BOX_HEIGHT, Component.translatable("gui.thaumaturge.thaumonomicon.search"));
        box.setBordered(true);
        box.setMaxLength(SEARCH_MAX_LENGTH);
        box.setTextColor(TINT_WHITE);
        box.setResponder(this::onSearchChanged);
        return box;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        storeState();
        super.onClose();
    }

    @Override
    public void tick() {
        super.tick();
        tickCounter++;
        MapAxis.follow(axisX, axisY, INERTIA_SNAP_SQUARED, INERTIA_FACTOR);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (searching || scrollY == 0.0) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        zoom = steppedZoom(zoom, scrollY);
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            drag.release();
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (searching) {
            if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
                setSearchMode(false);
                return true;
            }
        }
        boolean boxActive = searchBox != null;
        if (searching && boxActive && searchBox.keyPressed(event)) {
            return true;
        }
        boolean typing = boxActive && searchBox.isFocused();
        if (minecraft != null && !typing && minecraft.options.keyInventory.matches(event)) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT || model == null) {
            return super.mouseClicked(event, doubleClick);
        }
        double mx = event.x();
        double my = event.y();
        if (inRect(mx, my, SEARCH_BUTTON_X, searchButtonY(), SEARCH_BUTTON_SIZE, SEARCH_BUTTON_SIZE)) {
            playSound(TTSounds.CLACK, CLACK_VOLUME);
            setSearchMode(!searching);
            return true;
        }
        if (scrollButtonsVisible()) {
            if (inRect(mx, my, scrollButtonX(), SCROLL_UP_Y, SCROLL_BUTTON_WIDTH, SCROLL_BUTTON_HEIGHT)) {
                shiftScroll(-1);
                return true;
            }
            if (inRect(mx, my, scrollButtonX(), scrollDownY(), SCROLL_BUTTON_WIDTH, SCROLL_BUTTON_HEIGHT)) {
                shiftScroll(1);
                return true;
            }
        }
        for (Tab tab : layoutTabs()) {
            if (tab.category() != active && inRect(mx, my, tab.x(), tab.y() + tab.shift(), TAB_ICON_SIZE, TAB_ICON_SIZE)) {
                selectCategory(tab.category());
                return true;
            }
        }
        if (hovered != null && clickEntry(hovered)) {
            return true;
        }
        return clickSearchResult(mx, my) || super.mouseClicked(event, doubleClick);
    }

    private boolean clickSearchResult(double mx, double my) {
        if (!searching) {
            return false;
        }
        int row = search().rowAt(mx, my, viewWidth(), viewHeight());
        if (row < 0) {
            return false;
        }
        openResult(search().get(row));
        return true;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        if (model == null) {
            return;
        }
        model.clearVisibilityCache();
        hovered = null;
        if (searching) {
            drag.release();
            search().draw(graphics, font, mouseX, mouseY, viewWidth(), viewHeight(), tickCounter);
        } else {
            updateDrag(mouseX, mouseY);
            clampTarget();
            if (active != null) {
                drawMap(graphics, mouseX, mouseY, partialTick);
            }
        }
        drawChrome(graphics, mouseX, mouseY);
    }

    private static float steppedZoom(float from, double wheel) {
        float signedStep = wheel < 0.0 ? ZOOM_STEP : -ZOOM_STEP;
        return Mth.clamp(from + signedStep, ZOOM_MIN, ZOOM_MAX);
    }

    private void drawChrome(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        drawFrame(graphics);
        drawTabs(graphics, mouseX, mouseY);
        drawControls(graphics, mouseX, mouseY);
        drawHoverTooltip(graphics, mouseX, mouseY);
    }

    private void drawControls(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        drawSearchButton(graphics, mouseX, mouseY);
        drawScrollButtons(graphics, mouseX, mouseY);
    }

    private void drawHoverTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (hovered == null) {
            return;
        }
        List<TooltipLine> lines = tooltipLines(hovered);
        TTTooltipRenderer.render(graphics, font, lines, mouseX + TOOLTIP_OFFSET_X, mouseY + TOOLTIP_OFFSET_Y);
    }

    private static void centreIfStray(MapAxis axis, int low, int high) {
        if (axis.current() == UNSET_MAP_POSITION || axis.isOutside(low, high)) {
            axis.place((low + high) / 2.0);
        }
    }

    private BrowserModel model() {
        return Objects.requireNonNull(model);
    }

    private BrowserSearch search() {
        return Objects.requireNonNull(search);
    }

    private @Nullable CategoryRef resolveActive() {
        CategoryRef remembered = model().category(rememberedCategory);
        if (remembered != null) {
            return remembered;
        }
        if (!model().builtIn().isEmpty()) {
            return model().builtIn().getFirst();
        }
        return model().addOn().isEmpty() ? null : model().addOn().getFirst();
    }

    private int viewWidth() {
        return width - VIEW_TRIM;
    }

    private int viewHeight() {
        return height - VIEW_TRIM;
    }

    private boolean inViewport(double x, double y) {
        return x >= MARGIN && y >= MARGIN && x < MARGIN + viewWidth() && y < MARGIN + viewHeight();
    }

    private static boolean inRect(double x, double y, int left, int top, int w, int h) {
        return x >= left && y >= top && x < left + w && y < top + h;
    }

    private void playSound(Supplier<SoundEvent> sound, float volume) {
        model().player().playSound(sound.get(), volume, SOUND_PITCH);
    }

    private void storeState() {
        rememberedX = axisX.current();
        rememberedY = axisY.current();
        rememberedCategory = active == null ? null : active.id();
        rememberedScroll = scroll;
        rememberedSearch = searching;
    }

    private int tabsPerPage() {
        return Math.max(0, (viewHeight() - TAB_PAGE_MARGIN) / TAB_PITCH);
    }

    private int maxScroll() {
        return model().shownAddOn().size() - tabsPerPage();
    }

    private boolean scrollButtonsVisible() {
        return maxScroll() > 0 || scroll != 0;
    }

    private int scrollButtonX() {
        return width - SCROLL_BUTTON_X_OFFSET;
    }

    private int scrollDownY() {
        return viewHeight() + SCROLL_DOWN_Y_OFFSET;
    }

    private int searchButtonY() {
        return height - SEARCH_BUTTON_Y_OFFSET;
    }

    private void shiftScroll(int delta) {
        int target = scroll + delta;
        if (target >= 0 && (delta < 0 || scroll < maxScroll())) {
            playSound(TTSounds.CLACK, CLACK_VOLUME);
            scroll = target;
        }
    }

    private List<Tab> layoutTabs() {
        List<Tab> tabs = new ArrayList<>();
        int n = 0;
        for (CategoryRef category : model().shownBuiltIn()) {
            n++;
            tabs.add(new Tab(category, TAB_BUILT_IN_X, TAB_Y_BASE + TAB_PITCH * n, 0, false));
        }
        List<CategoryRef> shownAddOn = model().shownAddOn();
        int perPage = tabsPerPage();
        int count = shownAddOn.size();
        int shift = count > perPage || count < scroll ? (viewHeight() - TAB_PAGE_MARGIN) % TAB_PITCH / 2 : 0;
        for (int k = 1; k <= count; k++) {
            if (k > scroll && k <= scroll + perPage) {
                tabs.add(new Tab(shownAddOn.get(k - 1), width - TAB_ADD_ON_X_OFFSET, TAB_Y_BASE + TAB_PITCH * (k - scroll), shift, true));
            }
        }
        return tabs;
    }

    private void selectCategory(CategoryRef category) {
        playSound(TTSounds.CLACK, CLACK_VOLUME);
        setSearchMode(false);
        active = category;
        computeBounds();
        axisX.recentre((boundLeft + boundRight) / 2.0);
        axisY.recentre((boundTop + boundBottom) / 2.0);
    }

    private void setSearchMode(boolean on) {
        searching = on;
        drag.release();
        if (searchBox == null) {
            return;
        }
        searchBox.setValue("");
        search().clear();
        searchBox.setVisible(on);
        setFocused(on ? searchBox : null);
    }

    private void onSearchChanged(String query) {
        model().clearVisibilityCache();
        search().rebuild(query);
    }

    private void computeBounds() {
        model().clearVisibilityCache();
        boundLeft = 0;
        boundRight = 0;
        boundTop = 0;
        boundBottom = 0;
        if (active == null) {
            return;
        }
        boolean first = true;
        for (EntryNode node : model().entriesOf(active.id())) {
            if (!model().visible(node)) {
                continue;
            }
            int left = CELL * node.value().column() - viewWidth() + BOUND_NEAR_PAD;
            int right = CELL * node.value().column() - BOUND_FAR_PAD;
            int top = CELL * node.value().row() - viewHeight() + BOUND_NEAR_PAD;
            int bottom = CELL * node.value().row() - BOUND_FAR_PAD;
            boundLeft = first ? left : Math.min(boundLeft, left);
            boundRight = first ? right : Math.max(boundRight, right);
            boundTop = first ? top : Math.min(boundTop, top);
            boundBottom = first ? bottom : Math.max(boundBottom, bottom);
            first = false;
        }
    }

    private void updateDrag(int mouseX, int mouseY) {
        boolean down = GLFW.glfwGetMouseButton(minecraft.getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (!down || !inViewport(mouseX, mouseY)) {
            drag.release();
            return;
        }
        if (drag.isHeld()) {
            axisX.drag(drag.deltaX(mouseX) * zoom);
            axisY.drag(drag.deltaY(mouseY) * zoom);
        }
        drag.anchorAt(mouseX, mouseY);
    }

    private void clampTarget() {
        axisX.setGoal(clampAxis(axisX.goal(), boundLeft * (double) zoom, boundRight * (double) zoom - MAP_UPPER_BOUND_INSET));
        axisY.setGoal(clampAxis(axisY.goal(), boundTop * (double) zoom, boundBottom * (double) zoom - MAP_UPPER_BOUND_INSET));
    }

    private static double clampAxis(double value, double low, double high) {
        return low > high ? value : Mth.clamp(value, low, high);
    }

    private int drawnPosition(double from, double to, float partialTick, int low, int high) {
        int value = (int) Mth.lerp((double) partialTick, from, to);
        int lowLimit = (int) (low * (double) zoom);
        int highLimit = (int) (high * (double) zoom - MAP_UPPER_BOUND_INSET);
        return lowLimit > highLimit ? value : Mth.clamp(value, lowLimit, highLimit);
    }

    private void drawMap(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int drawnX = drawnPosition(axisX.previous(), axisX.current(), partialTick, boundLeft, boundRight);
        int drawnY = drawnPosition(axisY.previous(), axisY.current(), partialTick, boundTop, boundBottom);
        float inverse = 1.0F / zoom;
        IResearchCategory category = active.data();
        graphics.pose().pushMatrix();
        graphics.pose().scale(inverse, inverse);
        drawParallax(graphics, category.background(), drawnX / PARALLAX_BASE, drawnY / PARALLAX_BASE);
        category.overlayBackground().ifPresent(overlay -> drawParallax(graphics, overlay, drawnX / PARALLAX_OVERLAY, drawnY / PARALLAX_OVERLAY));
        graphics.pose().popMatrix();
        graphics.enableScissor(MARGIN, MARGIN, MARGIN + viewWidth(), MARGIN + viewHeight());
        graphics.pose().pushMatrix();
        graphics.pose().scale(inverse, inverse);
        List<EntryNode> nodes = model().entriesOf(active.id());
        drawConnectors(graphics, nodes, MARGIN - drawnX, MARGIN - drawnY);
        ConnectorRenderer.flush(graphics);
        drawNodes(graphics, nodes, drawnX, drawnY, mouseX, mouseY);
        graphics.pose().popMatrix();
        graphics.disableScissor();
    }

    private void drawParallax(GuiGraphicsExtractor graphics, Identifier texture, double offsetX, double offsetY) {
        int w = (int) ((viewWidth() + BACKGROUND_EXTRA) * zoom);
        int h = (int) ((viewHeight() + BACKGROUND_EXTRA) * zoom);
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, (int) (BACKGROUND_OFFSET * zoom), (int) (BACKGROUND_OFFSET * zoom), (float) offsetX, (float) offsetY, w, h, w, h, TEX, TEX);
    }

    private void drawConnectors(GuiGraphicsExtractor graphics, List<EntryNode> nodes, int originX, int originY) {
        BrowserModel data = model();
        IPlayerKnowledge knowledge = data.knowledge();
        for (EntryNode node : nodes) {
            if (!data.visible(node)) {
                continue;
            }
            IResearchEntry entry = node.value();
            boolean reverse = entry.hasMeta(ResearchEntryMeta.REVERSE);
            for (ResearchParent parent : entry.parents()) {
                EntryNode other = data.entry(parent.id());
                if (parent.inherit() || other == null || !other.categoryId().equals(node.categoryId()) || other.value().siblings().contains(node.id())) {
                    continue;
                }
                if (knowledge.isResearchComplete(other.id())) {
                    link(graphics, node, other, originX, originY, COLOR_PARENT_DONE, LAYER_PARENT_DONE, true, reverse);
                } else if (data.visible(other)) {
                    link(graphics, node, other, originX, originY, COLOR_PARENT_OPEN, LAYER_PARENT_OPEN, true, reverse);
                }
            }
            for (Identifier siblingId : entry.siblings()) {
                EntryNode other = data.entry(siblingId);
                if (other == null || !other.categoryId().equals(node.categoryId())) {
                    continue;
                }
                if (knowledge.isResearchComplete(other.id())) {
                    link(graphics, other, node, originX, originY, COLOR_SIBLING_DONE, LAYER_SIBLING_DONE, false, reverse);
                } else if (data.visible(other)) {
                    link(graphics, other, node, originX, originY, COLOR_SIBLING_OPEN, LAYER_SIBLING_OPEN, false, reverse);
                }
            }
        }
    }

    private static void link(GuiGraphicsExtractor graphics, EntryNode from, EntryNode to, int originX, int originY, int color, float layer, boolean arrow, boolean flipped) {
        ConnectorRenderer.draw(graphics, from.value().column(), from.value().row(), to.value().column(), to.value().row(), originX, originY, color, layer, arrow, flipped);
    }

    private void drawNodes(GuiGraphicsExtractor graphics, List<EntryNode> nodes, int drawnX, int drawnY, int mouseX, int mouseY) {
        BrowserModel data = model();
        IPlayerKnowledge knowledge = data.knowledge();
        boolean pointerInside = inViewport(mouseX, mouseY);
        for (EntryNode node : nodes) {
            if (!data.visible(node)) {
                continue;
            }
            IResearchEntry entry = node.value();
            int relX = CELL * entry.column() - drawnX;
            int relY = CELL * entry.row() - drawnY;
            if (relX < -CELL || relY < -CELL || relX > viewWidth() * zoom || relY > viewHeight() * zoom) {
                continue;
            }
            int originX = MARGIN + relX;
            int originY = MARGIN + relY;
            EntryIconRenderer.Status status = EntryIconRenderer.Status.UNKNOWN;
            if (knowledge.isResearchComplete(node.id())) {
                status = EntryIconRenderer.Status.COMPLETE;
            } else if (data.canUnlock(node)) {
                status = EntryIconRenderer.Status.IN_PROGRESS;
            }
            boolean warp = entry.stages().stream().anyMatch(stage -> stage.warp() > 0);
            Object icon = EntryIconRenderer.resolveIcon(entry, tickCounter);
            EntryIconRenderer.render(graphics, originX, originY, entry, status, icon, warp, knowledge.hasResearchFlag(node.id(), ResearchFlag.RESEARCH),
                    knowledge.hasResearchFlag(node.id(), ResearchFlag.PAGE));
            if (pointerInside && mouseX >= (originX + HOVER_BOX_NEAR) / zoom && mouseX <= (originX + HOVER_BOX_FAR) / zoom && mouseY >= (originY + HOVER_BOX_NEAR) / zoom
                    && mouseY <= (originY + HOVER_BOX_FAR) / zoom) {
                hovered = node;
            }
        }
    }

    private void drawFrame(GuiGraphicsExtractor graphics) {
        int farX = width - FRAME_FAR_OFFSET;
        int farY = height - FRAME_FAR_OFFSET;
        for (int x = MARGIN; x < width - MARGIN; x += FRAME_TILE) {
            int w = Math.min(FRAME_TILE, width - MARGIN - x);
            sprite(graphics, x, FRAME_NEAR, FRAME_HORIZONTAL_U, FRAME_CORNER_V, w, FRAME_CORNER_SIZE, TINT_WHITE);
            sprite(graphics, x, farY, FRAME_HORIZONTAL_U, FRAME_CORNER_V, w, FRAME_CORNER_SIZE, TINT_WHITE);
        }
        for (int y = MARGIN; y < height - MARGIN; y += FRAME_TILE) {
            int h = Math.min(FRAME_TILE, height - MARGIN - y);
            sprite(graphics, FRAME_NEAR, y, FRAME_CORNER_U, FRAME_VERTICAL_V, FRAME_CORNER_SIZE, h, TINT_WHITE);
            sprite(graphics, farX, y, FRAME_CORNER_U, FRAME_VERTICAL_V, FRAME_CORNER_SIZE, h, TINT_WHITE);
        }
        sprite(graphics, FRAME_NEAR, FRAME_NEAR, FRAME_CORNER_U, FRAME_CORNER_V, FRAME_CORNER_SIZE, FRAME_CORNER_SIZE, TINT_WHITE);
        sprite(graphics, FRAME_NEAR, farY, FRAME_CORNER_U, FRAME_CORNER_V, FRAME_CORNER_SIZE, FRAME_CORNER_SIZE, TINT_WHITE);
        sprite(graphics, farX, FRAME_NEAR, FRAME_CORNER_U, FRAME_CORNER_V, FRAME_CORNER_SIZE, FRAME_CORNER_SIZE, TINT_WHITE);
        sprite(graphics, farX, farY, FRAME_CORNER_U, FRAME_CORNER_V, FRAME_CORNER_SIZE, FRAME_CORNER_SIZE, TINT_WHITE);
    }

    private static void sprite(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int w, int h, int tint) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.RESEARCH_BROWSER, x, y, (float) u, (float) v, w, h, TEX, TEX, tint);
    }

    private TabStats statsOf(CategoryRef category) {
        IPlayerKnowledge knowledge = model().knowledge();
        boolean newResearch = false;
        boolean newPage = false;
        int known = 0;
        int counted = 0;
        for (EntryNode node : model().entriesOf(category.id())) {
            boolean isKnown = knowledge.isResearchKnown(node.id());
            if (!node.value().hasMeta(ResearchEntryMeta.AUTOUNLOCK)) {
                counted++;
                known += isKnown ? 1 : 0;
            }
            if (isKnown) {
                newResearch |= knowledge.hasResearchFlag(node.id(), ResearchFlag.RESEARCH);
                newPage |= knowledge.hasResearchFlag(node.id(), ResearchFlag.PAGE);
            }
        }
        return new TabStats(newResearch, newPage, counted == 0 ? 0 : (int) ((double) known / counted * PERCENT_SCALE));
    }

    private void drawTabs(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        Tab labelled = null;
        TabStats labelledStats = null;
        for (Tab tab : layoutTabs()) {
            boolean isActive = tab.category() == active;
            int y = tab.y() + tab.shift();
            boolean isHovered = inRect(mouseX, mouseY, tab.x(), y, TAB_ICON_SIZE, TAB_ICON_SIZE);
            TabStats stats = statsOf(tab.category());
            sprite(graphics, tab.x() - TAB_FRAME_PAD, y - TAB_FRAME_PAD, FRAME_CORNER_U, FRAME_CORNER_V, FRAME_CORNER_SIZE, FRAME_CORNER_SIZE, isActive ? TAB_FRAME_ACTIVE_TINT : TINT_WHITE);
            graphics.blit(RenderPipelines.GUI_TEXTURED, tab.category().data().icon(), tab.x(), y, 0.0F, 0.0F, TAB_ICON_SIZE, TAB_ICON_SIZE, TAB_ICON_SIZE, TAB_ICON_SIZE,
                    isActive || isHovered ? TINT_WHITE : TAB_ICON_DIM_TINT);
            if (stats.newResearch()) {
                drawBadge(graphics, tab.x() + BADGE_X, y + BADGE_RESEARCH_Y, BADGE_RESEARCH_U);
            }
            if (stats.newPage()) {
                drawBadge(graphics, tab.x() + BADGE_X, y + BADGE_PAGE_Y, BADGE_PAGE_U);
            }
            if (isHovered) {
                labelled = tab;
                labelledStats = stats;
            }
        }
        if (labelled != null) {
            drawTabLabel(graphics, labelled, labelledStats);
        }
    }

    private void drawBadge(GuiGraphicsExtractor graphics, int x, int y, int u) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(BADGE_SCALE, BADGE_SCALE);
        sprite(graphics, 0, 0, u, BADGE_V, BADGE_SIZE, BADGE_SIZE, BADGE_TINT);
        graphics.pose().popMatrix();
    }

    private void drawTabLabel(GuiGraphicsExtractor graphics, Tab tab, TabStats stats) {
        ResourceKey<IResearchCategory> key = ResourceKey.create(IResearchCategory.REGISTRY_KEY, tab.category().id());
        List<Component> lines = new ArrayList<>();
        lines.add(TTTooltips.categoryPercent(key, stats.percent()));
        if (stats.newResearch()) {
            lines.add(TTTooltips.researchNew());
        }
        if (stats.newPage()) {
            lines.add(TTTooltips.pageNew());
        }
        int y = tab.y() + tab.shift() + LABEL_TEXT_Y;
        for (Component line : lines) {
            int x = tab.addOn() ? viewWidth() + LABEL_ADD_ON_RIGHT - font.width(line) : tab.x() + LABEL_BUILT_IN_X;
            graphics.text(font, line, x, y, TINT_WHITE, false);
            y += LABEL_LINE_HEIGHT;
        }
    }

    private void drawSearchButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        boolean isHovered = inRect(mouseX, mouseY, SEARCH_BUTTON_X, searchButtonY(), SEARCH_BUTTON_SIZE, SEARCH_BUTTON_SIZE);
        sprite(graphics, SEARCH_BUTTON_X, searchButtonY(), SEARCH_BUTTON_U, SEARCH_BUTTON_V, SEARCH_BUTTON_SIZE, SEARCH_BUTTON_SIZE, isHovered ? TINT_WHITE : SEARCH_BUTTON_IDLE_TINT);
        if (isHovered) {
            graphics.text(font, Component.translatable("gui.thaumaturge.thaumonomicon.search"), SEARCH_BUTTON_X + SEARCH_LABEL_X, searchButtonY() + SEARCH_LABEL_Y, TINT_WHITE, false);
        }
    }

    private void drawScrollButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!scrollButtonsVisible()) {
            return;
        }
        drawScrollArrow(graphics, mouseX, mouseY, SCROLL_UP_Y, SCROLL_UP_V);
        drawScrollArrow(graphics, mouseX, mouseY, scrollDownY(), SCROLL_DOWN_V);
    }

    private void drawScrollArrow(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int y, int v) {
        boolean isHovered = inRect(mouseX, mouseY, scrollButtonX(), y, SCROLL_BUTTON_WIDTH, SCROLL_BUTTON_HEIGHT);
        sprite(graphics, scrollButtonX(), y, SCROLL_BUTTON_U, v, SCROLL_BUTTON_WIDTH, SCROLL_BUTTON_HEIGHT, isHovered ? TINT_WHITE : SCROLL_IDLE_TINT);
    }

    private void openResult(BrowserSearch.Result result) {
        if (result.kind() == BrowserSearch.Kind.CATEGORY) {
            CategoryRef category = model().category(result.id());
            if (category != null) {
                selectCategory(category);
            }
            return;
        }
        EntryNode node = model().entry(result.id());
        if (node != null) {
            openDetail(node);
        }
    }

    private void openDetail(EntryNode node) {
        storeState();
        playSound(TTSounds.PAGE, PAGE_VOLUME);
        minecraft.setScreen(new EntryDetailScreen(node.holder(), node.id(), this));
    }

    private boolean clickEntry(EntryNode node) {
        IPlayerKnowledge knowledge = model().knowledge();
        Identifier id = node.id();
        if (knowledge.isResearchKnown(id)) {
            knowledge.clearResearchFlag(id, ResearchFlag.RESEARCH);
            knowledge.clearResearchFlag(id, ResearchFlag.PAGE);
            ClientPacketDistributor.sendToServer(new ServerboundClearResearchFlagsPayload(id, List.of(ResearchFlag.RESEARCH, ResearchFlag.PAGE)));
            int stage = knowledge.researchStage(id);
            if (stage > 0 && stage >= node.value().stages().size() - 1) {
                ClientPacketDistributor.sendToServer(new ServerboundUnlockResearchPayload(id));
            }
            openDetail(node);
            return true;
        }
        if (model().canUnlock(node)) {
            computeBounds();
            ClientPacketDistributor.sendToServer(new ServerboundUnlockResearchPayload(id));
            openDetail(node);
            return true;
        }
        return false;
    }

    private List<TooltipLine> tooltipLines(EntryNode node) {
        BrowserModel data = model();
        IPlayerKnowledge knowledge = data.knowledge();
        IResearchEntry entry = node.value();
        Identifier id = node.id();
        List<TooltipLine> lines = new ArrayList<>();
        lines.add(new TooltipLine(TTTooltips.entryNameGold(Component.translatable(entry.nameKey())), TooltipLineScale.NORMAL));
        if (data.canUnlock(node)) {
            int stage = knowledge.researchStage(id);
            boolean inProgress = !knowledge.isResearchComplete(id) && !entry.stages().isEmpty();
            if (inProgress && stage < 0) {
                lines.add(new TooltipLine(TTTooltips.beginResearch(), TooltipLineScale.HALF));
            } else if (inProgress) {
                String counter = " " + (stage + STAGE_BASE) + "/" + entry.stages().size();
                lines.add(new TooltipLine(Component.translatable("tooltip.thaumaturge.research.stage").append(counter).withStyle(ChatFormatting.AQUA), TooltipLineScale.HALF));
            }
        } else {
            lines.add(new TooltipLine(TTTooltips.researchMissing(), TooltipLineScale.HALF));
            List<Component> missing = new ArrayList<>();
            for (ResearchParent parent : entry.parents()) {
                if (!parent.isSatisfiedBy(knowledge)) {
                    EntryNode parentNode = data.entry(parent.id());
                    missing.add(parentNode == null ? Component.literal("?") : Component.translatable(parentNode.value().nameKey()));
                }
            }
            missing.addAll(ResearchUnlockConditions.lockedMessages(data.player(), knowledge, id));
            for (Component item : missing) {
                lines.add(new TooltipLine(Component.literal(" - ").append(item).withStyle(ChatFormatting.YELLOW), TooltipLineScale.HALF));
            }
        }
        for (ResearchFlag flag : FLAG_NOTE_ORDER) {
            if (knowledge.hasResearchFlag(id, flag)) {
                Component note = flag == ResearchFlag.RESEARCH ? TTTooltips.researchNew() : TTTooltips.pageNew();
                lines.add(new TooltipLine(note, TooltipLineScale.HALF));
            }
        }
        if (minecraft.options.advancedItemTooltips) {
            lines.add(new TooltipLine(Component.literal(id.toString()).withStyle(ChatFormatting.DARK_GRAY), TooltipLineScale.NORMAL));
        }
        return lines;
    }

    private record Tab(CategoryRef category, int x, int y, int shift, boolean addOn) {
    }

    private record TabStats(boolean newResearch, boolean newPage, int percent) {
    }
}
