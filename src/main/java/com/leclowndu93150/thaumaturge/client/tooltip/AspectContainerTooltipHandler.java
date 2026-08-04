package com.leclowndu93150.thaumaturge.client.tooltip;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaContainerItem;
import java.util.List;
import net.minecraft.client.Minecraft;
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
public final class AspectContainerTooltipHandler {
    private AspectContainerTooltipHandler() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        if (!(stack.getItem() instanceof IEssentiaContainerItem container)) {
            return;
        }

        if (!shouldShow()) {
            return;
        }
        AspectList aspects = container.getAspects(stack);
        if (aspects.isEmpty()) {
            return;
        }
        List<Component> tooltip = event.getToolTip();
        tooltip.add(
                1,
                Component.translatable("tooltip.thaumaturge.aspects.header")
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0xB59ED9))));
        for (AspectInstance entry : aspects.sortedByAmount().reversed()) {
            Holder<IAspect> aspect = entry.aspect();
            MutableComponent line = Component.literal("  ").append(AspectComponents.name(aspect));
            if (AspectKnowledgeAccess.isKnown(aspect)) {
                line.append(Component.literal(" x" + entry.amount()));
            }
            line.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(aspect.value().color())));
            tooltip.add(2, line);
        }
    }

    private static boolean shouldShow() {
        Minecraft mc = Minecraft.getInstance();
        return mc.screen != null;
    }
}
