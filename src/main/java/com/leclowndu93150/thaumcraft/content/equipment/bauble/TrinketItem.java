package com.leclowndu93150.thaumcraft.content.equipment.bauble;

import com.leclowndu93150.thaumcraft.api.items.IVisDiscountGear;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class TrinketItem extends Item implements IVisDiscountGear {
    private final int visDiscount;

    public TrinketItem(Properties properties) {
        this(properties, 0);
    }

    public TrinketItem(Properties properties, int visDiscount) {
        super(properties);
        this.visDiscount = visDiscount;
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return visDiscount;
    }
}
