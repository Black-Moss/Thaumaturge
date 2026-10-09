package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingStore;
import com.leclowndu93150.thaumaturge.content.taint.item.ItemEssentiaCrystal;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.RootCommitJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class WorkbenchCraftingStore implements IArcaneCraftingStore {
    private static final int GRID_WIDTH = 3;
    private static final int CRYSTAL_FIRST = InventoryArcaneWorkbench.CRAFTING_SLOTS;
    private static final int CRYSTAL_END = InventoryArcaneWorkbench.WAND_SLOT;

    private final InventoryArcaneWorkbench inventory;
    private final Player player;
    private final Window window;

    public WorkbenchCraftingStore(InventoryArcaneWorkbench inventory, Player player, int left, int top, int width, int height) {
        this.inventory = inventory;
        this.player = player;
        this.window = new Window(left, top, width, height);
    }

    private record Window(int left, int top, int width, int height) {
        int cells() {
            return width * height;
        }

        int slotOf(int cell) {
            return left + cell % width + (top + cell / width) * GRID_WIDTH;
        }

        int cellAt(int slot) {
            int x = slot % GRID_WIDTH - left;
            int y = slot / GRID_WIDTH - top;
            boolean inside = x >= 0 && x < width && y >= 0 && y < height;
            return inside ? x + y * width : -1;
        }
    }

    @Override
    public boolean consume(Consumption consumption, TransactionContext transaction) {
        if (!gridMatches(consumption.grid())) {
            return false;
        }
        List<ItemStack> spill = new ArrayList<>();
        spendGrid(consumption.remainders(), spill, transaction);
        if (!payCrystals(consumption, transaction)) {
            return false;
        }
        replaceWand(consumption.wand(), transaction);
        if (!spill.isEmpty()) {
            new RootCommitJournal(() -> giveBack(spill)).updateSnapshots(transaction);
        }
        return true;
    }

    private void spendGrid(List<ItemStack> remainders, List<ItemStack> spill, TransactionContext transaction) {
        for (int cell = 0; cell < window.cells(); cell++) {
            int slot = window.slotOf(cell);
            ItemStack leftover = cell < remainders.size() ? remainders.get(cell).copy() : ItemStack.EMPTY;
            ItemStack result = takeOneInto(inventory.getItem(slot), leftover, spill);
            inventory.setItem(slot, result, transaction);
        }
    }

    private void replaceWand(ItemStack target, TransactionContext transaction) {
        if (ItemStack.matches(inventory.wandStack(), target)) {
            return;
        }
        inventory.setItem(InventoryArcaneWorkbench.WAND_SLOT, target.copy(), transaction);
    }

    private void giveBack(List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            player.getInventory().placeItemBackInInventory(stack);
        }
    }

    private boolean gridMatches(List<ItemStack> grid) {
        if (grid.size() != window.cells()) {
            return false;
        }
        for (int slot = 0; slot < InventoryArcaneWorkbench.CRAFTING_SLOTS; slot++) {
            int cell = window.cellAt(slot);
            ItemStack wanted = cell < 0 ? ItemStack.EMPTY : grid.get(cell);
            if (!ItemStack.matches(inventory.getItem(slot), wanted)) {
                return false;
            }
        }
        return true;
    }

    private static ItemStack takeOneInto(ItemStack current, ItemStack leftover, List<ItemStack> spill) {
        ItemStack kept = shrunk(current, 1);
        if (kept.isEmpty()) {
            return leftover;
        }
        if (leftover.isEmpty()) {
            return kept;
        }
        if (!ItemStack.isSameItemSameComponents(kept, leftover)) {
            spill.add(leftover);
            return kept;
        }
        leftover.grow(kept.getCount());
        return leftover;
    }

    private boolean payCrystals(Consumption consumption, TransactionContext transaction) {
        for (AspectInstance entry : consumption.crystals().entries()) {
            if (drainAspect(entry, transaction) > 0) {
                return false;
            }
        }
        return true;
    }

    private int drainAspect(AspectInstance entry, TransactionContext transaction) {
        int owed = entry.amount();
        int slot = CRYSTAL_FIRST;
        while (owed > 0 && slot < CRYSTAL_END) {
            ItemStack stack = inventory.getItem(slot);
            if (carriesAspect(stack, entry)) {
                int taken = Math.min(owed, stack.getCount());
                inventory.setItem(slot, shrunk(stack, taken), transaction);
                owed -= taken;
            }
            slot++;
        }
        return owed;
    }

    private static boolean carriesAspect(ItemStack stack, AspectInstance entry) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemEssentiaCrystal)) {
            return false;
        }
        Holder<IAspect> held = ItemEssentiaCrystal.aspectOf(stack);
        return held != null && held.value().tag().equals(entry.aspect().value().tag());
    }

    private static ItemStack shrunk(ItemStack stack, int amount) {
        int remaining = stack.getCount() - amount;
        return remaining > 0 ? stack.copyWithCount(remaining) : ItemStack.EMPTY;
    }
}
