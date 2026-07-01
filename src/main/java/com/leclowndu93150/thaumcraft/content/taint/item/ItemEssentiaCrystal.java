package com.leclowndu93150.thaumcraft.content.taint.item;

import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public final class ItemEssentiaCrystal extends Item {
    public ItemEssentiaCrystal(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        Holder<IAspect> aspect = aspectOf(stack);
        if (aspect == null) {
            return Component.translatable("item.thaumcraft.essentia_crystal.unknown");
        }
        Component aspectName = Component.translatable("aspect.thaumcraft." + aspect.value().tag());
        return Component.translatable("item.thaumcraft.essentia_crystal", aspectName);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        Holder<IAspect> aspect = aspectOf(stack);
        if (aspect != null) {
            int color = aspect.value().color();
            Component line = Component.translatable("aspect.thaumcraft." + aspect.value().tag() + ".desc")
                    .withStyle(style -> style.withColor(color));
            tooltip.accept(line);
        }
    }

    public static Holder<IAspect> aspectOf(ItemStack stack) {
        AspectInstance instance = stack.get(TCDataComponents.CRYSTAL_ASPECT.get());
        return instance == null ? null : instance.aspect();
    }

    public static int colorOf(ItemStack stack) {
        Holder<IAspect> aspect = aspectOf(stack);
        return aspect == null ? 0xFFFFFF : aspect.value().color();
    }
}
