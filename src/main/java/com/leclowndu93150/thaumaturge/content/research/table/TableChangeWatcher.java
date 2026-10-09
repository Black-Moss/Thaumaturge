package com.leclowndu93150.thaumaturge.content.research.table;

import java.util.Arrays;
import net.minecraft.world.item.ItemStack;

final class TableChangeWatcher {
    private final TableBinding binding;
    private final int[] watchedSlots;
    private final ItemStack[] snapshots;

    TableChangeWatcher(TableBinding binding, int... watchedSlots) {
        this.binding = binding;
        this.watchedSlots = watchedSlots.clone();
        this.snapshots = new ItemStack[watchedSlots.length];
        Arrays.fill(snapshots, ItemStack.EMPTY);
    }

    boolean absorbDifferences() {
        boolean differs = false;
        for (int i = 0; i < watchedSlots.length; i++) {
            if (!ItemStack.matches(binding.stackAt(watchedSlots[i]), snapshots[i])) {
                differs = true;
                break;
            }
        }
        if (!differs) {
            return false;
        }
        for (int i = 0; i < watchedSlots.length; i++) {
            snapshots[i] = binding.stackAt(watchedSlots[i]).copy();
        }
        return true;
    }
}
