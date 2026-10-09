package com.leclowndu93150.thaumaturge.client.screen.golem;

import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPanel;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.client.screen.AbstractTTContainerScreen;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.widget.TTButton;
import com.leclowndu93150.thaumaturge.client.screen.widget.TTButtonIcon;
import com.leclowndu93150.thaumaturge.client.screen.widget.TTHoverButton;
import com.leclowndu93150.thaumaturge.client.screen.widget.TTPlusMinusButton;
import com.leclowndu93150.thaumaturge.content.golem.seals.MenuSealBase;
import com.leclowndu93150.thaumaturge.registry.TTGolemTraits;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;

public final class SealScreen extends AbstractTTContainerScreen<MenuSealBase> {
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 232;
    private static final int CIRCLE_U = 96;
    private static final int CIRCLE_V = 0;
    private static final int CIRCLE_SIZE = 160;
    private static final int PANEL_V = 167;
    private static final int PANEL_Y = 143;
    private static final int PANEL_WIDTH = 176;
    private static final int PANEL_HEIGHT = 89;
    private static final int CATEGORY_ICON_V = 120;
    private static final int LOCK_U_LOCKED = 32;
    private static final int LOCK_U_UNLOCKED = 48;
    private static final int REDSTONE_U_ON = 64;
    private static final int REDSTONE_U_OFF = 80;
    private static final int TOGGLE_V = 136;
    private static final int BLACKLIST_U = 0;
    private static final int WHITELIST_U = 16;
    private static final int PROP_BG_U = 2;
    private static final int PROP_BG_V = 18;
    private static final int PROP_CHECK_U = 18;
    private static final int COLOR_DIAL_U = 2;
    private static final int COLOR_DIAL_V = 18;
    private static final int COLOR_SWATCH_U = 74;
    private static final int COLOR_SWATCH_V = 31;
    private static final int FILTER_FRAME_U = 0;
    private static final int FILTER_FRAME_V = 56;
    private static final int FILTER_FRAME_SIZE = 32;
    private static final int WHITE = 0xFFFFFF;
    private static final int LABEL_BLUE = 0xBBAAFF;
    private static final int LABEL_GREY = 0xDDDDDD;
    private static final int ATLAS = 256;
    private static final String[] AXIS_NAMES = {"X", "Y", "Z"};
    private static final int ICON = 16;
    private static final int HALF_ICON = ICON / 2;
    private static final double TAB_RADIUS = 86.0;
    private static final double TAB_SPREAD = 60.0;
    private static final double TAB_STEP_MIN = 12.0;
    private static final double TAB_STEP_MAX = 24.0;
    private static final double TAB_LEFT_ANGLE = -180.0;
    private static final double TAB_CENTRING = 30.0;
    private static final double LONE_TOGGLE_ANGLE = -204.0;
    private static final int STEP_BACK_X = 19;
    private static final int STEP_FORWARD_X = 9;
    private static final int PRIORITY_BUTTON_Y = -17;
    private static final int COLOR_DOWN_X = 6;
    private static final int COLOR_UP_X = 29;
    private static final int COLOR_BUTTON_Y = 4;
    private static final int LOCK_X = -32;
    private static final int FILTER_COLUMNS = 3;
    private static final int FILTER_ROW_STEP = 12;
    private static final int BLACKLIST_Y = 11;
    private static final int[] AREA_BUTTON_Y = {-25, 0, 25};
    private static final int[] AREA_ROW_AXIS = {1, 0, 2};
    private static final int CHECK_ROW_BASE = 5;
    private static final int CHECK_SIZE = 12;
    private static final int LABEL_LIMIT = 100;
    private static final int REQUIRED_ROW_Y = -8;
    private static final int FORBIDDEN_ROW_Y = 24;
    private static final int TRAIT_PITCH = 18;
    private static final int MIDDLE_X = IMAGE_WIDTH / 2;
    private static final int MIDDLE_Y = (IMAGE_HEIGHT - 72) / 2 - 8;
    private static final int CIRCLE_HALF = CIRCLE_SIZE / 2;
    private static final int DIAL_X = 17;
    private static final int DIAL_Y = 3;
    private static final int DIAL_SIZE = 12;
    private static final int SWATCH_X = 20;
    private static final int SWATCH_Y = 6;
    private static final int SWATCH_SIZE = 6;
    private static final int HOVER_LEFT = 5;
    private static final int HOVER_RIGHT = 41;
    private static final int HOVER_TOP = 3;
    private static final int HOVER_BOTTOM = 15;
    private static final int COLOR_LABEL_X = 23;
    private static final int COLOR_LABEL_Y = 17;
    private static final int TITLE_Y = -64;
    private static final int PRIORITY_CAPTION_Y = -28;
    private static final int PRIORITY_VALUE_Y = -16;
    private static final int OWNER_LABEL_Y = 32;
    private static final int FIRST_DYE_COLOR = 1;
    private static final int LAST_DYE_COLOR = 16;
    private static final int FILTER_CELL_STEP = 24;
    private static final int FILTER_ORIGIN_BASE = 16;
    private static final int FILTER_ORIGIN_STEP = 12;
    private static final int LIMIT_RIGHT_EDGE = 17;
    private static final int LIMIT_TOP = 9;
    private static final int LIMIT_ANY_COLOR = 0xFFAA00;
    private static final int[] AREA_CAPTION_Y = {-33, -9, 15};
    private static final int[] AREA_VALUE_Y = {-24, 0, 24};
    private static final String[] AREA_CAPTION_KEYS = {"y", "x", "z"};
    private static final int REQUIRED_CAPTION_Y = -26;
    private static final int FORBIDDEN_CAPTION_Y = 6;
    private static final int PROP_FRAME_SIZE = 12;
    private static final int PROP_FRAME_MARGIN = 2;
    private static final int PROP_LABEL_X = 12;
    private static final int PROP_SIZE = 8;
    private static final int PROP_LABEL_COLOR = 0xFFFFFFFF;
    private static final ToggleFace REDSTONE_FACE = new ToggleFace(REDSTONE_U_ON, REDSTONE_U_OFF, "gui.thaumaturge.seal.setting.redon", "gui.thaumaturge.seal.setting.redoff",
            MenuSealBase.BUTTON_REDSTONE_OFF, MenuSealBase.BUTTON_REDSTONE_ON);
    private static final ToggleFace LOCK_FACE = new ToggleFace(LOCK_U_LOCKED, LOCK_U_UNLOCKED, "gui.thaumaturge.seal.setting.lock", "gui.thaumaturge.seal.setting.unlock", MenuSealBase.BUTTON_UNLOCK,
            MenuSealBase.BUTTON_LOCK);
    private static final ToggleFace LIST_FACE = new ToggleFace(BLACKLIST_U, WHITELIST_U, "gui.thaumaturge.seal.setting.blacklist", "gui.thaumaturge.seal.setting.whitelist",
            MenuSealBase.BUTTON_BLACKLIST_OFF, MenuSealBase.BUTTON_BLACKLIST_ON);

