package com.leclowndu93150.thaumcraft.content.equipment;

import com.leclowndu93150.thaumcraft.api.items.IVisDiscountGear;
import com.leclowndu93150.thaumcraft.api.items.IWarpingGear;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class CultistRobeItem extends Item implements IVisDiscountGear, IWarpingGear {
    private static final int VIS_DISCOUNT = 1;
    private static final int WARP = 1;

    public CultistRobeItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return VIS_DISCOUNT;
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return WARP;
    }
}
