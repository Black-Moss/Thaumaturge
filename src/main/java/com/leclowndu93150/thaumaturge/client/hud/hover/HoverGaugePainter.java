package com.leclowndu93150.thaumaturge.client.hud.hover;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public final class HoverGaugePainter {
    private final HoverGaugeLayout layout;
    private final List<HoverGaugeStep> steps;

    public HoverGaugePainter(HoverGaugeLayout layout) {
        this.layout = layout;
        this.steps = List.of(this::drawFill, this::drawFrame, this::drawSpark, this::drawIcon);
    }

    public HoverGaugeLayout layout() {
        return layout;
    }

    public void paint(GuiGraphicsExtractor graphics, HoverGaugeState state) {
        for (HoverGaugeStep step : steps) {
            step.draw(graphics, state);
        }
    }

    private void drawFill(GuiGraphicsExtractor graphics, HoverGaugeState state) {
        int height = state.fillHeight();
        if (height <= 0) {
            return;
        }
        GaugePart part = layout.fill();
        SourceRect source = part.source();
        graphics.blit(RenderPipelines.GUI_TEXTURED, part.texture(), part.anchor().x(), state.middle() + part.anchor().yFromMiddle() - height, source.u(), source.v() + source.height() - height,
                source.width(), height, source.width(), height, part.textureWidth(), part.textureHeight(), part.tint());
    }

    private void drawFrame(GuiGraphicsExtractor graphics, HoverGaugeState state) {
        GaugePart part = layout.frame();
        SourceRect source = part.source();
        graphics.blit(RenderPipelines.GUI_TEXTURED, part.texture(), part.anchor().x(), state.middle() + part.anchor().yFromMiddle(), source.u(), source.v(), source.width(), source.height(),
                source.width(), source.height(), part.textureWidth(), part.textureHeight(), part.tint());
    }

    private void drawSpark(GuiGraphicsExtractor graphics, HoverGaugeState state) {
        if (!state.hovering()) {
            return;
        }
        GaugePart part = layout.spark();
        SourceRect source = part.source();
        int frame = state.tickCount() % layout.sparkFrames();
        graphics.blit(RenderPipelines.GUI_TEXTURED, part.texture(), part.anchor().x(), state.middle() + part.anchor().yFromMiddle(), frame * source.width(), source.v(), source.width(),
                source.height(), source.width(), source.height(), part.textureWidth(), part.textureHeight(), part.tint());
    }

    private void drawIcon(GuiGraphicsExtractor graphics, HoverGaugeState state) {
        Anchor icon = layout.icon();
        graphics.item(state.gear(), icon.x(), state.middle() + icon.yFromMiddle());
    }
}