    public SealScreen(MenuSealBase menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TTScreenTextures.GUI_BASE, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    private int centreX() {
        return leftPos + MIDDLE_X;
    }

    private int centreY() {
        return topPos + MIDDLE_Y;
    }

    @Override
    protected void init() {
        super.init();
        rebuildCategoryWidgets();
    }

    private void rebuildCategoryWidgets() {
        clearWidgets();
        ISealEntity seal = menu.seal();
        if (seal == null) {
            return;
        }
        addTabs(seal);
        switch (menu.panel()) {
            case PRIORITY -> addPriorityControls(seal);
            case FILTER -> seal.filter().ifPresent(this::addFilterControls);
            case AREA -> addAreaControls();
            case TOGGLES -> addSettingControls(seal);
            case TAGS -> {
                addTagButtons(seal.type().requiredTraits(), REQUIRED_ROW_Y);
                addTagButtons(seal.type().forbiddenTraits(), FORBIDDEN_ROW_Y);
            }
        }
    }

    private void addTabs(ISealEntity seal) {
        List<SealPanel> panels = menu.panels();
        double redstoneAngle = panels.size() > 1 ? addPanelTabs(panels) : LONE_TOGGLE_ANGLE;
        addRenderableWidget(toggleButton(ringX(redstoneAngle), ringY(redstoneAngle), seal::isRedstoneControlled, REDSTONE_FACE));
    }

    private double addPanelTabs(List<SealPanel> panels) {
        int count = panels.size();
        double step = Mth.clamp(TAB_SPREAD / count, TAB_STEP_MIN, TAB_STEP_MAX);
        double first = TAB_LEFT_ANGLE + TAB_CENTRING * (count - 1) / count;
        for (int index = 0; index < count; index++) {
            double angle = first - index * step;
            SealPanel panel = panels.get(index);
            addRenderableWidget(new CategoryButton(ringX(angle), ringY(angle), panel.ordinal(), panel == menu.panel(), tabAction(panel == menu.panel(), index)));
        }
        return first - count * step;
    }

    private Runnable tabAction(boolean alreadyOpen, int index) {
        return alreadyOpen ? () -> {
        } : () -> selectCategory(index);
    }

    private int ringX(double degrees) {
        return centreX() + (int) (TAB_RADIUS * Math.cos(Math.toRadians(degrees))) - HALF_ICON;
    }

    private int ringY(double degrees) {
        return centreY() + (int) (TAB_RADIUS * Math.sin(Math.toRadians(degrees))) - HALF_ICON;
    }

    private StateButton toggleButton(int x, int y, BooleanSupplier state, ToggleFace face) {
        return new StateButton(x, y, () -> face.uv(state.getAsBoolean()), () -> face.message(state.getAsBoolean()), () -> sendButton(face.click(state.getAsBoolean())));
    }

    private void addStepper(boolean grow, int x, int y, Component label, int buttonId) {
        Runnable press = () -> sendButton(buttonId);
        addRenderableWidget(grow ? TTPlusMinusButton.plus(x, y, label, press) : TTPlusMinusButton.minus(x, y, label, press));
    }

    private void addPriorityControls(ISealEntity seal) {
        int x = centreX();
        int y = centreY();
        addStepper(false, x - STEP_BACK_X, y + PRIORITY_BUTTON_Y, Component.translatable("gui.thaumaturge.seal.priority_down"), MenuSealBase.BUTTON_PRIORITY_DOWN);
        addStepper(true, x + STEP_FORWARD_X, y + PRIORITY_BUTTON_Y, Component.translatable("gui.thaumaturge.seal.priority_up"), MenuSealBase.BUTTON_PRIORITY_UP);
        addStepper(false, x + COLOR_DOWN_X, y + COLOR_BUTTON_Y, Component.translatable("gui.thaumaturge.seal.color_previous"), MenuSealBase.BUTTON_COLOR_DOWN);
        addStepper(true, x + COLOR_UP_X, y + COLOR_BUTTON_Y, Component.translatable("gui.thaumaturge.seal.color_next"), MenuSealBase.BUTTON_COLOR_UP);
        if (isOwner(seal)) {
            addRenderableWidget(toggleButton(x + LOCK_X, y, seal::isLocked, LOCK_FACE));
        }
    }

    private boolean isOwner(ISealEntity seal) {
        return minecraft != null && minecraft.player != null && minecraft.player.getUUID().equals(seal.owner());
    }

    private void addFilterControls(ISealFilter filter) {
        int lastRow = (filter.spec().slots() - 1) / FILTER_COLUMNS;
        int y = centreY() + FILTER_ROW_STEP * lastRow + BLACKLIST_Y;
        addRenderableWidget(toggleButton(centreX() - HALF_ICON, y, filter::isBlacklist, LIST_FACE));
    }

    private void addAreaControls() {
        for (int row = 0; row < AREA_BUTTON_Y.length; row++) {
            String axis = AXIS_NAMES[AREA_ROW_AXIS[row]];
            int shrinkId = MenuSealBase.BUTTON_AREA_BASE + 2 * row;
            int y = centreY() + AREA_BUTTON_Y[row];
            addStepper(false, centreX() - STEP_BACK_X, y, Component.translatable("gui.thaumaturge.seal.area_shrink", axis), shrinkId);
            addStepper(true, centreX() + STEP_FORWARD_X, y, Component.translatable("gui.thaumaturge.seal.area_grow", axis), shrinkId + 1);
        }
    }

    private static int settingHalfSpacing(int count) {
        if (count <= 3) {
            return 8;
        }
        if (count <= 5) {
            return 7;
        }
        return count <= 8 ? 6 : 5;
    }

    private int settingIndent(List<SealSetting> settings) {
        int indent = CHECK_SIZE;
        for (SealSetting setting : settings) {
            int labelWidth = Math.min(LABEL_LIMIT, font.width(Component.translatable(setting.nameKey())));
            indent = Math.max(indent, (CHECK_SIZE + labelWidth) / 2);
        }
        return indent;
    }

    private void addSettingControls(ISealEntity seal) {
        List<SealSetting> settings = seal.type().settings();
        if (settings.isEmpty()) {
            return;
        }
        int halfSpacing = settingHalfSpacing(settings.size());
        int span = (settings.size() - 1) * halfSpacing;
        int x = centreX() - settingIndent(settings);
        int top = centreY() - CHECK_ROW_BASE - span;
        for (int index = 0; index < settings.size(); index++) {
            SealSetting setting = settings.get(index);
            int settingIndex = index;
            addRenderableWidget(new PropButton(x, top + 2 * halfSpacing * index, setting, () -> seal.setting(setting),
                    () -> sendButton((seal.setting(setting) ? MenuSealBase.BUTTON_TOGGLE_OFF_BASE : MenuSealBase.BUTTON_TOGGLE_ON_BASE) + settingIndex)));
        }
    }

    private void addTagButtons(List<Holder<GolemTrait>> traits, int rowOffset) {
        int halfRun = (traits.size() - 1) * TRAIT_PITCH / 2;
        for (int slot = 0; slot < traits.size(); slot++) {
            GolemTrait trait = traits.get(slot).value();
            TTHoverButton button = TTHoverButton.centered(centreX() + slot * TRAIT_PITCH - halfRun, centreY() + rowOffset, ICON, new TTButtonIcon.TextureIcon(trait.icon()),
                    Component.translatable(GolemTrait.nameKey(TTGolemTraits.registry().getKey(trait))), () -> {
                    });
            button.setDescription(Component.translatable(GolemTrait.descriptionKey(TTGolemTraits.registry().getKey(trait))));
            addRenderableWidget(button);
        }
    }

    private void selectCategory(int index) {
        sendButton(index);
        rebuildCategoryWidgets();
    }

    private void sendButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            menu.clickMenuButton(minecraft.player, id);
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
        rebuildCategoryWidgets();
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
        rebuildCategoryWidgets();
    }

