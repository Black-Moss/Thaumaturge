package com.leclowndu93150.thaumcraft.client.toast;

import com.leclowndu93150.thaumcraft.client.render.research.EntryIconRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class ResearchToast implements Toast {
    private static final long DURATION_MS = 5000L;
    private static final Identifier TOAST_SPRITE = Identifier.withDefaultNamespace("toast/recipe");

    private final Component title;
    private final Component subtitle;
    private final Object icon;
    private final Identifier id;
    private Toast.Visibility wantedVisibility = Visibility.SHOW;

    public ResearchToast(Identifier researchId, Component title, Component subtitle, Object icon) {
        this.id = researchId;
        this.title = title;
        this.subtitle = subtitle;
        this.icon = icon;
    }

    @Override
    public Visibility getWantedVisibility() {
        return wantedVisibility;
    }

    @Override
    public void update(ToastManager manager, long fullyVisibleForMs) {
        wantedVisibility = fullyVisibleForMs > DURATION_MS ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long fullyVisibleForMs) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TOAST_SPRITE, 0, 0, width(), height());
        graphics.text(font, title, 30, 7, 0xFF552200, false);
        graphics.text(font, subtitle, 30, 18, 0xFF000000, false);
        EntryIconRenderer.drawResearchIcon(graphics, 8, 8, icon, false);
    }

    @Override
    public Object getToken() {
        return id;
    }
}
