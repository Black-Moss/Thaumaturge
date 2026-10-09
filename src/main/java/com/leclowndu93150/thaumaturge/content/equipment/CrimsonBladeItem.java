package com.leclowndu93150.thaumaturge.content.equipment;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;

public class CrimsonBladeItem extends Item {
    private static final int BLADE_WARP = 2;
    private static final int REPAIR_INTERVAL_TICKS = 20;
    private static final int WEAKNESS_TICKS = 60;
    private static final int HUNGER_TICKS = 120;
    private static final int EFFECT_AMPLIFIER = 0;

    public CrimsonBladeItem(Properties properties) {
        super(GearWarp.with(properties, BLADE_WARP));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!dueForRepair(stack, entity)) {
            return;
        }
        stack.setDamageValue(stack.getDamageValue() - 1);
    }

    private static boolean dueForRepair(ItemStack stack, Entity owner) {
        if (!(owner instanceof LivingEntity)) {
            return false;
        }
        return owner.tickCount % REPAIR_INTERVAL_TICKS == 0 && stack.isDamaged();
    }

    private static void sap(LivingEntity target, Holder<MobEffect> effect, int duration) {
        target.addEffect(new MobEffectInstance(effect, duration, EFFECT_AMPLIFIER));
    }

    private static boolean pvpBlocked(LivingEntity attacker, LivingEntity target) {
        return attacker instanceof Player player && target instanceof Player victim && !player.canHarmPlayer(victim);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (target.level().isClientSide()) {
            return;
        }
        if (pvpBlocked(attacker, target)) {
            return;
        }
        sap(target, MobEffects.WEAKNESS, WEAKNESS_TICKS);
        sap(target, MobEffects.HUNGER, HUNGER_TICKS);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.thaumaturge.crimson_blade.greater_sapping").withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, context, display, builder, flag);
    }
}
