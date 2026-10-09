package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public final class FortressArmorItem extends Item {
    public static final int NO_MASK = -1;

    private static final List<String> MASK_KEYS = List.of("item.thaumaturge.fortress_helm.mask.0", "item.thaumaturge.fortress_helm.mask.1", "item.thaumaturge.fortress_helm.mask.2");

    public FortressArmorItem(Properties properties) {
        super(properties);
    }

    public static boolean hasGoggles(ItemStack stack) {
        return stack.has(TTDataComponents.GOGGLES_UPGRADE.get());
    }

    public static int mask(ItemStack stack) {
        return stack.getOrDefault(TTDataComponents.FORTRESS_MASK.get(), NO_MASK);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        if (hasGoggles(stack)) {
            builder.accept(Component.translatable("item.thaumaturge.goggles_revealing").withStyle(ChatFormatting.DARK_PURPLE));
        }
        int mask = mask(stack);
        if (mask >= 0 && mask < MASK_KEYS.size()) {
            builder.accept(Component.translatable(MASK_KEYS.get(mask)).withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(stack, context, display, builder, flag);
    }
}
