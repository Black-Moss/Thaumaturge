package com.leclowndu93150.thaumaturge.content.item;

import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public final class CelestialNotesItem extends Item {
    public CelestialNotesItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemStack stackOf(CelestialBody body) {
        ItemStack stack = new ItemStack(TCItems.CELESTIAL_NOTES.get());
        stack.set(TCDataComponents.CELESTIAL_BODY.get(), body);
        return stack;
    }

    public static CelestialBody bodyOf(ItemStack stack) {
        return stack.getOrDefault(TCDataComponents.CELESTIAL_BODY.get(), CelestialBody.SUN);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.thaumaturge.celestial_notes." + bodyOf(stack).getSerializedName() + ".text")
                .withStyle(ChatFormatting.AQUA));
    }
}
