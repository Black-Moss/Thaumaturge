package com.leclowndu93150.thaumaturge.content.entity;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

final class PechHoard {
    private static final String HOARD_KEY = "hoard";
    private static final String LEGACY_HOARD_KEY = "Items";

    private PechHoard() {}

    static NonNullList<ItemStack> empty() {
        return NonNullList.withSize(EntityPech.LOOT_SLOTS, ItemStack.EMPTY);
    }

    static int filledSlots(List<ItemStack> loot) {
        int filled = 0;
        for (ItemStack stack : loot) {
            if (!stack.isEmpty()) {
                filled++;
            }
        }
        return filled;
    }

    static boolean canStore(List<ItemStack> loot, ItemStack stack) {
        for (ItemStack slot : loot) {
            if (slot.isEmpty() || ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() + stack.getCount() <= slot.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    static ItemStack store(List<ItemStack> loot, ItemStack stack) {
        ItemStack rest = stack.copy();
        for (ItemStack slot : loot) {
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, rest)) {
                int moved = Math.min(rest.getCount(), slot.getMaxStackSize() - slot.getCount());
                if (moved > 0) {
                    slot.grow(moved);
                    rest.shrink(moved);
                }
            }
        }
        if (rest.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (int i = 0; i < loot.size(); i++) {
            if (loot.get(i).isEmpty()) {
                loot.set(i, rest.copy());
                return ItemStack.EMPTY;
            }
        }
        return rest;
    }

    static void save(ValueOutput output, List<ItemStack> loot) {
        ValueOutput.TypedOutputList<ItemStackWithSlot> hoard = output.list(HOARD_KEY, ItemStackWithSlot.CODEC);
        for (int i = 0; i < loot.size(); i++) {
            if (!loot.get(i).isEmpty()) {
                hoard.add(new ItemStackWithSlot(i, loot.get(i)));
            }
        }
    }

    static NonNullList<ItemStack> load(ValueInput input) {
        NonNullList<ItemStack> loot = empty();
        fill(loot, input, HOARD_KEY);
        if (filledSlots(loot) == 0) {
            fill(loot, input, LEGACY_HOARD_KEY);
        }
        return loot;
    }

    private static void fill(List<ItemStack> loot, ValueInput input, String key) {
        for (ItemStackWithSlot entry : input.listOrEmpty(key, ItemStackWithSlot.CODEC)) {
            if (entry.isValidInContainer(EntityPech.LOOT_SLOTS)) {
                loot.set(entry.slot(), entry.stack());
            }
        }
    }
}
