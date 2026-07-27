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

    public SlotCrystalEssentia(Container container, int index, int x, int y, ResourceKey<IAspect> required) {
        super(container, index, x, y);
        this.required = required;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return isValidCrystal(stack, required);
    }

    public ResourceKey<IAspect> getRequired() {
        return required;
    }

    public static boolean isValidCrystal(ItemStack stack, ResourceKey<IAspect> required) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemEssentiaCrystal)) {
            return false;
        }
        Holder<IAspect> holder = ItemEssentiaCrystal.aspectOf(stack);
        return holder != null && holder.is(required);
    }
}