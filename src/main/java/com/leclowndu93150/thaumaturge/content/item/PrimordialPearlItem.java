package com.leclowndu93150.thaumaturge.content.item;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jspecify.annotations.Nullable;

public final class PrimordialPearlItem extends Item {
    public static final int MAX_DAMAGE = 8;
    public static final int PEARL_MAX_DAMAGE = 2;
    public static final int NODULE_MAX_DAMAGE = 5;

    private static final String PEARL_SUFFIX = ".pearl";
    private static final String NODULE_SUFFIX = ".nodule";
    private static final String MOTE_SUFFIX = ".mote";

    public PrimordialPearlItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
        int next = instance.getOrDefault(DataComponents.DAMAGE, 0) + 1;
        if (next >= MAX_DAMAGE) {
            return null;
        }
        return new ItemStackTemplate(this, DataComponentPatch.builder().set(DataComponents.DAMAGE, next).build());
    }

    @Override
    public Component getName(ItemStack stack) {
        int damage = stack.getOrDefault(DataComponents.DAMAGE, 0);
        String suffix = damage <= PEARL_MAX_DAMAGE ? PEARL_SUFFIX : damage <= NODULE_MAX_DAMAGE ? NODULE_SUFFIX : MOTE_SUFFIX;
        return Component.translatable(getDescriptionId() + suffix);
    }
}
