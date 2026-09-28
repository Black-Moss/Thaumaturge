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

    private final InventoryArcaneWorkbench inventory;
    private final Player player;
    private final int left;
    private final int top;
    private final int width;
    private final int height;

    public WorkbenchCraftingStore(InventoryArcaneWorkbench inventory, Player player, int left, int top, int width, int height) {
        this.inventory = inventory;
        this.player = player;
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean consume(Consumption consumption, TransactionContext transaction) {
        if (!gridMatches(consumption.grid())) {
            return false;
        }
        List<ItemStack> overflow = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int compact = x + y * width;
                int slot = x + left + (y + top) * GRID_WIDTH;
                ItemStack remainder = compact < consumption.remainders().size() ? consumption.remainders().get(compact).copy() : ItemStack.EMPTY;
                inventory.setItem(slot, consumeOne(inventory.getItem(slot), remainder, overflow), transaction);
            }
        }
        if (!consumeCrystals(consumption, transaction)) {
            return false;
        }
        if (!ItemStack.matches(inventory.wandStack(), consumption.wand())) {
            inventory.setItem(InventoryArcaneWorkbench.WAND_SLOT, consumption.wand().copy(), transaction);
        }
        if (!overflow.isEmpty()) {
            new RootCommitJournal(() -> overflow.forEach(stack -> player.getInventory().placeItemBackInInventory(stack))).updateSnapshots(transaction);
        }
        return true;
    }

    private boolean gridMatches(List<ItemStack> grid) {
        if (grid.size() != width * height) {
            return false;
        }
        for (int slot = 0; slot < InventoryArcaneWorkbench.CRAFTING_SLOTS; slot++) {
            int x = slot % GRID_WIDTH - left;
            int y = slot / GRID_WIDTH - top;
            boolean inside = x >= 0 && x < width && y >= 0 && y < height;
            ItemStack expected = inside ? grid.get(x + y * width) : ItemStack.EMPTY;
            if (!ItemStack.matches(inventory.getItem(slot), expected)) {
                return false;
            }
        }
        return true;
    }

    private static ItemStack consumeOne(ItemStack current, ItemStack remainder, List<ItemStack> overflow) {
        ItemStack left = shrunk(current, 1);
        if (remainder.isEmpty()) {
            return left;
        }
        if (left.isEmpty()) {
            return remainder;
        }
        if (ItemStack.isSameItemSameComponents(left, remainder)) {
            remainder.grow(left.getCount());
            return remainder;
        }
        overflow.add(remainder);
        return left;
    }

    private boolean consumeCrystals(Consumption consumption, TransactionContext transaction) {
        for (AspectInstance entry : consumption.crystals().entries()) {
            int needed = entry.amount();
            for (int slot = InventoryArcaneWorkbench.CRAFTING_SLOTS; slot < InventoryArcaneWorkbench.WAND_SLOT && needed > 0; slot++) {
                ItemStack crystal = inventory.getItem(slot);
                if (crystal.isEmpty() || !(crystal.getItem() instanceof ItemEssentiaCrystal)) {
                    continue;
                }
                Holder<IAspect> aspect = ItemEssentiaCrystal.aspectOf(crystal);
                if (aspect != null && aspect.value().tag().equals(entry.aspect().value().tag())) {
                    int removed = Math.min(needed, crystal.getCount());
                    inventory.setItem(slot, shrunk(crystal, removed), transaction);
                    needed -= removed;
                }
            }
            if (needed > 0) {
                return false;
            }
        }
        return true;
    }

    private static ItemStack shrunk(ItemStack stack, int amount) {
        return stack.getCount() <= amount ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - amount);
    }
}
