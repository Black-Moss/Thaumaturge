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
import net.minecraft.core.Holder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
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

    private final int middleX;
    private final int middleY;

    public SealScreen(MenuSealBase menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TTScreenTextures.GUI_BASE, IMAGE_WIDTH, IMAGE_HEIGHT);
        this.middleX = IMAGE_WIDTH / 2;
        this.middleY = (IMAGE_HEIGHT - 72) / 2 - 8;
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
        int count = panels.size();
        double toggleAngle = LONE_TOGGLE_ANGLE;
        if (count > 1) {
            double step = Mth.clamp(TAB_SPREAD / count, TAB_STEP_MIN, TAB_STEP_MAX);
            double first = TAB_LEFT_ANGLE + TAB_CENTRING * (count - 1) / count;
            for (int k = 0; k < count; k++) {
                SealPanel panel = panels.get(k);
                int index = k;
                boolean selected = panel == menu.panel();
                addRenderableWidget(new CategoryButton(tabX(first - k * step) - HALF_ICON, tabY(first - k * step) - HALF_ICON, panel.ordinal(), selected, () -> {
                    if (!selected) {
                        selectCategory(index);
                    }
                }));
            }
            toggleAngle = first - count * step;
        }
        addRenderableWidget(new StateButton(tabX(toggleAngle) - HALF_ICON, tabY(toggleAngle) - HALF_ICON, () -> seal.isRedstoneControlled() ? REDSTONE_U_ON : REDSTONE_U_OFF,
                () -> Component.translatable(seal.isRedstoneControlled() ? "golem.prop.redon" : "golem.prop.redoff"),
                () -> sendButton(seal.isRedstoneControlled() ? MenuSealBase.BUTTON_REDSTONE_OFF : MenuSealBase.BUTTON_REDSTONE_ON)));
    }

    private int tabX(double degrees) {
        return leftPos + middleX + (int) (TAB_RADIUS * Math.cos(Math.toRadians(degrees)));
    }

    private int tabY(double degrees) {
        return topPos + middleY + (int) (TAB_RADIUS * Math.sin(Math.toRadians(degrees)));
    }

    private void addPriorityControls(ISealEntity seal) {
        int x = leftPos + middleX;
        int y = topPos + middleY;
        addRenderableWidget(
                TTPlusMinusButton.minus(x - STEP_BACK_X, y + PRIORITY_BUTTON_Y, Component.translatable("gui.thaumaturge.seal.priority_down"), () -> sendButton(MenuSealBase.BUTTON_PRIORITY_DOWN)));
        addRenderableWidget(
                TTPlusMinusButton.plus(x + STEP_FORWARD_X, y + PRIORITY_BUTTON_Y, Component.translatable("gui.thaumaturge.seal.priority_up"), () -> sendButton(MenuSealBase.BUTTON_PRIORITY_UP)));
        addRenderableWidget(
                TTPlusMinusButton.minus(x + COLOR_DOWN_X, y + COLOR_BUTTON_Y, Component.translatable("gui.thaumaturge.seal.color_previous"), () -> sendButton(MenuSealBase.BUTTON_COLOR_DOWN)));
        addRenderableWidget(TTPlusMinusButton.plus(x + COLOR_UP_X, y + COLOR_BUTTON_Y, Component.translatable("gui.thaumaturge.seal.color_next"), () -> sendButton(MenuSealBase.BUTTON_COLOR_UP)));
        if (minecraft != null && minecraft.player != null && minecraft.player.getUUID().equals(seal.owner())) {
            addRenderableWidget(
                    new StateButton(x + LOCK_X, y, () -> seal.isLocked() ? LOCK_U_LOCKED : LOCK_U_UNLOCKED, () -> Component.translatable(seal.isLocked() ? "golem.prop.lock" : "golem.prop.unlock"),
                            () -> sendButton(seal.isLocked() ? MenuSealBase.BUTTON_UNLOCK : MenuSealBase.BUTTON_LOCK)));
        }
    }

    private void addFilterControls(ISealFilter filter) {
        int lastRow = (filter.spec().slots() - 1) / FILTER_COLUMNS;
        addRenderableWidget(new StateButton(leftPos + middleX - HALF_ICON, topPos + middleY + FILTER_ROW_STEP * lastRow + BLACKLIST_Y, () -> filter.isBlacklist() ? BLACKLIST_U : WHITELIST_U,
                () -> Component.translatable(filter.isBlacklist() ? "golem.prop.blacklist" : "golem.prop.whitelist"),
                () -> sendButton(filter.isBlacklist() ? MenuSealBase.BUTTON_BLACKLIST_OFF : MenuSealBase.BUTTON_BLACKLIST_ON)));
    }

    private void addAreaControls() {
        for (int row = 0; row < AREA_BUTTON_Y.length; row++) {
            int axis = AREA_ROW_AXIS[row];
            int shrink = MenuSealBase.BUTTON_AREA_BASE + 2 * row;
            int y = topPos + middleY + AREA_BUTTON_Y[row];
            addRenderableWidget(TTPlusMinusButton.minus(leftPos + middleX - STEP_BACK_X, y, Component.translatable("gui.thaumaturge.seal.area_shrink", AXIS_NAMES[axis]), () -> sendButton(shrink)));
            addRenderableWidget(
                    TTPlusMinusButton.plus(leftPos + middleX + STEP_FORWARD_X, y, Component.translatable("gui.thaumaturge.seal.area_grow", AXIS_NAMES[axis]), () -> sendButton(shrink + 1)));
        }
    }

    private void addSettingControls(ISealEntity seal) {
        List<SealSetting> settings = seal.type().settings();
        int count = settings.size();
        if (count == 0) {
            return;
        }
        int halfSpacing = count <= 3 ? 8 : count <= 5 ? 7 : count <= 8 ? 6 : 5;
        int span = (count - 1) * halfSpacing;
        int indent = CHECK_SIZE;
        for (SealSetting setting : settings) {
            indent = Math.max(indent, (CHECK_SIZE + Math.min(LABEL_LIMIT, font.width(Component.translatable(setting.nameKey())))) / 2);
        }
        for (int i = 0; i < count; i++) {
            SealSetting setting = settings.get(i);
            int index = i;
            addRenderableWidget(new PropButton(leftPos + middleX - indent, topPos + middleY - CHECK_ROW_BASE - span + 2 * halfSpacing * i, setting, () -> seal.setting(setting),
                    () -> sendButton((seal.setting(setting) ? MenuSealBase.BUTTON_TOGGLE_OFF_BASE : MenuSealBase.BUTTON_TOGGLE_ON_BASE) + index)));
        }
    }

    private void addTagButtons(List<Holder<GolemTrait>> tags, int centreY) {
        for (int p = 0; p < tags.size(); p++) {
            GolemTrait tag = tags.get(p).value();
            TTHoverButton button = TTHoverButton.centered(leftPos + middleX + p * TRAIT_PITCH - (tags.size() - 1) * TRAIT_PITCH / 2, topPos + middleY + centreY, ICON,
                    new TTButtonIcon.TextureIcon(tag.icon()), Component.translatable(GolemTrait.nameKey(TTGolemTraits.registry().getKey(tag))), () -> {
                    });
            button.setDescription(Component.translatable(GolemTrait.descriptionKey(TTGolemTraits.registry().getKey(tag))));
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

    @Override
    protected void extractBackgroundTexture(GuiGraphicsExtractor graphics) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.GUI_BASE, leftPos + middleX - 80, topPos + middleY - 80, CIRCLE_U, CIRCLE_V, CIRCLE_SIZE, CIRCLE_SIZE, ATLAS, ATLAS);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.GUI_BASE, leftPos, topPos + PANEL_Y, 0, PANEL_V, PANEL_WIDTH, PANEL_HEIGHT, ATLAS, ATLAS);
    }

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ISealEntity seal = menu.seal();
        if (seal == null) {
            return;
        }
        drawCentered(graphics, Component.translatable("button.category." + menu.panel().ordinal()).getString(), leftPos + middleX, topPos + middleY - 64, WHITE);
        switch (menu.panel()) {
            case PRIORITY -> {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.GUI_BASE, leftPos + middleX + 17, topPos + middleY + 3, COLOR_DIAL_U, COLOR_DIAL_V, 12, 12, ATLAS, ATLAS);
                if (menu.color() >= 1 && menu.color() <= 16) {
                    int dye = DyeColor.byId(menu.color() - 1).getTextureDiffuseColor();
                    graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.GUI_BASE, leftPos + middleX + 20, topPos + middleY + 6, COLOR_SWATCH_U, COLOR_SWATCH_V, 6, 6, ATLAS, ATLAS,
                            ARGB.opaque(dye));
                }
                int mx = mouseX - leftPos;
                int my = mouseY - topPos;
                if (mx >= middleX + 5 && mx <= middleX + 41 && my >= middleY + 3 && my <= middleY + 15) {
                    String label = menu.color() >= 1 && menu.color() <= 16
                            ? Component.translatable("golem.prop.color", Component.translatable("color.minecraft." + DyeColor.byId(menu.color() - 1).getName())).getString()
                            : Component.translatable("golem.prop.colorall").getString();
                    drawCentered(graphics, label, leftPos + middleX + 23, topPos + middleY + 17, WHITE);
                }
                drawCentered(graphics, Component.translatable("golem.prop.priority").getString(), leftPos + middleX, topPos + middleY - 28, LABEL_BLUE);
                drawCentered(graphics, String.valueOf(menu.priority()), leftPos + middleX, topPos + middleY - 16, WHITE);
                if (minecraft != null && minecraft.player != null && minecraft.player.getUUID().equals(seal.owner())) {
                    drawCentered(graphics, Component.translatable("golem.prop.owner").getString(), leftPos + middleX, topPos + middleY + 32, LABEL_BLUE);
                }
            }
            case FILTER -> {
                ISealFilter filter = seal.filter().orElse(null);
                if (filter != null) {
                    int size = filter.spec().slots();
                    int offsetX = 16 + (size - 1) % 3 * 12;
                    int offsetY = 16 + (size - 1) / 3 * 12;
                    for (int a = 0; a < size; a++) {
                        int x = a % 3;
                        int y = a / 3;
                        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.GUI_BASE, leftPos + middleX + x * 24 - offsetX, topPos + middleY + y * 24 - offsetY, FILTER_FRAME_U,
                                FILTER_FRAME_V, FILTER_FRAME_SIZE, FILTER_FRAME_SIZE, ATLAS, ATLAS);
                    }
                    if (!filter.isBlacklist()) {
                        for (int i = 0; i < menu.filterSlotCount(); i++) {
                            Slot slot = menu.slots.get(i);
                            if (slot.isActive() && !slot.getItem().isEmpty()) {
                                int limit = filter.limit(i);
                                String text = limit == 0 ? "*" : String.valueOf(limit);
                                graphics.text(font, text, leftPos + slot.x + 17 - font.width(text), topPos + slot.y + 9, ARGB.opaque(limit == 0 ? 0xFFAA00 : WHITE), true);
                            }
                        }
                    }
                }
            }
            case AREA -> {
                drawCentered(graphics, Component.translatable("button.caption.y").getString(), leftPos + middleX, topPos + middleY - 33, LABEL_GREY);
                drawCentered(graphics, Component.translatable("button.caption.x").getString(), leftPos + middleX, topPos + middleY - 9, LABEL_GREY);
                drawCentered(graphics, Component.translatable("button.caption.z").getString(), leftPos + middleX, topPos + middleY + 15, LABEL_GREY);
                drawCentered(graphics, String.valueOf(menu.area().getY()), leftPos + middleX, topPos + middleY - 24, WHITE);
                drawCentered(graphics, String.valueOf(menu.area().getX()), leftPos + middleX, topPos + middleY, WHITE);
                drawCentered(graphics, String.valueOf(menu.area().getZ()), leftPos + middleX, topPos + middleY + 24, WHITE);
            }
            case TAGS -> {
                drawCentered(graphics, Component.translatable("button.caption.required").getString(), leftPos + middleX, topPos + middleY - 26, LABEL_GREY);
                drawCentered(graphics, Component.translatable("button.caption.forbidden").getString(), leftPos + middleX, topPos + middleY + 6, LABEL_GREY);
            }
            case TOGGLES -> {
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {}

    private void drawCentered(GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
        graphics.text(font, text, x - font.width(text) / 2, y, ARGB.opaque(color), true);
    }

    interface UvSupplier {
        int get();
    }

    interface MessageSupplier {
        Component get();
    }

    static final class CategoryButton extends TTButton {
        private static final float IDLE_SHADE = 0.7F;
        private static final float HOVER_SHADE = 0.9F;

        private final int page;
        private final boolean selected;

        CategoryButton(int x, int y, int page, boolean selected, Runnable onPress) {
            super(x, y, ICON, ICON, Component.translatable("button.category." + page), onPress);
            this.page = page;
            this.selected = selected;
            setDescription(Component.translatable("button.category." + page + ".desc"));
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            float shade = selected ? 1.0F : isHovered() ? HOVER_SHADE : IDLE_SHADE;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.GUI_BASE, getX(), getY(), page * ICON, CATEGORY_ICON_V, ICON, ICON, ATLAS, ATLAS,
                    ARGB.colorFromFloat(shade, shade, shade, shade));
        }
    }

    static final class StateButton extends TTButton {
        private final UvSupplier uv;
        private final MessageSupplier messageSupplier;

        StateButton(int x, int y, UvSupplier uv, MessageSupplier messageSupplier, Runnable onPress) {
            super(x, y, 16, 16, messageSupplier.get(), onPress);
            this.uv = uv;
            this.messageSupplier = messageSupplier;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            setMessage(messageSupplier.get());
            graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.GUI_BASE, getX(), getY(), uv.get(), TOGGLE_V, 16, 16, ATLAS, ATLAS, activeTintColor(tintColor(), isHovered(), active));
        }
    }

    final class PropButton extends TTButton {
        private final SealSetting setting;
        private final BooleanSupplier value;

        PropButton(int x, int y, SealSetting setting, BooleanSupplier value, Runnable onPress) {
            super(x, y, 8, 8, Component.translatable(setting.nameKey()), onPress);
            this.setting = setting;
            this.value = value;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.GUI_BASE, getX() - 2, getY() - 2, PROP_BG_U, PROP_BG_V, 12, 12, ATLAS, ATLAS);
            if (value.getAsBoolean()) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.GUI_BASE, getX() - 2, getY() - 2, PROP_CHECK_U, PROP_BG_V, 12, 12, ATLAS, ATLAS);
            }
            graphics.text(font, Component.translatable(setting.nameKey()).getString(), getX() + 12, getY(), 0xFFFFFFFF, true);
        }
    }
}
