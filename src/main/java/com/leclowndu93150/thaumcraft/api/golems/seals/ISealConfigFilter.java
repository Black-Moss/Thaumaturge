package com.leclowndu93150.thaumcraft.api.golems.seals;

import java.util.List;
import net.minecraft.world.item.ItemStack;

/**
 * Configuration surface for seals with an item filter. Filter slots hold ghost stacks; the
 * optional per-slot size limits how many items a matching operation may move.
 *
 * @since 1.0.0
 */
public interface ISealConfigFilter {
    /**
     * @return the filter's ghost stacks
     */
    List<ItemStack> getInv();

    /**
     * @return the per-slot size limits, parallel to {@link #getInv()}
     */
    List<Integer> getSizes();

    /**
     * @return the number of filter slots
     */
    int getFilterSize();

    ItemStack getFilterSlot(int slot);

    int getFilterSlotSize(int slot);

    void setFilterSlot(int slot, ItemStack stack);

    void setFilterSlotSize(int slot, int size);

    /**
     * @return whether the filter excludes matching items instead of requiring them
     */
    boolean isBlacklist();

    void setBlacklist(boolean blacklist);

    /**
     * @return whether the filter UI exposes per-slot stack size limiters
     */
    boolean hasStacksizeLimiters();
}
