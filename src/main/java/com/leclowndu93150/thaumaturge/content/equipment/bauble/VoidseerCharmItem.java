package com.leclowndu93150.thaumaturge.content.equipment.bauble;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.registry.TTAttributes;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public final class VoidseerCharmItem extends Item implements IVisDiscountGear, IWarpingGear {
    public static final Identifier DISCOUNT_MODIFIER_ID = TTIds.rl("voidseer_discount");

    private static final String TEXT_KEY = "item.thaumaturge.voidseer_charm.text";
    private static final int WARP_CAP = 100;
    private static final float MAX_DISCOUNT_PERCENT = 25.0F;
    private static final double PERCENT_DIVISOR = 100.0;
    private static final float WARP_DIVISOR = 100.0F;
    private static final int WARP_PER_DISCOUNT_PERCENT = 5;

    public VoidseerCharmItem(Properties properties) {
        super(properties);
    }

    public static int discountFor(LivingEntity wearer) {
        if (!(wearer instanceof Player player)) {
            return 0;
        }
        int permanent = Math.min(WarpHelper.getWarp(player).get(WarpType.PERMANENT), WARP_CAP);
        return (int) (permanent / WARP_DIVISOR * MAX_DISCOUNT_PERCENT);
    }

    public void wornTick(ItemStack stack, LivingEntity wearer) {
        AttributeInstance instance = wearer.getAttribute(TTAttributes.VIS_DISCOUNT);
        if (wearer.level().isClientSide() || instance == null) {
            return;
        }
        double desired = discountFor(wearer) / PERCENT_DIVISOR;
        AttributeModifier existing = instance.getModifier(DISCOUNT_MODIFIER_ID);
        if ((existing == null ? 0.0 : existing.amount()) == desired) {
            return;
        }
        instance.removeModifier(DISCOUNT_MODIFIER_ID);
        if (desired != 0.0) {
            instance.addTransientModifier(new AttributeModifier(DISCOUNT_MODIFIER_ID, desired, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static void clearDiscount(LivingEntity wearer) {
        AttributeInstance instance = wearer.getAttribute(TTAttributes.VIS_DISCOUNT);
        if (instance != null) {
            instance.removeModifier(DISCOUNT_MODIFIER_ID);
        }
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return 0;
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return discountFor(wearer) / WARP_PER_DISCOUNT_PERCENT;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable(TEXT_KEY).withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
