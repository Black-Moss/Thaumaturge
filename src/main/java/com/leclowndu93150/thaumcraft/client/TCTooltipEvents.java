package com.leclowndu93150.thaumcraft.client;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.warp.WarpHelper;
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
        int warp = WarpHelper.getFinalWarp(event.getItemStack(), event.getEntity());
        if (warp > 0) {
            event.getToolTip().add(1, Component.translatable("item.thaumcraft.warping")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}
