package com.leclowndu93150.thaumaturge.content.warp;

import com.leclowndu93150.thaumaturge.api.warp.IPlayerWarp;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class WarpBindings implements WarpHelper.Bindings {
    @Override
    public IPlayerWarp getWarp(Player player) {
        return player.getData(TTAttachments.WARP);
    }

    @Override
    public void addWarp(ServerPlayer player, int amount, WarpType type) {
        WarpLedger.change(player, amount, type);
    }

    @Override
    public int getActualWarp(Player player) {
        return player.getData(TTAttachments.WARP).actual();
    }

    @Override
    public int getFinalWarp(ItemStack stack, LivingEntity wearer) {
        return WarpGear.stackWarp(stack, wearer);
    }
}
