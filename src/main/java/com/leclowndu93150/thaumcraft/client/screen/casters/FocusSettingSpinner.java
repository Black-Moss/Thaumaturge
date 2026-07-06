package com.leclowndu93150.thaumcraft.client.screen.casters;

import com.leclowndu93150.thaumcraft.api.casters.NodeSetting;
import com.leclowndu93150.thaumcraft.client.screen.TCScreenTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public final class FocusSettingSpinner extends AbstractWidget {
    private static final int ARROW_SIZE = 10;
    private static final int U_MINUS = 20;
    private static final int U_PLUS = 30;
    private static final int V_ARROWS = 0;
    private static final int ATLAS = 256;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private final NodeSetting setting;
    private final Runnable onChange;

    public FocusSettingSpinner(int x, int y, int width, NodeSetting setting, Runnable onChange) {
        super(x, y, width + ARROW_SIZE, ARROW_SIZE, Component.empty());
        this.setting = setting;
        this.onChange = onChange;
        setTooltip(Tooltip.create(setting.getName()));
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int bodyWidth = this.width - ARROW_SIZE;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.GUI_BASE,
                getX(), getY(), U_MINUS, V_ARROWS, ARROW_SIZE, ARROW_SIZE, ATLAS, ATLAS);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.GUI_BASE,
                getX() + bodyWidth, getY(), U_PLUS, V_ARROWS, ARROW_SIZE, ARROW_SIZE, ATLAS, ATLAS);
        Component value = setting.getValueText();
        var font = Minecraft.getInstance().font;
        graphics.text(font, value,
                getX() + (bodyWidth + ARROW_SIZE) / 2 - font.width(value) / 2,
                getY() + 1, TEXT_COLOR, true);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        int bodyWidth = this.width - ARROW_SIZE;
        if (event.x() < getX() + ARROW_SIZE) {
            setting.decrement();
            onChange.run();
        } else if (event.x() >= getX() + bodyWidth) {
            setting.increment();
            onChange.run();
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
