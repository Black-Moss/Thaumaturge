package com.leclowndu93150.thaumcraft.content.equipment;

import com.leclowndu93150.thaumcraft.api.items.IGoggles;
import com.leclowndu93150.thaumcraft.api.items.IRevealer;
import com.leclowndu93150.thaumcraft.api.items.IVisDiscountGear;
import com.leclowndu93150.thaumcraft.api.items.IWarpingGear;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.jspecify.annotations.Nullable;

public final class VoidRobeArmorItem extends Item
        implements IVisDiscountGear, IWarpingGear, IGoggles, IRevealer {
    private static final int VIS_DISCOUNT = 5;
    private static final int WARP = 3;

    public VoidRobeArmorItem(Properties properties) {
        super(properties);
    }

    private static boolean isHelm(ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot() == EquipmentSlot.HEAD;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        VoidGearItem.selfRepairTick(stack, entity);
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return VIS_DISCOUNT;
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return WARP;
    }

    @Override
    public boolean showIngamePopups(ItemStack stack, LivingEntity wearer) {
        return isHelm(stack);
    }

    @Override
    public boolean showNodes(ItemStack stack, LivingEntity wearer) {
        return isHelm(stack);
    }
}
