package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class SlotWorkbenchWand extends Slot {

    public SlotWorkbenchWand(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    public static boolean isUsableWand(ItemStack stack) {
        if (!(stack.getItem() instanceof ICaster)) {
            return false;
        }
        if (stack.getItem() instanceof ItemWand wand && wand.isStaff(stack)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return isUsableWand(stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
