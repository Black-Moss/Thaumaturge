package com.leclowndu93150.thaumcraft.content.item.equipment;

import com.leclowndu93150.thaumcraft.api.items.IGoggles;
import com.leclowndu93150.thaumcraft.api.items.IRevealer;
import com.leclowndu93150.thaumcraft.api.items.IVisDiscountGear;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class GogglesItem extends Item implements IGoggles, IRevealer, IVisDiscountGear {
    public GogglesItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean showIngamePopups(ItemStack stack, LivingEntity wearer) {
        return true;
    }

    @Override
    public boolean showNodes(ItemStack stack, LivingEntity holder) {
        return true;
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return 5;
    }

    @Override
    public EquipmentSlotGroup getAppliedSlot(ItemStack stack) {
        return EquipmentSlotGroup.HEAD;
    }
}
