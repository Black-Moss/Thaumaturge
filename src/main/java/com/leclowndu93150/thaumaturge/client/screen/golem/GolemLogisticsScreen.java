package com.leclowndu93150.thaumaturge.client.screen.golem;

import com.leclowndu93150.thaumaturge.client.screen.AbstractTTContainerScreen;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.widget.TTLabelButton;
import com.leclowndu93150.thaumaturge.client.screen.widget.TTPlusMinusButton;
import com.leclowndu93150.thaumaturge.client.screen.widget.TTScrollButton;
import com.leclowndu93150.thaumaturge.client.screen.widget.TTSlider;
import com.leclowndu93150.thaumaturge.content.golem.logistics.MenuGolemLogistics;
import com.leclowndu93150.thaumaturge.network.ServerboundLogisticsRequestPayload;
import com.leclowndu93150.thaumaturge.network.ServerboundLogisticsSearchPayload;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

public final class GolemLogisticsScreen extends AbstractTTContainerScreen<MenuGolemLogistics> {
    private static final int IMAGE_SIZE = 215;
    private static final int ATLAS = 256;

    private static final int SELECTION_U = 222;
    private static final int SELECTION_V = 46;
    private static final int SELECTION_SIZE = 20;
    private static final int SELECTION_ORIGIN_X = 17;
    private static final int SELECTION_ORIGIN_Y = 17;

    private static final int SCROLL_UP_X = 195;
    private static final int SCROLL_UP_Y = 16;
    private static final int SCROLL_DOWN_X = 195;
    private static final int SCROLL_DOWN_Y = 180;
    private static final int SCROLLBAR_X = 196;
    private static final int SCROLLBAR_Y = 28;
    private static final int SCROLLBAR_W = 8;
    private static final int SCROLLBAR_H = 149;

    private static final int COUNT_MINUS_X = 13;
    private static final int COUNT_PLUS_X = 57;
    private static final int COUNT_BUTTON_Y = 195;
    private static final int COUNTBAR_X = 24;
    private static final int COUNTBAR_Y = 196;
    private static final int COUNTBAR_W = 32;
    private static final int COUNTBAR_H = 8;
    private static final int COUNT_LABEL_X = 83;
    private static final int COUNT_LABEL_Y = 196;
    private static final int COUNT_LABEL_COLOR = 0xFF333333;

    private static final int REQUEST_X = 116;
    private static final int REQUEST_Y = 200;
    private static final int REQUEST_W = 40;
    private static final int REQUEST_H = 13;
    private static final int REQUEST_U = 37;
    private static final int REQUEST_V = 82;

    private static final int SEARCH_X = 143;
    private static final int SEARCH_Y = 196;
    private static final int SEARCH_W = 55;
    private static final int SEARCH_HINT_X = 146;
    private static final int SEARCH_HINT_Y = 197;
    private static final int SEARCH_HINT_COLOR = 0xFF222222;

    private static final long REFRESH_INTERVAL_MS = 1000L;
    private static final int MIN_REQUEST = 1;
    private static final float CLACK_VOLUME = 0.66F;

    private record PagingSpec(int x, int y, TTScrollButton.Direction direction, String name, int buttonId) {
    }

    private static final List<PagingSpec> PAGING_BUTTONS = List.of(new PagingSpec(SCROLL_UP_X, SCROLL_UP_Y, TTScrollButton.Direction.UP, "scroll_up", MenuGolemLogistics.BUTTON_PAGE_UP),
            new PagingSpec(SCROLL_DOWN_X, SCROLL_DOWN_Y, TTScrollButton.Direction.DOWN, "scroll_down", MenuGolemLogistics.BUTTON_PAGE_DOWN));

