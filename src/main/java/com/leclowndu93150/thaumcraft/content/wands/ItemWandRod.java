package com.leclowndu93150.thaumcraft.content.wands;

import com.leclowndu93150.thaumcraft.api.wands.WandRod;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public final class ItemWandRod extends Item {
    private final Supplier<WandRod> rod;

    public ItemWandRod(Item.Properties properties, Supplier<WandRod> rod) {
        super(properties);
        this.rod = rod;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.thaumcraft.wand.capacity", rod.get().capacity())
                .withStyle(ChatFormatting.GOLD));
    }
}
