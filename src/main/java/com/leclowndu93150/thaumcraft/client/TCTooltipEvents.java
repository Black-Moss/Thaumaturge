package com.leclowndu93150.thaumcraft.client;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.items.IRechargable;
import com.leclowndu93150.thaumcraft.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumcraft.api.items.RechargeAccess;
import com.leclowndu93150.thaumcraft.api.warp.WarpHelper;
import com.leclowndu93150.thaumcraft.content.equipment.InfusionEnchantmentHelper;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCTooltipEvents {
    private TCTooltipEvents() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (event.getEntity() == null) {
            return;
        }
        for (Map.Entry<InfusionEnchantment, Integer> entry : InfusionEnchantmentHelper.get(event.getItemStack()).levels().entrySet()) {
            InfusionEnchantment enchantment = entry.getKey();
            Component line = Component.translatable("enchantment.thaumcraft." + enchantment.getSerializedName());
            if (enchantment.maxLevel() > 1) {
                line = Component.translatable("enchantment.thaumcraft." + enchantment.getSerializedName())
                        .append(" ").append(Component.translatable("enchantment.level." + entry.getValue()));
            }
            event.getToolTip().add(1, line.copy().withStyle(ChatFormatting.GOLD));
        }
        int warp = WarpHelper.getFinalWarp(event.getItemStack(), event.getEntity());
        if (warp > 0) {
            event.getToolTip().add(1, Component.translatable("item.thaumcraft.warping")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
        if (event.getItemStack().getItem() instanceof IRechargable rechargable) {
            event.getToolTip().add(1, Component.translatable("tooltip.thaumcraft.charge",
                            RechargeAccess.getCharge(event.getItemStack()),
                            rechargable.getMaxCharge(event.getItemStack(), event.getEntity()))
                    .withStyle(ChatFormatting.AQUA));
        }
    }
}
