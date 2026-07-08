package com.leclowndu93150.thaumcraft.content.golem;

import com.leclowndu93150.thaumcraft.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumcraft.content.golem.seals.MenuSealBase;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;

public final class SealGuiOpener {
    private SealGuiOpener() {}

    public static void open(Player player, ISealEntity seal) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        serverPlayer.openMenu(new SimpleMenuProvider(
                (containerId, inventory, menuPlayer) -> new MenuSealBase(containerId, inventory, seal),
                Component.translatable("gui.thaumcraft.seal")), buf -> {
            buf.writeBlockPos(seal.getSealPos().pos());
            buf.writeEnum(seal.getSealPos().face());
        });
    }
}
