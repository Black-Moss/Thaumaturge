package com.leclowndu93150.thaumcraft.compat.jei.category;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.AspectComponents;
import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.config.ThaumcraftClientConfig;
import com.leclowndu93150.thaumcraft.content.aspect.AspectIndexHolder;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class ItemAspectTooltipHandler {
    private ItemAspectTooltipHandler() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        if (!shouldShow()) {
            return;
        }
        AspectList aspects = AspectIndexHolder.get().of(stack);
        if (aspects.isEmpty()) {
            return;
        }
        List<Component> tooltip = event.getToolTip();
        tooltip.add(Component.translatable("tooltip.thaumcraft.aspects.header")
                .withStyle(style -> style.withColor(TextColor.fromRgb(0xB59ED9))));
        for (AspectInstance entry : aspects.sortedByAmount()) {
            Holder<IAspect> aspect = entry.aspect();
            MutableComponent line = Component.literal("  ")
                    .append(AspectComponents.name(aspect))
                    .append(Component.literal(" x" + entry.amount()));
            line.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(aspect.value().color())));
            tooltip.add(line);
        }
    }

    private static boolean shouldShow() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) {
            return false;
        }
        if (mc.screen instanceof AbstractContainerScreen<?>) {
            return false;
        }
        boolean shift = mc.hasShiftDown();
        return shift != ThaumcraftClientConfig.showAspectsByDefault();
    }
}
