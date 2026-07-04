package com.leclowndu93150.thaumcraft.content.equipment;

import com.leclowndu93150.thaumcraft.api.items.IVisDiscountGear;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class RobeArmorItem extends Item implements IVisDiscountGear {
    private final int visDiscount;

    public RobeArmorItem(int visDiscount, Properties properties) {
        super(properties);
        this.visDiscount = visDiscount;
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return visDiscount;
    }
}