    private static void blitBase(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int size) {
        blitBase(graphics, x, y, u, v, size, size);
    }

    private static void blitBase(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int width, int height) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.GUI_BASE, x, y, u, v, width, height, ATLAS, ATLAS);
    }

    private static void blitBaseTinted(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int size, int tint) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.GUI_BASE, x, y, u, v, size, size, ATLAS, ATLAS, tint);
    }

    @Override
    protected void extractBackgroundTexture(GuiGraphicsExtractor graphics) {
        blitBase(graphics, centreX() - CIRCLE_HALF, centreY() - CIRCLE_HALF, CIRCLE_U, CIRCLE_V, CIRCLE_SIZE);
        blitBase(graphics, leftPos, topPos + PANEL_Y, 0, PANEL_V, PANEL_WIDTH, PANEL_HEIGHT);
    }

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ISealEntity seal = menu.seal();
        if (seal == null) {
            return;
        }
        drawLine(graphics, Component.translatable("gui.thaumaturge.seal.category." + menu.panel().ordinal()), TITLE_Y, WHITE);
        switch (menu.panel()) {
            case PRIORITY -> drawPriorityOverlay(graphics, seal, mouseX, mouseY);
            case FILTER -> seal.filter().ifPresent(filter -> drawFilterOverlay(graphics, filter));
            case AREA -> drawAreaOverlay(graphics);
            case TAGS -> drawTagCaptions(graphics);
            case TOGGLES -> {
            }
        }
    }

    private void drawPriorityOverlay(GuiGraphicsExtractor graphics, ISealEntity seal, int mouseX, int mouseY) {
        int color = menu.color();
        boolean dyed = color >= FIRST_DYE_COLOR && color <= LAST_DYE_COLOR;
        blitBase(graphics, centreX() + DIAL_X, centreY() + DIAL_Y, COLOR_DIAL_U, COLOR_DIAL_V, DIAL_SIZE);
        if (dyed) {
            int tint = ARGB.opaque(DyeColor.byId(color - 1).getTextureDiffuseColor());
            blitBaseTinted(graphics, centreX() + SWATCH_X, centreY() + SWATCH_Y, COLOR_SWATCH_U, COLOR_SWATCH_V, SWATCH_SIZE, tint);
        }
        int localX = mouseX - leftPos;
        int localY = mouseY - topPos;
        boolean overDial = localX >= MIDDLE_X + HOVER_LEFT && localX <= MIDDLE_X + HOVER_RIGHT && localY >= MIDDLE_Y + HOVER_TOP && localY <= MIDDLE_Y + HOVER_BOTTOM;
        if (overDial) {
            Component name = dyed
                    ? Component.translatable("gui.thaumaturge.seal.setting.color", Component.translatable("color.minecraft." + DyeColor.byId(color - 1).getName()))
                    : Component.translatable("gui.thaumaturge.seal.setting.colorall");
            drawCentered(graphics, name.getString(), centreX() + COLOR_LABEL_X, centreY() + COLOR_LABEL_Y, WHITE);
        }
        drawLine(graphics, Component.translatable("gui.thaumaturge.seal.setting.priority"), PRIORITY_CAPTION_Y, LABEL_BLUE);
        drawCentered(graphics, String.valueOf(menu.priority()), centreX(), centreY() + PRIORITY_VALUE_Y, WHITE);
        if (isOwner(seal)) {
            drawLine(graphics, Component.translatable("gui.thaumaturge.seal.setting.owner"), OWNER_LABEL_Y, LABEL_BLUE);
        }
    }

    private void drawFilterOverlay(GuiGraphicsExtractor graphics, ISealFilter filter) {
        int size = filter.spec().slots();
        int originX = FILTER_ORIGIN_BASE + (size - 1) % FILTER_COLUMNS * FILTER_ORIGIN_STEP;
        int originY = FILTER_ORIGIN_BASE + (size - 1) / FILTER_COLUMNS * FILTER_ORIGIN_STEP;
        for (int cell = 0; cell < size; cell++) {
            int frameX = centreX() + cell % FILTER_COLUMNS * FILTER_CELL_STEP - originX;
            int frameY = centreY() + cell / FILTER_COLUMNS * FILTER_CELL_STEP - originY;
            blitBase(graphics, frameX, frameY, FILTER_FRAME_U, FILTER_FRAME_V, FILTER_FRAME_SIZE);
        }
        if (filter.isBlacklist()) {
            return;
        }
        for (int index = 0; index < menu.filterSlotCount(); index++) {
            Slot slot = menu.slots.get(index);
            if (slot.isActive() && !slot.getItem().isEmpty()) {
                drawLimit(graphics, slot, filter.limit(index));
            }
        }
    }

    private void drawLimit(GuiGraphicsExtractor graphics, Slot slot, int limit) {
        String text = limit == 0 ? "*" : String.valueOf(limit);
        int color = ARGB.opaque(limit == 0 ? LIMIT_ANY_COLOR : WHITE);
        graphics.text(font, text, leftPos + slot.x + LIMIT_RIGHT_EDGE - font.width(text), topPos + slot.y + LIMIT_TOP, color, true);
    }

    private void drawAreaOverlay(GuiGraphicsExtractor graphics) {
        int[] extents = {menu.area().getY(), menu.area().getX(), menu.area().getZ()};
        for (int row = 0; row < extents.length; row++) {
            drawLine(graphics, Component.translatable("gui.thaumaturge.seal.caption." + AREA_CAPTION_KEYS[row]), AREA_CAPTION_Y[row], LABEL_GREY);
        }
        for (int row = 0; row < extents.length; row++) {
            drawCentered(graphics, String.valueOf(extents[row]), centreX(), centreY() + AREA_VALUE_Y[row], WHITE);
        }
    }

    private void drawTagCaptions(GuiGraphicsExtractor graphics) {
        drawLine(graphics, Component.translatable("gui.thaumaturge.seal.caption.required"), REQUIRED_CAPTION_Y, LABEL_GREY);
        drawLine(graphics, Component.translatable("gui.thaumaturge.seal.caption.forbidden"), FORBIDDEN_CAPTION_Y, LABEL_GREY);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {}

    private void drawLine(GuiGraphicsExtractor graphics, Component text, int offsetY, int color) {
        drawCentered(graphics, text.getString(), centreX(), centreY() + offsetY, color);
    }

    private void drawCentered(GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
        graphics.text(font, text, x - font.width(text) / 2, y, ARGB.opaque(color), true);
    }

    private record ToggleFace(int onU, int offU, String onKey, String offKey, int onClick, int offClick) {
        int uv(boolean on) {
            return on ? onU : offU;
        }

        Component message(boolean on) {
            return Component.translatable(on ? onKey : offKey);
        }

        int click(boolean on) {
            return on ? onClick : offClick;
        }
    }

    static final class CategoryButton extends TTButton {
        private static final float IDLE_SHADE = 0.7F;
        private static final float HOVER_SHADE = 0.9F;

        private final int page;
        private final boolean selected;

        CategoryButton(int x, int y, int page, boolean selected, Runnable onPress) {
            super(x, y, ICON, ICON, Component.translatable("gui.thaumaturge.seal.category." + page), onPress);
            this.page = page;
            this.selected = selected;
            setDescription(Component.translatable("gui.thaumaturge.seal.category." + page + ".desc"));
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            float shade = selected ? 1.0F : isHovered() ? HOVER_SHADE : IDLE_SHADE;
            blitBaseTinted(graphics, getX(), getY(), page * ICON, CATEGORY_ICON_V, ICON, ARGB.colorFromFloat(shade, shade, shade, shade));
        }
    }

    static final class StateButton extends TTButton {
        private final IntSupplier uv;
        private final Supplier<Component> messageSupplier;

        StateButton(int x, int y, IntSupplier uv, Supplier<Component> messageSupplier, Runnable onPress) {
            super(x, y, ICON, ICON, messageSupplier.get(), onPress);
            this.uv = uv;
            this.messageSupplier = messageSupplier;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            setMessage(messageSupplier.get());
            blitBaseTinted(graphics, getX(), getY(), uv.getAsInt(), TOGGLE_V, ICON, activeTintColor(tintColor(), isHovered(), active));
        }
    }

    final class PropButton extends TTButton {
        private final SealSetting setting;
        private final BooleanSupplier value;

        PropButton(int x, int y, SealSetting setting, BooleanSupplier value, Runnable onPress) {
            super(x, y, PROP_SIZE, PROP_SIZE, Component.translatable(setting.nameKey()), onPress);
            this.setting = setting;
            this.value = value;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int frameX = getX() - PROP_FRAME_MARGIN;
            int frameY = getY() - PROP_FRAME_MARGIN;
            blitBase(graphics, frameX, frameY, PROP_BG_U, PROP_BG_V, PROP_FRAME_SIZE);
            if (value.getAsBoolean()) {
                blitBase(graphics, frameX, frameY, PROP_CHECK_U, PROP_BG_V, PROP_FRAME_SIZE);
            }
            graphics.text(font, Component.translatable(setting.nameKey()).getString(), getX() + PROP_LABEL_X, getY(), PROP_LABEL_COLOR, true);
        }
    }
}
