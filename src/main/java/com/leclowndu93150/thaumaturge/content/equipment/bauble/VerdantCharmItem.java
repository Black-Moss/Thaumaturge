package com.leclowndu93150.thaumaturge.content.equipment.bauble;

import com.leclowndu93150.thaumaturge.api.items.ChargeDisplay;
import com.leclowndu93150.thaumaturge.api.items.ChargeProfile;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public final class VerdantCharmItem extends Item {
    public static final int TYPE_BASE = 0;
    public static final int TYPE_LIFE = 1;
    public static final int TYPE_SUSTAIN = 2;

    private static final int CHARGE_CAPACITY = 200;
    private static final int PULSE_INTERVAL_TICKS = 20;
    private static final int LOW_AIR_TICKS = 100;
    private static final int RESTORED_AIR_TICKS = 300;
    private static final float HEAL_AMOUNT = 1.0F;
    private static final int FOOD_POINTS = 1;
    private static final float FOOD_SATURATION_MODIFIER = 0.3F;
    private static final int WITHER_COST = 20;
    private static final int POISON_COST = 10;
    private static final int TAINT_COST = 5;
    private static final int BREATHE_COST = 1;
    private static final int HEAL_COST = 5;
    private static final int FEED_COST = 1;
    private static final int WITHER_PRIORITY = 5;
    private static final int POISON_PRIORITY = 4;
    private static final int TAINT_PRIORITY = 3;
    private static final int BREATHE_PRIORITY = 2;
    private static final int HEAL_PRIORITY = 1;
    private static final int FEED_PRIORITY = 0;
    private static final String LIFE_KEY = "item.thaumaturge.verdant_charm.life.text";
    private static final String SUSTAIN_KEY = "item.thaumaturge.verdant_charm.sustain.text";
    private static final List<Remedy> REMEDIES = buildRemedies();

    public VerdantCharmItem(Properties properties) {
        super(properties.component(TTDataComponents.RECHARGEABLE.get(), new ChargeProfile(CHARGE_CAPACITY, ChargeDisplay.ALWAYS)));
    }

    public static int type(ItemStack stack) {
        return stack.getOrDefault(TTDataComponents.VERDANT_TYPE.get(), TYPE_BASE);
    }

    public void wornTick(ItemStack stack, LivingEntity wearer) {
        if (!(wearer instanceof ServerPlayer player) || player.tickCount % PULSE_INTERVAL_TICKS != 0) {
            return;
        }
        Remedy chosen = pick(player, type(stack), RechargeAccess.getCharge(stack));
        if (chosen != null && RechargeAccess.consumeCharge(stack, player, chosen.cost())) {
            chosen.outcome().apply(player);
        }
    }

    private static Remedy pick(ServerPlayer player, int type, int charge) {
        for (Remedy remedy : REMEDIES) {
            if (charge >= remedy.cost() && remedy.condition().holds(player, type)) {
                return remedy;
            }
        }
        return null;
    }

    private static List<Remedy> buildRemedies() {
        List<Remedy> all = new ArrayList<>();
        all.add(cure(MobEffects.WITHER, WITHER_COST, WITHER_PRIORITY));
        all.add(cure(MobEffects.POISON, POISON_COST, POISON_PRIORITY));
        all.add(cure(TTMobEffects.FLUX_TAINT, TAINT_COST, TAINT_PRIORITY));
        all.add(new Remedy(BREATHE_PRIORITY, BREATHE_COST, VerdantCharmItem::needsAir, VerdantCharmItem::refillAir));
        all.add(new Remedy(HEAL_PRIORITY, HEAL_COST, VerdantCharmItem::isHurt, VerdantCharmItem::mend));
        all.add(new Remedy(FEED_PRIORITY, FEED_COST, VerdantCharmItem::isHungry, VerdantCharmItem::nourish));
        all.sort(Comparator.comparingInt(Remedy::priority).reversed());
        return List.copyOf(all);
    }

    private static boolean needsAir(ServerPlayer player, int type) {
        return type == TYPE_SUSTAIN && player.getAirSupply() < LOW_AIR_TICKS;
    }

    private static boolean isHurt(ServerPlayer player, int type) {
        return type == TYPE_LIFE && player.getHealth() < player.getMaxHealth();
    }

    private static boolean isHungry(ServerPlayer player, int type) {
        return type == TYPE_SUSTAIN && player.canEat(false);
    }

    private static void refillAir(ServerPlayer player) {
        player.setAirSupply(RESTORED_AIR_TICKS);
    }

    private static void mend(ServerPlayer player) {
        player.heal(HEAL_AMOUNT);
    }

    private static void nourish(ServerPlayer player) {
        player.getFoodData().eat(FOOD_POINTS, FOOD_SATURATION_MODIFIER);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        String key = switch (type(stack)) {
            case TYPE_LIFE -> LIFE_KEY;
            case TYPE_SUSTAIN -> SUSTAIN_KEY;
            default -> null;
        };
        if (key != null) {
            tooltip.accept(Component.translatable(key).withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    private static Remedy cure(Holder<MobEffect> effect, int cost, int priority) {
        return new Remedy(priority, cost, (player, type) -> player.hasEffect(effect), player -> player.removeEffect(effect));
    }

    private interface Condition {
        boolean holds(ServerPlayer player, int type);
    }

    private interface Outcome {
        void apply(ServerPlayer player);
    }

    private record Remedy(int priority, int cost, Condition condition, Outcome outcome) {
    }
}
