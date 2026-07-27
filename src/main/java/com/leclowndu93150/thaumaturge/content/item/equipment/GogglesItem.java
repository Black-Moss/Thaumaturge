package com.leclowndu93150.thaumaturge.content.item.equipment;

import com.leclowndu93150.thaumaturge.api.items.IGoggles;
import com.leclowndu93150.thaumaturge.api.items.IRevealer;
import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
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

}
