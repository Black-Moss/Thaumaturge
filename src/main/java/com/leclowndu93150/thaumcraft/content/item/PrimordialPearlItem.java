package com.leclowndu93150.thaumcraft.content.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class PrimordialPearlItem extends Item {
    public static final int MAX_DAMAGE = 8;

    public static final int PEARL_MAX_DAMAGE = 2;
    public static final int NODULE_MAX_DAMAGE = 5;

    public PrimordialPearlItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        int damage = stack.getDamageValue();
        String suffix;
        if (damage < 3) {
            suffix = ".pearl";
        } else if (damage < 6) {
            suffix = ".nodule";
        } else {
            suffix = ".mote";
        }
        return Component.translatable(this.getDescriptionId() + suffix);
    }
}
