package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.api.items.IHoverGear;
import com.leclowndu93150.thaumaturge.client.hud.hover.FuelFraction;
import com.leclowndu93150.thaumaturge.client.hud.hover.HoverGaugeLayout;
import com.leclowndu93150.thaumaturge.client.hud.hover.HoverGaugePainter;
import com.leclowndu93150.thaumaturge.client.hud.hover.HoverGaugeState;
import com.leclowndu93150.thaumaturge.content.equipment.hover.HoverManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.GuiLayer;

public final class HoverHudOverlay implements GuiLayer {
    private final HoverGaugePainter painter = new HoverGaugePainter(HoverGaugeLayout.STANDARD);

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui || mc.screen != null) {
            return;
        }
        ItemStack gear = HoverManager.wornHoverGear(player);
        if (!(gear.getItem() instanceof IHoverGear hoverGear)) {
            return;
        }
        float fraction = FuelFraction.of(hoverGear.getHoverFuel(gear), hoverGear.getMaxHoverFuel(gear));
        int fillHeight = FuelFraction.pixels(fraction, painter.layout().fillMaxHeight());
        painter.paint(graphics, new HoverGaugeState(gear, graphics.guiHeight() / 2, fillHeight, HoverManager.isHovering(player), player.tickCount));
    }
}
