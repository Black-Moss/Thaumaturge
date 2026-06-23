package com.leclowndu93150.thaumcraft.client.hud;

import com.leclowndu93150.thaumcraft.api.items.GogglesAccess;
import com.leclowndu93150.thaumcraft.client.aura.ClientAuraCache;
import com.leclowndu93150.thaumcraft.content.item.ThaumometerItem;
import com.leclowndu93150.thaumcraft.network.ServerboundRequestAuraChunkPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@OnlyIn(Dist.CLIENT)
public final class AuraHudOverlay implements GuiLayer {
    private static final int X_PADDING = 6;
    private static final int Y_PADDING = 6;
    private static final int LINE_SPACING = 10;
    private static final int COLOR_TITLE = 0xFFB890FF;
    private static final int COLOR_VIS = 0xFFFFFFFF;
    private static final int COLOR_FLUX = 0xFFFF7070;
    private static final int COLOR_BASE = 0xFFA0A0A0;

    public AuraHudOverlay() {}

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }
        if (!shouldShow(player)) {
            return;
        }
        ClientAuraCache.tick();
        ChunkPos pos = ChunkPos.containing(player.blockPosition());
        if (ClientAuraCache.shouldRefresh(pos)) {
            ClientPacketDistributor.sendToServer(new ServerboundRequestAuraChunkPayload(pos.x(), pos.z()));
        }
        ClientAuraCache.Snapshot snap = ClientAuraCache.get(pos);
        if (snap == null) {
            return;
        }
        Font font = mc.font;
        int x = X_PADDING;
        int y = Y_PADDING;
        Component title = Component.translatable("hud.thaumcraft.aura.title").withStyle(ChatFormatting.LIGHT_PURPLE);
        guiGraphics.text(font, title, x, y, COLOR_TITLE, false);
        y += LINE_SPACING;
        guiGraphics.text(font, Component.translatable("hud.thaumcraft.aura.vis", format(snap.vis())), x, y, COLOR_VIS, false);
        y += LINE_SPACING;
        guiGraphics.text(font, Component.translatable("hud.thaumcraft.aura.flux", format(snap.flux())), x, y, COLOR_FLUX, false);
        y += LINE_SPACING;
        guiGraphics.text(font, Component.translatable("hud.thaumcraft.aura.base", (int) snap.base()), x, y, COLOR_BASE, false);
    }

    private static boolean shouldShow(Player player) {
        if (GogglesAccess.wearsGoggles(player)) {
            return true;
        }
        ItemStack main = player.getMainHandItem();
        if (!main.isEmpty() && main.getItem() instanceof ThaumometerItem) {
            return true;
        }
        ItemStack off = player.getOffhandItem();
        return !off.isEmpty() && off.getItem() instanceof ThaumometerItem;
    }

    private static String format(float value) {
        int whole = (int) value;
        int tenths = Math.abs((int) (value * 10.0F) - whole * 10);
        return whole + "." + tenths;
    }
}
