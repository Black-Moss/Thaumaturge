package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class VoidGearItem extends Item implements IWarpingGear {
    private static final int REPAIR_INTERVAL_TICKS = 20;
    static final int VOID_GEAR_WARP = 1;

    public VoidGearItem(Properties properties) {
        super(properties);
    }

    static void selfRepairTick(ItemStack stack, Entity entity) {
        if (entity instanceof LivingEntity && stack.isDamaged() && entity.tickCount % REPAIR_INTERVAL_TICKS == 0) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        selfRepairTick(stack, entity);
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return VOID_GEAR_WARP;
    }
}
