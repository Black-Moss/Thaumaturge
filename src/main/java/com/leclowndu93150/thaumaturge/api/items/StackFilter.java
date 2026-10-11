package com.leclowndu93150.thaumaturge.api.items;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

/**
 * A whitelist or blacklist of ghost stacks, with optional per-entry size limits, judged under an {@link ItemMatch}.
 *
 * <p>Empty ghost entries are ignored. Under a whitelist a candidate passes when some entry matches it; under a blacklist it passes
 * when no entry does. An empty candidate never passes. Each entry is the reference side of {@link ItemMatch#test}. The lists are read,
 * not copied, so callers must not change them while the filter is in use. No member changes any stack or inventory, and every member is
 * safe on both logical sides.
 *
 * @param entries   the ghost stacks
 * @param limits    the size limits, aligned by position with {@code entries}; may be shorter, missing positions meaning no limit
 * @param blacklist true to forbid matching stacks, false to require a match
 * @param match     the rules between an entry and a candidate
 * @since 1.1.0
 */
public record StackFilter(List<ItemStack> entries, List<Integer> limits, boolean blacklist, ItemMatch match) {
    private static final int NO_ENTRY = -1;
    private static final int UNLIMITED = 0;
    private static final int LEAVE_ONE_MINIMUM = 2;

    /**
     * Creates a filter without size limits.
     *
     * @param entries   the ghost stacks
     * @param blacklist true to forbid matching stacks, false to require a match
     * @param match     the rules between an entry and a candidate
     * @return the filter
     */
    public static StackFilter of(List<ItemStack> entries, boolean blacklist, ItemMatch match) {
        return new StackFilter(entries, List.of(), blacklist, match);
    }

    /**
     * @param candidate the stack to judge
     * @return whether the candidate passes; false for an empty candidate
     */
    public boolean accepts(ItemStack candidate) {
        return !candidate.isEmpty() && (matchingEntry(candidate) == NO_ENTRY) == blacklist;
    }

    /**
     * @param candidates the stacks to search, in order
     * @return the first passing candidate itself, not a copy, or the empty stack
     */
    public ItemStack firstAccepted(List<ItemStack> candidates) {
        for (ItemStack candidate : candidates) {
            if (accepts(candidate)) {
                return candidate;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Finds the first passing candidate together with the size limit that applies to it.
     *
     * @param candidates the stacks to search, in order
     * @return the first passing candidate itself, not a copy, with the matching entry's limit under a whitelist; the limit is zero,
     *         meaning unlimited, under a blacklist, when the entry has no limit or when nothing passes
     */
    public InvHelper.FilterMatch firstAcceptedWithLimit(List<ItemStack> candidates) {
        for (ItemStack candidate : candidates) {
            if (candidate.isEmpty()) {
                continue;
            }
            int entry = matchingEntry(candidate);
            if ((entry == NO_ENTRY) == blacklist) {
                return new InvHelper.FilterMatch(candidate, blacklist ? UNLIMITED : limitAt(entry));
            }
        }
        return new InvHelper.FilterMatch(ItemStack.EMPTY, UNLIMITED);
    }

    /**
     * Searches an inventory, slot by slot, for the first content that passes.
     *
     * @param handler  the inventory to search, or null for an absent inventory
     * @param leaveOne whether a content passes only when the inventory holds at least two items matching it under {@link #match}, so
     *                 that one can be left behind
     * @return a copy of the slot content with its count capped at the item's maximum stack size, or the empty stack
     */
    public ItemStack firstAccepted(@Nullable ResourceHandler<ItemResource> handler, boolean leaveOne) {
        if (handler == null) {
            return ItemStack.EMPTY;
        }
        for (int slot = 0; slot < handler.size(); slot++) {
            ItemResource resource = handler.getResource(slot);
            if (resource.isEmpty()) {
                continue;
            }
            ItemStack content = resource.toStack(handler.getAmountAsInt(slot));
            if (accepts(content) && (!leaveOne || InvHelper.count(handler, content, match) >= LEAVE_ONE_MINIMUM)) {
                return InvHelper.capped(content, content.getMaxStackSize());
            }
        }
        return ItemStack.EMPTY;
    }

    private int matchingEntry(ItemStack candidate) {
        for (int index = 0; index < entries.size(); index++) {
            ItemStack entry = entries.get(index);
            if (!entry.isEmpty() && match.test(entry, candidate)) {
                return index;
            }
        }
        return NO_ENTRY;
    }

    private int limitAt(int entry) {
        return entry < limits.size() ? limits.get(entry) : UNLIMITED;
    }
}
