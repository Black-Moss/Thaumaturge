package com.leclowndu93150.thaumaturge.content.warp;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;

public final class WarpNotices {
    private WarpNotices() {}

    public static void send(ServerPlayer player, String key) {
        Component message = Component.translatable(key).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC);
        player.connection.send(new ClientboundSetActionBarTextPacket(message));
    }
}
