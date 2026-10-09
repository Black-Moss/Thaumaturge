package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.TTIds;
import java.util.Arrays;
import java.util.List;
import java.util.function.ToIntFunction;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class FortressArmorEvents {
    private static final Identifier SET_ARMOR_ID = TTIds.rl("fortress_set_armor");
    private static final Identifier SET_TOUGHNESS_ID = TTIds.rl("fortress_set_toughness");
    private static final List<EquipmentSlot> SET_SLOTS = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS);
    private static final float MAGIC_ABSORPTION_DIVISOR = 35.0F;
    private static final float FIRE_ABSORPTION_DIVISOR = 20.0F;
    private static final int GHOST_MASK = 1;
    private static final int FIEND_MASK = 2;
    private static final float GHOST_DAMAGE_DIVISOR = 10.0F;
    private static final float FIEND_DAMAGE_DIVISOR = 12.0F;
    private static final int GHOST_WITHER_TICKS = 80;
    private static final int GHOST_WITHER_AMPLIFIER = 0;
    private static final float FIEND_HEAL = 1.0F;

    private FortressArmorEvents() {}

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!event.getSlot().isArmor() || !(event.getEntity() instanceof ServerPlayer wearer)) {
            return;
        }
        refreshSetBonus(wearer);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        LivingEntity target = event.getEntity();
        if (target instanceof Player wearer) {
            absorb(event, wearer, source);
            retaliate(event, wearer, source);
        }
        siphon(event, target, source);
    }

    private static void refreshSetBonus(ServerPlayer player) {
        boolean anyWorn = false;
        int maskedPieces = 0;
        for (EquipmentSlot slot : SET_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof FortressArmorItem) {
                anyWorn = true;
                maskedPieces += FortressArmorItem.mask(stack) == FortressArmorItem.NO_MASK ? 0 : 1;
            }
        }
        int toughness = anyWorn ? 1 : 0;
        applyBonus(player, Attributes.ARMOR_TOUGHNESS, SET_TOUGHNESS_ID, toughness);
        applyBonus(player, Attributes.ARMOR, SET_ARMOR_ID, toughness + maskedPieces);
    }

    private static void applyBonus(ServerPlayer player, Holder<Attribute> attribute, Identifier id, int bonus) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            replaceModifier(instance, id, bonus);
        }
    }

    private static void replaceModifier(AttributeInstance instance, Identifier id, int bonus) {
        instance.removeModifier(id);
        if (bonus > 0) {
            instance.addTransientModifier(new AttributeModifier(id, bonus, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void absorb(LivingIncomingDamageEvent event, Player player, DamageSource source) {
        boolean magic = source.is(DamageTypeTags.WITCH_RESISTANT_TO);
        boolean fire = source.is(DamageTypeTags.IS_FIRE);
        if (!magic && !fire) {
            return;
        }
        int fortress = wornDefense(player, FortressArmorItem.class, TTMaterials.ARMOR_FORTRESS);
        int robes = wornDefense(player, VoidRobeArmorItem.class, TTMaterials.ARMOR_VOID_ROBE);
        float ratio = absorptionRatio(magic, fortress, robes);
        if (ratio > 0.0F) {
            event.setAmount(event.getAmount() * Math.max(0.0F, 1.0F - ratio));
        }
    }

    private static float absorptionRatio(boolean magic, int fortress, int robes) {
        if (magic) {
            return (fortress + robes) / MAGIC_ABSORPTION_DIVISOR;
        }
        return fortress > 0 ? fortress / FIRE_ABSORPTION_DIVISOR : 0.0F;
    }

    private static int wornDefense(Player player, Class<? extends Item> kind, ArmorMaterial material) {
        ToIntFunction<ArmorType> contribution = type -> kind.isInstance(player.getItemBySlot(type.getSlot()).getItem()) ? material.defense().getOrDefault(type, 0) : 0;
        return Arrays.stream(ArmorType.values()).mapToInt(contribution).sum();
    }

    private static void retaliate(LivingIncomingDamageEvent event, Player player, DamageSource source) {
        if (helmetMask(player) != GHOST_MASK || !(attackerOf(source) instanceof LivingEntity attacker)) {
            return;
        }
        if (rolls(player.getRandom(), event.getAmount(), GHOST_DAMAGE_DIVISOR)) {
            attacker.addEffect(new MobEffectInstance(MobEffects.WITHER, GHOST_WITHER_TICKS, GHOST_WITHER_AMPLIFIER));
        }
    }

    private static void siphon(LivingIncomingDamageEvent event, LivingEntity victim, DamageSource source) {
        if (!(source.getEntity() instanceof Player attacker) || helmetMask(attacker) != FIEND_MASK) {
            return;
        }
        if (rolls(victim.getRandom(), event.getAmount(), FIEND_DAMAGE_DIVISOR)) {
            attacker.heal(FIEND_HEAL);
        }
    }

    private static Entity attackerOf(DamageSource source) {
        Entity direct = source.getEntity();
        return direct != null ? direct : source.getDirectEntity();
    }

    private static boolean rolls(RandomSource random, float amount, float divisor) {
        return random.nextFloat() < amount / divisor;
    }

    private static int helmetMask(Player player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        return isFortress(head) ? FortressArmorItem.mask(head) : FortressArmorItem.NO_MASK;
    }

    private static boolean isFortress(ItemStack stack) {
        return stack.getItem() instanceof FortressArmorItem;
    }
}
