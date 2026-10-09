package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.taint.item.ItemEssentiaCrystal;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class SlotCrystalEssentia extends Slot {
    private final ResourceKey<IAspect> required;

    public SlotCrystalEssentia(Container inventory, int index, int xPos, int yPos, ResourceKey<IAspect> aspectKey) {
        super(inventory, index, xPos, yPos);
        required = aspectKey;
    }

    public static boolean isValidCrystal(ItemStack stack, ResourceKey<IAspect> required) {
        Holder<IAspect> aspect = stack.getItem() instanceof ItemEssentiaCrystal ? ItemEssentiaCrystal.aspectOf(stack) : null;
        return aspect != null && aspect.is(required);
    }

    public ResourceKey<IAspect> getRequired() {
        return required;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return isValidCrystal(stack, required);
    }
}
