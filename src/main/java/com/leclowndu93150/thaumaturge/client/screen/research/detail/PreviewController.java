package com.leclowndu93150.thaumaturge.client.screen.research.detail;

import com.leclowndu93150.thaumaturge.client.render.research.RecipeDisplayWidget;
import com.leclowndu93150.thaumaturge.client.screen.widget.TTPlusMinusButton;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public final class PreviewController {
    private static final int ALL_LAYERS = -1;
    private static final int ROTATION_HALF_WIDTH = 48;
    private static final int ROTATION_HALF_HEIGHT = 50;
    private static final int CENTER_LIFT = 12;
    private static final int CONTROLS_Y_OFFSET = 48;
    private static final int LABEL_Y_OFFSET = 49;
    private static final int PREVIOUS_BUTTON_GAP = 16;
    private static final int NEXT_BUTTON_GAP = 6;
    private static final int LABEL_COLOR = 0xFF505050;

    private @Nullable TTPlusMinusButton previousLayer;
    private @Nullable TTPlusMinusButton nextLayer;
    private int layerCount;
    private int centerX;
    private int centerY;
    private int visibleLayer = ALL_LAYERS;
    private float rotation = Float.NaN;
    private float rotationOffset;
    private boolean rotating;

    public void bind(TTPlusMinusButton previous, TTPlusMinusButton next) {
        previousLayer = previous;
        nextLayer = next;
        hideButtons();
    }

    public void beginFrame() {
        hideButtons();
        layerCount = 0;
    }

    private void hideButtons() {
        if (previousLayer != null && nextLayer != null) {
            previousLayer.visible = false;
            nextLayer.visible = false;
        }
    }

    public int visibleLayer() {
        return visibleLayer;
    }

    public void configure(GuiGraphicsExtractor graphics, Font font, int cx, int cy, int layers) {
        layerCount = layers;
        centerX = cx;
        centerY = cy - CENTER_LIFT;
        if (layers <= 1 || previousLayer == null || nextLayer == null) {
            return;
        }
        if (visibleLayer >= layers) {
            visibleLayer = ALL_LAYERS;
        }
        Component label = visibleLayer < 0
                ? Component.translatable("gui.thaumaturge.thaumonomicon.preview.all_layers")
                : Component.translatable("gui.thaumaturge.thaumonomicon.preview.layers", visibleLayer + 1, layers);
        int halfWidth = font.width(label) / 2;
        previousLayer.setPosition(cx - halfWidth - PREVIOUS_BUTTON_GAP, cy + CONTROLS_Y_OFFSET);
        nextLayer.setPosition(cx + halfWidth + NEXT_BUTTON_GAP, cy + CONTROLS_Y_OFFSET);
        previousLayer.visible = true;
        nextLayer.visible = true;
        graphics.text(font, label, cx - halfWidth, cy + LABEL_Y_OFFSET, LABEL_COLOR, false);
    }

    public void shiftLayer(int delta) {
        visibleLayer = Math.floorMod(visibleLayer + 1 + delta, layerCount + 1) - 1;
    }

    public void reset() {
        visibleLayer = ALL_LAYERS;
        rotation = Float.NaN;
        rotationOffset = 0;
        rotating = false;
    }

    public float currentRotation() {
        return Float.isNaN(rotation) ? RecipeDisplayWidget.automaticRotation() + rotationOffset : rotation;
    }

    public boolean isRotating() {
        return rotating;
    }

    public void beginRotation() {
        rotation = currentRotation();
        rotating = true;
    }

    public void dragRotation(double dragX) {
        rotation += (float) dragX;
    }

    public void endRotation() {
        rotating = false;
        rotationOffset = rotation - RecipeDisplayWidget.automaticRotation();
        rotation = Float.NaN;
    }

    public @Nullable Rect rotationArea() {
        if (layerCount <= 0) {
            return null;
        }
        return new Rect(centerX - ROTATION_HALF_WIDTH, centerY - ROTATION_HALF_HEIGHT, ROTATION_HALF_WIDTH * 2, ROTATION_HALF_HEIGHT * 2);
    }

    public List<Rect> layerButtonAreas() {
        List<Rect> areas = new ArrayList<>();
        addVisibleArea(areas, previousLayer);
        addVisibleArea(areas, nextLayer);
        return areas;
    }

    private static void addVisibleArea(List<Rect> areas, @Nullable AbstractWidget button) {
        if (button != null && button.visible) {
            areas.add(new Rect(button.getX(), button.getY(), button.getWidth(), button.getHeight()));
        }
    }
}
