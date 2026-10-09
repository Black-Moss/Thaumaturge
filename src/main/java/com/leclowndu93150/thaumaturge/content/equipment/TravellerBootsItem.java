package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.ChargeDisplay;
import com.leclowndu93150.thaumaturge.api.items.ChargeProfile;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class TravellerBootsItem extends Item {
    private static final int CHARGE_CAPACITY = 240;
    private static final int ENERGY_SECONDS_PER_CHARGE = 60;
    private static final int TICKS_PER_SECOND = 20;
    private static final int CHARGE_PER_REFILL = 1;
    private static final double STEP_BONUS = 0.4;
    private static final double JUMP_BONUS = 0.275;
    private static final float GROUND_ACCELERATION = 0.05F;
    private static final float WATER_ACCELERATION_DIVISOR = 4.0F;
    private static final float WATER_AIR_ACCELERATION = 0.025F;
    private static final Identifier STEP_MODIFIER_ID = TTIds.rl("traveller_step");
    private static final Identifier JUMP_MODIFIER_ID = TTIds.rl("traveller_jump");
    private static final Vec3 FORWARD = new Vec3(0.0, 0.0, 1.0);

    public TravellerBootsItem(Properties properties) {
        super(properties.component(TTDataComponents.RECHARGEABLE.get(), new ChargeProfile(CHARGE_CAPACITY, ChargeDisplay.ON_CHANGE)));
    }

    @Override
    public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (slot != EquipmentSlot.FEET || !(entity instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % TICKS_PER_SECOND == 0) {
            upkeepEnergy(stack, player);
        }
        boolean active = RechargeAccess.getCharge(stack) > 0 && !player.getAbilities().flying && !player.isShiftKeyDown();
        if (active) {
            addModifier(player, Attributes.STEP_HEIGHT, STEP_MODIFIER_ID, STEP_BONUS);
            addModifier(player, Attributes.JUMP_STRENGTH, JUMP_MODIFIER_ID, JUMP_BONUS);
        } else {
            clearMovementBoosts(player);
        }
    }

    static void clearMovementBoosts(ServerPlayer player) {
        removeModifier(player, Attributes.STEP_HEIGHT, STEP_MODIFIER_ID);
        removeModifier(player, Attributes.JUMP_STRENGTH, JUMP_MODIFIER_ID);
    }

    public static void clientMovementTick(Player player, ItemStack boots) {
        if (RechargeAccess.getCharge(boots) <= 0 || player.getAbilities().flying || player.zza <= 0.0F) {
            return;
        }
        boolean inWater = player.isInWater();
        if (player.onGround()) {
            player.moveRelative(inWater ? GROUND_ACCELERATION / WATER_ACCELERATION_DIVISOR : GROUND_ACCELERATION, FORWARD);
        } else if (inWater) {
            player.moveRelative(WATER_AIR_ACCELERATION, FORWARD);
        }
    }

    private static void upkeepEnergy(ItemStack stack, ServerPlayer player) {
        int energy = stack.getOrDefault(TTDataComponents.ENERGY.get(), 0);
        if (energy > 0) {
            stack.set(TTDataComponents.ENERGY.get(), energy - 1);
        } else if (RechargeAccess.consumeCharge(stack, player, CHARGE_PER_REFILL)) {
            stack.set(TTDataComponents.ENERGY.get(), ENERGY_SECONDS_PER_CHARGE);
        }
    }

    private static void addModifier(ServerPlayer player, Holder<Attribute> attribute, Identifier id, double amount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null && !instance.hasModifier(id)) {
            instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void removeModifier(ServerPlayer player, Holder<Attribute> attribute, Identifier id) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null && instance.hasModifier(id)) {
            instance.removeModifier(id);
        }
    }
}
