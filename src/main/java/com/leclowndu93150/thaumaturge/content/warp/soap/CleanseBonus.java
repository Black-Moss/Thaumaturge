package com.leclowndu93150.thaumaturge.content.warp.soap;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface CleanseBonus {
    int bonus(ServerPlayer player);
}
