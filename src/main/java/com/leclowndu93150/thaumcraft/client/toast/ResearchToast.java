package com.leclowndu93150.thaumcraft.client.toast;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ResearchToast implements Toast {
    private static final long DURATION_MS = 5000L;
    private static final Identifier TOAST_SPRITE = Identifier.withDefaultNamespace("toast/recipe");

    private final Component title;
    private final Component subtitle;
    private final ItemStack icon;
    private final Identifier id;
    private Toast.Visibility wantedVisibility = Visibility.SHOW;

    public ResearchToast(Identifier researchId, Component title, Component subtitle, ItemStack icon) {
        this.id = researchId;
        this.title = title;
        this.subtitle = subtitle;
        this.icon = icon == null || icon.isEmpty() ? new ItemStack(Items.PAPER) : icon;
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
        graphics.item(icon, 8, 8);
    }

    @Override
    public Object getToken() {
        return id;
    }
}
