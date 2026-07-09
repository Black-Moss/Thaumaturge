package com.leclowndu93150.thaumcraft.content.warp;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.items.IWarpingGear;
import com.leclowndu93150.thaumcraft.api.warp.IPlayerWarp;
import com.leclowndu93150.thaumcraft.api.warp.ItemWarp;
import com.leclowndu93150.thaumcraft.api.warp.WarpHelper;
import com.leclowndu93150.thaumcraft.api.warp.WarpType;
import com.leclowndu93150.thaumcraft.compat.curio.ThaumcraftCuriosCompat;
import com.leclowndu93150.thaumcraft.registry.TCAttachments;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import com.leclowndu93150.thaumcraft.registry.TCDataMaps;
import com.leclowndu93150.thaumcraft.registry.TCSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public final class WarpManager {
    private WarpManager() {}

    public static WarpData data(Player player) {
        return player.getData(TCAttachments.WARP);
    }

    public static void addWarp(ServerPlayer player, int amount, WarpType type) {
        WarpData data = data(player);
        if (amount < 0) {
            amount = Math.max(amount, -data.get(type));
        }
        if (amount == 0) {
            return;
        }
        data.add(type, amount);
        if (amount > 0) {
            data.setCounter(data.total());
            notifyGain(player, type);
        } else {
            notifyLoss(player, type);
        }
        player.syncData(TCAttachments.WARP);
    }

    public static int getActualWarp(Player player) {
        IPlayerWarp warp = data(player);
        return warp.get(WarpType.PERMANENT) + warp.get(WarpType.NORMAL);
    }

    public static int getFinalWarp(ItemStack stack, LivingEntity wearer) {
        if (stack.isEmpty()) {
            return 0;
        }
        int warp = 0;
        if (stack.getItem() instanceof IWarpingGear gear) {
            warp += gear.getWarp(stack, wearer);
        }
        ItemWarp mapped = stack.getItem().builtInRegistryHolder().getData(TCDataMaps.ITEM_WARP);
        if (mapped != null) {
            warp += mapped.amount();
        }
        warp += stack.getOrDefault(TCDataComponents.STACK_WARP.get(), 0);
        return warp;
    }

    public static int getWarpFromGear(ServerPlayer player) {
        int warp = getFinalWarp(player.getMainHandItem(), player);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.isArmor()) {
                warp += getFinalWarp(player.getItemBySlot(slot), player);
            }
        }
        if (ModList.get().isLoaded(TCIds.CURIOS)) {
            for (ItemStack stack : ThaumcraftCuriosCompat.equippedCurios(player)) {
                warp += getFinalWarp(stack, player);
            }
        }
        return warp;
    }

    private static void notifyGain(ServerPlayer player, WarpType type) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                TCSounds.WHISPERS.get(), SoundSource.AMBIENT, 0.5F, 1.0F);
        String key = switch (type) {
            case PERMANENT -> "warp.thaumcraft.gain.permanent";
            case NORMAL -> "warp.thaumcraft.gain.normal";
            case TEMPORARY -> "warp.thaumcraft.gain.temporary";
        };
        sendActionBar(player, key);
    }

    private static void notifyLoss(ServerPlayer player, WarpType type) {
        String key = switch (type) {
            case PERMANENT -> "warp.thaumcraft.lose.permanent";
            case NORMAL -> "warp.thaumcraft.lose.normal";
            case TEMPORARY -> "warp.thaumcraft.lose.temporary";
        };
        sendActionBar(player, key);
    }

    public static void sendActionBar(ServerPlayer player, String key) {
        player.connection.send(new ClientboundSetActionBarTextPacket(
                Component.translatable(key).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)));
    }

    public static final class Bindings implements WarpHelper.Bindings {
        @Override
        public IPlayerWarp getWarp(Player player) {
            return data(player);
        }

        @Override
        public void addWarp(ServerPlayer player, int amount, WarpType type) {
            WarpManager.addWarp(player, amount, type);
        }

        @Override
        public int getActualWarp(Player player) {
            return WarpManager.getActualWarp(player);
        }

        @Override
        public int getFinalWarp(ItemStack stack, LivingEntity wearer) {
            return WarpManager.getFinalWarp(stack, wearer);
        }
    }
}