    public GolemLogisticsScreen(MenuGolemLogistics menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TTScreenTextures.GUI_LOGISTICS, IMAGE_SIZE, IMAGE_SIZE);
    }

    private final List<AbstractWidget> selectionWidgets = new ArrayList<>();

    private @Nullable TTSlider scrollbar;
    private @Nullable TTSlider countbar;
    private @Nullable EditBox searchField;

    private int selectedSlot = -1;
    private ItemStack selectedStack = ItemStack.EMPTY;
    private int requestCount = MIN_REQUEST;
    private int shownPage;
    private long refreshAt;

    @Override
    protected void init() {
        super.init();
        selectionWidgets.clear();
        addPagingButtons();
        addCountSteppers();
        addSliders();
        addBottomRow();
        syncVisibility();
    }

    private void addPagingButtons() {
        PAGING_BUTTONS.stream().map(this::pagingButton).forEach(this::addRenderableWidget);
    }

    private AbstractWidget pagingButton(PagingSpec spec) {
        Component label = Component.translatable("gui.thaumaturge.logistics." + spec.name());
        return TTScrollButton.of(leftPos + spec.x(), topPos + spec.y(), spec.direction(), label, () -> clickButton(spec.buttonId()));
    }

    private void addCountSteppers() {
        Component lessLabel = Component.translatable("gui.thaumaturge.logistics.count_down");
        Component moreLabel = Component.translatable("gui.thaumaturge.logistics.count_up");
        TTPlusMinusButton less = TTPlusMinusButton.minus(leftPos + COUNT_MINUS_X, topPos + COUNT_BUTTON_Y, lessLabel, () -> adjustCount(-1));
        TTPlusMinusButton more = TTPlusMinusButton.plus(leftPos + COUNT_PLUS_X, topPos + COUNT_BUTTON_Y, moreLabel, () -> adjustCount(1));
        selectionWidgets.add(less);
        selectionWidgets.add(more);
        addRenderableWidget(less);
        addRenderableWidget(more);
    }

    private void addSliders() {
        TTSlider pages = new TTSlider(leftPos + SCROLLBAR_X, topPos + SCROLLBAR_Y, SCROLLBAR_W, SCROLLBAR_H, true, 0.0F, menu.end(), menu.start(), this::onScroll);
        TTSlider amount = new TTSlider(leftPos + COUNTBAR_X, topPos + COUNTBAR_Y, COUNTBAR_W, COUNTBAR_H, false, MIN_REQUEST, selectedCount(), requestCount, this::onCountChanged);
        scrollbar = pages;
        countbar = amount;
        selectionWidgets.add(amount);
        addRenderableWidget(pages);
        addRenderableWidget(amount);
    }

    private void addBottomRow() {
        addRequestButton();
        addSearchField();
    }

    private void addRequestButton() {
        Component label = Component.translatable("gui.thaumaturge.logistics.request");
        TTLabelButton button = TTLabelButton.centered(leftPos + REQUEST_X, topPos + REQUEST_Y, REQUEST_W, REQUEST_H, TTScreenTextures.GUI_BASE, REQUEST_U, REQUEST_V, REQUEST_W, REQUEST_H, ATLAS,
                ATLAS, label, this::sendRequest);
        selectionWidgets.add(button);
        addRenderableWidget(button);
    }

    private void addSearchField() {
        EditBox field = new EditBox(font, leftPos + SEARCH_X, topPos + SEARCH_Y, SEARCH_W, font.lineHeight, Component.empty());
        field.setMaxLength(MenuGolemLogistics.SEARCH_MAX_LENGTH);
        field.setBordered(true);
        field.setTextColor(-1);
        field.setResponder(this::onSearchChanged);
        searchField = field;
        addRenderableWidget(field);
    }

    private void clickButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    private void onScroll(float value) {
        int page = Math.round(value);
        if (page == shownPage) {
            return;
        }
        shownPage = page;
        clickButton(MenuGolemLogistics.BUTTON_SET_PAGE + page);
    }

    private void onCountChanged(float value) {
        requestCount = Math.max(MIN_REQUEST, Math.round(value));
    }

    private void adjustCount(int delta) {
        requestCount = Math.clamp(requestCount + delta, MIN_REQUEST, selectedCount());
        if (countbar != null) {
            countbar.setValue(requestCount);
        }
    }

    private void onSearchChanged(String text) {
        ClientPacketDistributor.sendToServer(new ServerboundLogisticsSearchPayload(text));
    }

    private void sendRequest() {
        ItemStack stack = selectedStack();
        if (stack.isEmpty()) {
            return;
        }
        ClientPacketDistributor.sendToServer(new ServerboundLogisticsRequestPayload(stack.copyWithCount(1), requestCount));
    }

    private ItemStack selectedStack() {
        boolean inRange = selectedSlot >= 0 && selectedSlot < menu.slots.size();
        return inRange ? menu.getSlot(selectedSlot).getItem() : ItemStack.EMPTY;
    }

    private int selectedCount() {
        ItemStack stack = selectedStack();
        return stack.isEmpty() ? MIN_REQUEST : stack.getCount();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshIfDue();
        syncSelection();
        syncVisibility();
        syncCountRange();
        syncPageRange();
    }

    private void refreshIfDue() {
        long now = System.currentTimeMillis();
        boolean due = now >= refreshAt;
        if (due) {
            refreshAt = now + REFRESH_INTERVAL_MS;
            clickButton(MenuGolemLogistics.BUTTON_REFRESH);
        }
    }

    private void syncSelection() {
        if (selectedSlot < 0) {
            return;
        }
        ItemStack current = selectedStack();
        if (current.isEmpty()) {
            selectedSlot = -1;
            selectedStack = ItemStack.EMPTY;
            return;
        }
        if (ItemStack.isSameItemSameComponents(current, selectedStack)) {
            return;
        }
        selectedSlot = findSlotHolding(selectedStack);
        if (selectedSlot < 0) {
            selectedStack = ItemStack.EMPTY;
        }
    }

    private int findSlotHolding(ItemStack wanted) {
        return menu.slots.stream().filter(slot -> ItemStack.isSameItemSameComponents(wanted, slot.getItem())).map(slot -> slot.index).findFirst().orElse(-1);
    }

    private void syncVisibility() {
        boolean shown = selectedSlot >= 0;
        selectionWidgets.forEach(widget -> widget.visible = shown);
    }

    private void syncCountRange() {
        int available = selectedCount();
        if (countbar == null || selectedSlot < 0 || countbar.max() == available) {
            return;
        }
        countbar.setMax(available);
        requestCount = MIN_REQUEST;
        countbar.setValue(requestCount);
    }

    private void syncPageRange() {
        if (scrollbar == null || scrollbar.max() == menu.end()) {
            return;
        }
        int firstPage = menu.start();
        scrollbar.setMax(menu.end());
        shownPage = firstPage;
        scrollbar.setValue(firstPage);
    }

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (selectedSlot < 0) {
            return;
        }
        int column = selectedSlot % MenuGolemLogistics.COLUMNS;
        int row = selectedSlot / MenuGolemLogistics.COLUMNS;
        int x = leftPos + SELECTION_ORIGIN_X + column * MenuGolemLogistics.SLOT_STRIDE;
        int y = topPos + SELECTION_ORIGIN_Y + row * MenuGolemLogistics.SLOT_STRIDE;
        graphics.blit(RenderPipelines.GUI_TEXTURED, background(), x, y, SELECTION_U, SELECTION_V, SELECTION_SIZE, SELECTION_SIZE, ATLAS, ATLAS);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (selectedSlot >= 0) {
            String amount = String.valueOf(requestCount);
            graphics.text(font, amount, COUNT_LABEL_X - font.width(amount) / 2, COUNT_LABEL_Y, COUNT_LABEL_COLOR, false);
        }
        if (isSearchHintShown()) {
            graphics.text(font, Component.translatable("gui.thaumaturge.logistics.search"), SEARCH_HINT_X, SEARCH_HINT_Y, SEARCH_HINT_COLOR, false);
        }
    }

    private boolean isSearchHintShown() {
        return searchField != null && searchField.getValue().isEmpty() && !searchField.isFocused();
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int button, ContainerInput containerInput) {
        if (slot == null || !slot.hasItem()) {
            super.slotClicked(slot, slotId, button, containerInput);
        } else {
            pickSlot(slotId, slot);
        }
    }

    private void pickSlot(int slotId, Slot slot) {
        playClack();
        selectedStack = slot.getItem().copy();
        selectedSlot = slotId;
        requestCount = MIN_REQUEST;
        Optional.ofNullable(countbar).ifPresent(this::resetCountSlider);
    }

    private void resetCountSlider(TTSlider slider) {
        slider.setMax(selectedCount());
        slider.setValue(requestCount);
    }

    private void playClack() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(TTSounds.CLACK.get(), CLACK_VOLUME, 1.0F);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean scrolled = scrollY != 0.0;
        if (scrolled) {
            clickButton(scrollY < 0.0 ? MenuGolemLogistics.BUTTON_PAGE_DOWN : MenuGolemLogistics.BUTTON_PAGE_UP);
        }
        return scrolled || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        Optional.ofNullable(searchField).filter(field -> !field.isMouseOver(event.x(), event.y())).ifPresent(field -> field.setFocused(false));
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!event.isEscape()) {
            return searchConsumes(event) || super.keyPressed(event);
        }
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.closeContainer();
        }
        return true;
    }

    private boolean searchConsumes(KeyEvent event) {
        if (searchField == null || !searchField.isFocused()) {
            return false;
        }
        return searchField.keyPressed(event) || searchField.canConsumeInput();
    }
}
