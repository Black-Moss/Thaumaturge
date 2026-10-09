package com.leclowndu93150.thaumaturge.client.screen.widget;

import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class TTSlider extends AbstractWidget {
    private static final int HANDLE_SIZE = 8;
    private static final int HANDLE_U = 20;
    private static final int HANDLE_V = 20;
    private static final int TRACK_THICKNESS = 4;
    private static final int TRACK_LENGTH = 32;
    private static final int TRACK_OFFSET = 2;
    private static final int VERTICAL_TRACK_U = 240;
    private static final int HORIZONTAL_TRACK_U = 208;
    private static final int TRACK_V = 176;
    private static final int PRESS_INSET = HANDLE_SIZE / 2;

    private final Consumer<Float> onChange;
    private final boolean vertical;
    private final float floor;
    private float ceiling;
    private float fraction;

    public TTSlider(int x, int y, int width, int height, boolean vertical, float min, float max, float value, Consumer<Float> onChange) {
        super(x, y, width, height, Component.empty());
        floor = min;
        ceiling = max;
        this.onChange = onChange;
        this.vertical = vertical;
        fraction = fractionOf(value);
    }

    public void setValue(float value) {
        fraction = fractionOf(value);
    }

    public float value() {
        return Mth.lerp(fraction, floor, ceiling);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        drawTrack(graphics);
        drawHandle(graphics);
    }

    private int axisLength() {
        return vertical ? getHeight() : getWidth();
    }

    private void drawTrack(GuiGraphicsExtractor graphics) {
        int length = axisLength();
        int shiftX = vertical ? TRACK_OFFSET : 0;
        int shiftY = vertical ? 0 : TRACK_OFFSET;
        int u = vertical ? VERTICAL_TRACK_U : HORIZONTAL_TRACK_U;
        int drawW = vertical ? TRACK_THICKNESS : length;
        int drawH = vertical ? length : TRACK_THICKNESS;
        int sourceW = vertical ? TRACK_THICKNESS : TRACK_LENGTH;
        int sourceH = vertical ? TRACK_LENGTH : TRACK_THICKNESS;
        blitRegion(graphics, getX() + shiftX, getY() + shiftY, u, TRACK_V, drawW, drawH, sourceW, sourceH);
    }

    private void drawHandle(GuiGraphicsExtractor graphics) {
        int travel = (int) (fraction * (axisLength() - HANDLE_SIZE));
        int handleX = getX() + (vertical ? 0 : travel);
        int handleY = getY() + (vertical ? travel : 0);
        blitRegion(graphics, handleX, handleY, HANDLE_U, HANDLE_V, HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE);
    }

    private static void blitRegion(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int width, int height, int regionWidth, int regionHeight) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.GUI_BASE, x, y, (float) u, (float) v, width, height, regionWidth, regionHeight, TTScreenTextures.TEX_SIZE,
                TTScreenTextures.TEX_SIZE);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        track(event);
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        track(event);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    private void track(MouseButtonEvent event) {
        double pointer = vertical ? event.y() : event.x();
        int origin = vertical ? getY() : getX();
        double span = axisLength() - HANDLE_SIZE;
        fraction = span <= 0.0 ? 0.0F : (float) Mth.clamp((pointer - (origin + PRESS_INSET)) / span, 0.0, 1.0);
        onChange.accept(value());
    }

    public void setMax(float max) {
        fraction = 0.0F;
        ceiling = max;
    }

    public float max() {
        return ceiling;
    }

    private float fractionOf(float value) {
        float range = ceiling - floor;
        return ceiling <= floor ? 0.0F : Mth.clamp((value - floor) / range, 0.0F, 1.0F);
    }
}
