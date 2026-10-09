package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class VoidRobeArmorItem extends Item implements IVisDiscountGear {
    private static final int ROBE_VIS_DISCOUNT = 5;
    private static final int ROBE_STACK_WARP = 3;

    public VoidRobeArmorItem(Properties properties) {
        super(warded(properties));
    }

    private static Properties warded(Properties properties) {
        return GearWarp.with(properties, ROBE_STACK_WARP);
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return ROBE_VIS_DISCOUNT;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        VoidGearItem.selfRepairTick(stack, entity);
    }
}
