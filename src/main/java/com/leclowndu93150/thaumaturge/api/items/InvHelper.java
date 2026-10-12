package com.leclowndu93150.thaumaturge.api.items;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/**
 * Static inventory access built on the NeoForge item resource transfer API: lookup, insertion, room checks, counting, extraction,
 * ejection into the world, and drawing ingredients from neighbouring containers or a player.
 *
 * <p>Item comparison is described by {@link ItemMatch}, and ghost-stack whitelists and blacklists by {@link StackFilter}.
 *
 * <p>Calls that change a world inventory, spawn an item entity or change a player inventory belong on the logical server. Lookups,
 * counts, room checks and {@link #playerHolds} only read and are safe on the client. Every member that changes an inventory opens and
 * closes its own root transaction, so none may be called from inside a caller's open transaction.
 *
 * @since 1.0.0
 */
public final class InvHelper {
    /**
     * Matches by item or shared item tag, with no damage or component relaxation.
     *
     * @deprecated use {@link ItemMatch#ITEM_OR_TAG}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static final InvFilter BASE_TAGS = new InvFilter(ItemMatch.ITEM_OR_TAG, true);

    private static final double EJECT_REACH = 1.0;
    private static final double EJECT_SPEED = 0.3;
    private static final double EYE_FRACTION = 0.5;

    private InvHelper() {}

    /**
     * @param level the level to query
     * @param pos   the block position
     * @param side  the face to access, or null for unsided access
     * @return the item handler exposed at the position, or null when there is none
     * @since 1.1.0
     */
    public static @Nullable ResourceHandler<ItemResource> handlerAt(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(Capabilities.Item.BLOCK, pos, side);
    }

    /**
     * Offers the whole stack to a handler in one root transaction.
     *
     * @param handler  the target, or null for an absent inventory
     * @param stack    the stack to insert; not modified
     * @param simulate whether to leave the handler unchanged
     * @return the unplaced remainder; the empty stack when everything was accepted, the input stack when the handler is null or the
     *         stack is empty
     */
    public static ItemStack insertStack(@Nullable ResourceHandler<ItemResource> handler, ItemStack stack, boolean simulate) {
        if (handler == null || stack.isEmpty()) {
            return stack;
        }
        int offered = stack.getCount();
        int accepted;
        try (Transaction transaction = Transaction.openRoot()) {
            accepted = handler.insert(ItemResource.of(stack), offered, transaction);
            if (!simulate) {
                transaction.commit();
            }
        }
        return copyOrEmpty(stack, offered - accepted);
    }

    /**
     * Offers the whole stack to the inventory exposed at a block face.
     *
     * @param level    the level
     * @param pos      the block holding the inventory
     * @param side     the face to insert through
     * @param stack    the stack to insert; not modified
     * @param simulate whether to leave the inventory unchanged
     * @return the unplaced remainder; the input stack when no inventory is exposed there
     * @since 1.1.0
     */
    public static ItemStack insertAt(Level level, BlockPos pos, Direction side, ItemStack stack, boolean simulate) {
        return insertStack(handlerAt(level, pos, side), stack, simulate);
    }

    /**
     * Reports how much of a stack an inventory would accept, without changing it.
     *
     * @param level the level
     * @param pos   the block holding the inventory
     * @param side  the face to insert through
     * @param stack the stack to test; not modified
     * @return a copy of the stack carrying the count that would be accepted, or the empty stack when none would be
     * @since 1.1.0
     */
    public static ItemStack acceptedPortion(Level level, BlockPos pos, Direction side, ItemStack stack) {
        return copyOrEmpty(stack, acceptedCount(level, pos, side, stack));
    }

    /**
     * @param level the level
     * @param pos   the block holding the inventory
     * @param side  the face to insert through
     * @param stack the stack to test
     * @return true when the stack is empty or at least one item of it would be accepted
     * @since 1.1.0
     */
    public static boolean acceptsAny(Level level, BlockPos pos, Direction side, ItemStack stack) {
        return stack.isEmpty() || acceptedCount(level, pos, side, stack) > 0;
    }

    /**
     * @param level the level
     * @param pos   the block holding the inventory
     * @param side  the face to insert through
     * @param stack the stack to test
     * @return true when the whole stack would be accepted
     * @since 1.1.0
     */
    public static boolean acceptsAll(Level level, BlockPos pos, Direction side, ItemStack stack) {
        return acceptedCount(level, pos, side, stack) == stack.getCount();
    }

    /**
     * @param handler the inventory, or null
     * @param wanted  the reference stack of every comparison; its count is ignored
     * @param match   the comparison rules
     * @return the number of matching items across all slots; zero for a null handler or an empty wanted stack
     * @since 1.1.0
     */
    public static int count(@Nullable ResourceHandler<ItemResource> handler, ItemStack wanted, ItemMatch match) {
        if (handler == null || wanted.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (int slot = 0; slot < handler.size(); slot++) {
            ItemResource resource = handler.getResource(slot);
            if (!resource.isEmpty() && match.test(wanted, resource.toStack())) {
                total += handler.getAmountAsInt(slot);
            }
        }
        return total;
    }

    /**
     * @param level  the level
     * @param pos    the block holding the inventory
     * @param side   the face to access
     * @param wanted the reference stack of every comparison; its count is ignored
     * @param match  the comparison rules
     * @return the number of matching items in the inventory exposed at the face; zero when there is none
     * @since 1.1.0
     */
    public static int countAt(Level level, BlockPos pos, Direction side, ItemStack wanted, ItemMatch match) {
        return count(handlerAt(level, pos, side), wanted, match);
    }

    /**
     * Adds up the counts of matching dropped items near a block.
     *
     * @param level  the level
     * @param pos    the block whose cell is the search centre
     * @param wanted the reference stack of every comparison; its count is ignored
     * @param range  the distance in blocks the cell is grown by on each axis in both directions
     * @param match  the comparison rules
     * @return the combined count of non-empty matching item entities touching the grown cell
     * @since 1.1.0
     */
    public static int countDropped(Level level, BlockPos pos, ItemStack wanted, double range, ItemMatch match) {
        if (wanted.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (ItemEntity drop : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(range))) {
            ItemStack dropped = drop.getItem();
            if (!dropped.isEmpty() && match.test(wanted, dropped)) {
                total += dropped.getCount();
            }
        }
        return total;
    }

    /**
     * Removes up to the wanted count of matching items from a handler, slot by slot in index order, in one root transaction.
     *
     * @param handler  the inventory, or null
     * @param wanted   the reference stack of every comparison; its count is the amount requested
     * @param match    the comparison rules
     * @param simulate whether to leave the inventory unchanged
     * @return a copy of the wanted stack carrying the removed count, or the empty stack when nothing was removed, the handler is null
     *         or the wanted stack is empty
     * @apiNote Under rules looser than {@link ItemMatch#EXACT} the removed items may differ from the wanted stack; the result always
     *          carries the wanted stack's item and components.
     * @since 1.1.0
     */
    public static ItemStack extract(@Nullable ResourceHandler<ItemResource> handler, ItemStack wanted, ItemMatch match, boolean simulate) {
        if (handler == null || wanted.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int removed;
        try (Transaction transaction = Transaction.openRoot()) {
            removed = drain(handler, wanted, match, wanted.getCount(), transaction);
            if (!simulate) {
                transaction.commit();
            }
        }
        return copyOrEmpty(wanted, removed);
    }

    /**
     * Behaves as {@link #extract(ResourceHandler, ItemStack, ItemMatch, boolean)} on the inventory exposed at a block face.
     *
     * @param level    the level
     * @param pos      the block holding the inventory
     * @param side     the face to access
     * @param wanted   the reference stack; its count is the amount requested
     * @param match    the comparison rules
     * @param simulate whether to leave the inventory unchanged
     * @return a copy of the wanted stack carrying the removed count, or the empty stack
     * @since 1.1.0
     */
    public static ItemStack extractAt(Level level, BlockPos pos, Direction side, ItemStack wanted, ItemMatch match, boolean simulate) {
        return extract(handlerAt(level, pos, side), wanted, match, simulate);
    }

    /**
     * Pushes a stack out of a block toward one side. The stack is first offered to the inventory exposed by the neighbour on that
     * side; whatever it refuses is dropped into the world as described in {@link #ejectOrReturn}. Does nothing on the client.
     *
     * @param level the level
     * @param pos   the origin block
     * @param side  the direction to eject toward
     * @param stack the stack to eject; not modified
     * @since 1.1.0
     */
    public static void eject(Level level, BlockPos pos, Direction side, ItemStack stack) {
        push(level, pos, side, stack, false);
    }

    /**
     * Pushes a stack out of a block toward one side, handing back what a neighbouring inventory refuses.
     *
     * <p>The stack is first offered to the inventory exposed by the neighbour on that side, through its facing side. When such an
     * inventory exists, whatever it refuses is returned to the caller. When none exists, the stack is dropped as an item entity: it
     * appears horizontally centred on the origin block, one block out along the face, at the floor level of the origin block plus
     * the vertical step of the face. When the neighbour is a full solid block it appears at the origin's horizontal centre and floor
     * level instead. It moves straight outward at 0.3 blocks per tick and has no pickup delay.
     *
     * @param level the level
     * @param pos   the origin block
     * @param side  the direction to eject toward
     * @param stack the stack to eject; not modified
     * @return the refused part when a neighbouring inventory exists, otherwise the empty stack; on the client nothing happens and the
     *         input stack is returned
     * @since 1.1.0
     */
    public static ItemStack ejectOrReturn(Level level, BlockPos pos, Direction side, ItemStack stack) {
        return push(level, pos, side, stack, true);
    }

    /**
     * Drops a copy of a stack at an entity, at its horizontal position and half its eye height above its feet. The item gets the
     * usual small random hop (up to 0.1 sideways, 0.2 up). Does nothing on the client or for an empty stack.
     *
     * @param level  the level
     * @param stack  the stack to drop; not modified
     * @param entity the entity to drop at
     * @since 1.1.0
     */
    public static void dropAt(Level level, ItemStack stack, Entity entity) {
        if (level.isClientSide() || stack.isEmpty()) {
            return;
        }
        double y = entity.getY() + entity.getEyeHeight() * EYE_FRACTION;
        level.addFreshEntity(new ItemEntity(level, entity.getX(), y, entity.getZ(), stack.copy()));
    }

    /**
     * @param stack the stack to copy
     * @param limit the largest count of the copy
     * @return a copy with the smaller of the stack's count and the limit, or the empty stack when the result would hold nothing
     * @since 1.1.0
     */
    public static ItemStack capped(ItemStack stack, int limit) {
        return stack.isEmpty() ? ItemStack.EMPTY : copyOrEmpty(stack, Math.min(stack.getCount(), limit));
    }

    /**
     * Tests whether the containers on the five sides of a block other than the top together hold a stack's count, matched under
     * {@link ItemMatch#ITEM_OR_TAG}.
     *
     * @param level the level
     * @param pos   the origin block
     * @param stack the stack to find; its count is the amount needed
     * @return whether enough matching items are held; true for an empty stack
     * @since 1.1.0
     */
    public static boolean neighboursHold(Level level, BlockPos pos, ItemStack stack) {
        return countNeighbours(level, pos, stack) >= stack.getCount();
    }

    /**
     * Removes a stack's count from the containers on the five sides of a block other than the top, in direction order, matched under
     * {@link ItemMatch#ITEM_OR_TAG}. All or nothing: when the containers together hold too few, none is changed.
     *
     * @param level the level; the logical server
     * @param pos   the origin block
     * @param stack the stack to remove; not modified
     * @return whether the whole count was removed
     * @since 1.1.0
     */
    public static boolean takeFromNeighbours(Level level, BlockPos pos, ItemStack stack) {
        try (Transaction transaction = Transaction.openRoot()) {
            if (drainNeighbours(level, pos, stack, transaction) < stack.getCount()) {
                return false;
            }
            transaction.commit();
            return true;
        }
    }

    /**
     * Tests whether a player's main inventory, the hotbar and storage slots but not armour or the off hand, holds a stack's count.
     *
     * <p>Matching ignores data components when the wanted stack carries none of its own, and otherwise requires the wanted stack's
     * components to be a subset of the held stack's. Damage always has to agree.
     *
     * @param player the player
     * @param wanted the wanted stack; its count is the amount needed
     * @param byTag  whether items sharing an item tag also match
     * @return whether enough matching items are held; false for an empty wanted stack
     * @since 1.1.0
     */
    public static boolean playerHolds(Player player, ItemStack wanted, boolean byTag) {
        return !wanted.isEmpty() && count(mainSlots(player), wanted, playerMatch(wanted, byTag)) >= wanted.getCount();
    }

    /**
     * Removes a stack's count from a player's main inventory, slot by slot in order, matching as {@link #playerHolds} does. All or
     * nothing: when the player holds too few, the inventory is unchanged. The normal container synchronisation carries the change to
     * the client.
     *
     * @param player the player; on the logical server
     * @param wanted the wanted stack; not modified
     * @param byTag  whether items sharing an item tag also match
     * @return whether the whole count was removed; false for an empty wanted stack
     * @since 1.1.0
     */
    public static boolean takeFromPlayer(Player player, ItemStack wanted, boolean byTag) {
        return drainPlayer(player, wanted, byTag, false);
    }

    /**
     * Draws a set of ingredients from the containers around a block, falling back on a player.
     *
     * <p>Each stack, judged on its own, must be fully available either in the containers on the five sides other than the top (as
     * {@link #neighboursHold}) or in the player's main inventory with tag matching on (as {@link #playerHolds}); otherwise the call
     * fails. Unless simulating, each stack is then taken from the containers first, and only the shortfall is taken from the player.
     * The whole draw is one transaction: if the sources run out part way, for example because two ingredients compete for the same
     * items, nothing is changed and the call fails.
     *
     * @param level    the level; the logical server unless simulating
     * @param pos      the origin block
     * @param player   the player to fall back on
     * @param simulate whether to only report availability
     * @param stacks   the ingredients; not modified
     * @return whether every ingredient is available, and when not simulating, whether all were taken
     * @since 1.1.0
     */
    public static boolean takeFromNeighboursOrPlayer(Level level, BlockPos pos, Player player, boolean simulate, ItemStack... stacks) {
        for (ItemStack stack : stacks) {
            if (!neighboursHold(level, pos, stack) && !playerHolds(player, stack, true)) {
                return false;
            }
        }
        if (simulate) {
            return true;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            ResourceHandler<ItemResource> inventory = mainSlots(player);
            for (ItemStack stack : stacks) {
                int shortfall = stack.getCount() - drainNeighbours(level, pos, stack, transaction);
                if (shortfall > 0 && drain(inventory, stack, playerMatch(stack, true), shortfall, transaction) < shortfall) {
                    return false;
                }
            }
            transaction.commit();
            return true;
        }
    }

    /**
     * @param level the level
     * @param pos   the block position
     * @param side  the face to access, or null for unsided access
     * @return the item handler at the position, or null when there is none
     * @deprecated use {@link #handlerAt(Level, BlockPos, Direction)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static @Nullable ResourceHandler<ItemResource> getItemHandlerAt(Level level, BlockPos pos, @Nullable Direction side) {
        return handlerAt(level, pos, side);
    }

    /**
     * @param level    the level
     * @param pos      the block holding the inventory
     * @param side     the face to insert through
     * @param stack    the stack to insert; not modified
     * @param simulate whether to leave the inventory unchanged
     * @return the unplaced remainder
     * @deprecated use {@link #insertAt(Level, BlockPos, Direction, ItemStack, boolean)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static ItemStack insertStackAt(Level level, BlockPos pos, Direction side, ItemStack stack, boolean simulate) {
        return insertAt(level, pos, side, stack, simulate);
    }

    /**
     * @param level the level
     * @param pos   the block holding the inventory
     * @param side  the face to insert through
     * @param stack the stack to test
     * @return a copy carrying the count that would be accepted, or the empty stack
     * @deprecated use {@link #acceptedPortion(Level, BlockPos, Direction, ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static ItemStack hasRoomFor(Level level, BlockPos pos, Direction side, ItemStack stack) {
        return acceptedPortion(level, pos, side, stack);
    }

    /**
     * @param level the level
     * @param pos   the block holding the inventory
     * @param side  the face to insert through
     * @param stack the stack to test
     * @return true when the stack is empty or some of it would be accepted
     * @deprecated use {@link #acceptsAny(Level, BlockPos, Direction, ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean hasRoomForSome(Level level, BlockPos pos, Direction side, ItemStack stack) {
        return acceptsAny(level, pos, side, stack);
    }

    /**
     * @param level the level
     * @param pos   the block holding the inventory
     * @param side  the face to insert through
     * @param stack the stack to test
     * @return true when the whole stack would be accepted
     * @deprecated use {@link #acceptsAll(Level, BlockPos, Direction, ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean hasRoomForAll(Level level, BlockPos pos, Direction side, ItemStack stack) {
        return acceptsAll(level, pos, side, stack);
    }

    /**
     * @param handler the inventory, or null
     * @param wanted  the stack to count
     * @param filter  the comparison flags
     * @return the number of matching items
     * @deprecated use {@link #count(ResourceHandler, ItemStack, ItemMatch)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static int countTotalItemsIn(@Nullable ResourceHandler<ItemResource> handler, ItemStack wanted, InvFilter filter) {
        return count(handler, wanted, filter.match);
    }

    /**
     * @param level  the level
     * @param pos    the block holding the inventory
     * @param side   the face to access
     * @param wanted the stack to count
     * @param filter the comparison flags
     * @return the number of matching items
     * @deprecated use {@link #countAt(Level, BlockPos, Direction, ItemStack, ItemMatch)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static int countTotalItemsIn(Level level, BlockPos pos, Direction side, ItemStack wanted, InvFilter filter) {
        return countAt(level, pos, side, wanted, filter.match);
    }

    /**
     * @param first  the reference stack
     * @param second the candidate stack
     * @param filter the comparison flags
     * @return whether the stacks are equal under the filter
     * @deprecated use {@link ItemMatch#test(ItemStack, ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean areItemStacksEqual(ItemStack first, ItemStack second, InvFilter filter) {
        return filter.match.test(first, second);
    }

    /**
     * @param ghosts    the ghost stacks
     * @param blacklist true for a blacklist, false for a whitelist
     * @param handler   the inventory to search, or null
     * @param filter    the comparison flags
     * @param leaveOne  whether at least two matching items must be held
     * @return the first passing content, capped at its maximum stack size, or the empty stack
     * @deprecated use {@link StackFilter#firstAccepted(ResourceHandler, boolean)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static ItemStack findFirstMatchFromFilter(List<ItemStack> ghosts, boolean blacklist, @Nullable ResourceHandler<ItemResource> handler, InvFilter filter, boolean leaveOne) {
        return StackFilter.of(ghosts, blacklist, filter.match).firstAccepted(handler, leaveOne);
    }

    /**
     * @param ghosts    the ghost stacks
     * @param blacklist true for a blacklist, false for a whitelist
     * @param stack     the candidate
     * @param filter    the comparison flags
     * @return whether the candidate passes
     * @deprecated use {@link StackFilter#accepts(ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean matchesFilters(List<ItemStack> ghosts, boolean blacklist, ItemStack stack, InvFilter filter) {
        return StackFilter.of(ghosts, blacklist, filter.match).accepts(stack);
    }

    /**
     * @param ghosts     the ghost stacks
     * @param sizes      the size limits, aligned with the ghosts
     * @param blacklist  true for a blacklist, false for a whitelist
     * @param candidates the stacks to search in order
     * @param filter     the comparison flags
     * @return the first passing candidate, or the empty stack
     * @deprecated use {@link StackFilter#firstAccepted(List)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static ItemStack findFirstMatchFromFilter(List<ItemStack> ghosts, List<Integer> sizes, boolean blacklist, List<ItemStack> candidates, InvFilter filter) {
        return new StackFilter(ghosts, sizes, blacklist, filter.match).firstAccepted(candidates);
    }

    /**
     * @param ghosts     the ghost stacks
     * @param sizes      the size limits, aligned with the ghosts
     * @param blacklist  true for a blacklist, false for a whitelist
     * @param candidates the stacks to search in order
     * @param filter     the comparison flags
     * @return the first passing candidate with its size limit
     * @deprecated use {@link StackFilter#firstAcceptedWithLimit(List)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static FilterMatch findFirstMatchFromFilterWithSize(List<ItemStack> ghosts, List<Integer> sizes, boolean blacklist, List<ItemStack> candidates, InvFilter filter) {
        return new StackFilter(ghosts, sizes, blacklist, filter.match).firstAcceptedWithLimit(candidates);
    }

    /**
     * @param stack the stack to copy
     * @param limit the largest count of the copy
     * @return the capped copy
     * @deprecated use {@link #capped(ItemStack, int)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static ItemStack copyLimitedStack(ItemStack stack, int limit) {
        return capped(stack, limit);
    }

    /**
     * @param level    the level
     * @param pos      the block holding the inventory
     * @param side     the face to access
     * @param wanted   the stack to extract
     * @param filter   the comparison flags
     * @param simulate whether to leave the inventory unchanged
     * @return a copy of the wanted stack carrying the removed count, or the empty stack
     * @deprecated use {@link #extractAt(Level, BlockPos, Direction, ItemStack, ItemMatch, boolean)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static ItemStack removeStackFrom(Level level, BlockPos pos, Direction side, ItemStack wanted, InvFilter filter, boolean simulate) {
        return extractAt(level, pos, side, wanted, filter.match, simulate);
    }

    /**
     * @param handler  the inventory, or null
     * @param wanted   the stack to extract
     * @param filter   the comparison flags
     * @param simulate whether to leave the inventory unchanged
     * @return a copy of the wanted stack carrying the removed count, or the empty stack
     * @deprecated use {@link #extract(ResourceHandler, ItemStack, ItemMatch, boolean)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static ItemStack removeStackFrom(@Nullable ResourceHandler<ItemResource> handler, ItemStack wanted, InvFilter filter, boolean simulate) {
        return extract(handler, wanted, filter.match, simulate);
    }

    /**
     * @param level the level
     * @param pos   the origin position
     * @param side  the direction to eject toward
     * @param stack the stack to eject
     * @deprecated use {@link #eject(Level, BlockPos, Direction, ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void ejectStackAt(Level level, BlockPos pos, Direction side, ItemStack stack) {
        eject(level, pos, side, stack);
    }

    /**
     * @param level the level
     * @param pos   the origin position
     * @param side  the direction to eject toward
     * @param stack the stack to eject
     * @param smart whether a neighbouring inventory hands its refusal back to the caller
     * @return the refused part when smart is set and an inventory exists on that side, otherwise the empty stack
     * @deprecated use {@link #ejectOrReturn(Level, BlockPos, Direction, ItemStack)}, or {@link #eject(Level, BlockPos, Direction,
     *             ItemStack)} when smart is not set
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static ItemStack ejectStackAt(Level level, BlockPos pos, Direction side, ItemStack stack, boolean smart) {
        return push(level, pos, side, stack, smart);
    }

    /**
     * @param level  the level
     * @param pos    the search centre
     * @param wanted the stack to count
     * @param range  the distance added on each axis in both directions
     * @param filter the comparison flags
     * @return the combined count of matching dropped items
     * @deprecated use {@link #countDropped(Level, BlockPos, ItemStack, double, ItemMatch)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static int countStackInWorld(Level level, BlockPos pos, ItemStack wanted, double range, InvFilter filter) {
        return countDropped(level, pos, wanted, range, filter.match);
    }

    /**
     * @param level  the level
     * @param stack  the stack to drop
     * @param entity the entity to drop at
     * @deprecated use {@link #dropAt(Level, ItemStack, Entity)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void dropItemAtEntity(Level level, ItemStack stack, Entity entity) {
        dropAt(level, stack, entity);
    }

    /**
     * @param level the level
     * @param pos   the origin position
     * @param stack the stack to find
     * @return whether the neighbours other than the one above hold the stack's count
     * @deprecated use {@link #neighboursHold(Level, BlockPos, ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean checkAdjacentChests(Level level, BlockPos pos, ItemStack stack) {
        return neighboursHold(level, pos, stack);
    }

    /**
     * @param level the level
     * @param pos   the origin position
     * @param stack the stack to remove
     * @return whether the whole count was removed
     * @deprecated use {@link #takeFromNeighbours(Level, BlockPos, ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean consumeFromAdjacentChests(Level level, BlockPos pos, ItemStack stack) {
        return takeFromNeighbours(level, pos, stack);
    }

    /**
     * @param player the player
     * @param wanted the wanted stack
     * @param tags   whether to match by shared item tags
     * @return whether the main inventory holds the wanted count
     * @deprecated use {@link #playerHolds(Player, ItemStack, boolean)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean isPlayerCarryingAmount(Player player, ItemStack wanted, boolean tags) {
        return playerHolds(player, wanted, tags);
    }

    /**
     * With the check skipped, whatever matching items are held are removed even when they fall short of the wanted count.
     *
     * @param player    the player
     * @param wanted    the wanted stack
     * @param skipCheck whether to remove what is held without first checking the full count is there
     * @param tags      whether to match by shared item tags
     * @return whether the whole count was removed
     * @deprecated use {@link #takeFromPlayer(Player, ItemStack, boolean)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean consumePlayerItem(Player player, ItemStack wanted, boolean skipCheck, boolean tags) {
        return drainPlayer(player, wanted, tags, skipCheck);
    }

    /**
     * @param level    the level
     * @param pos      the origin position
     * @param player   the player
     * @param simulate whether to only report availability
     * @param stacks   the stacks to consume
     * @return whether every stack was available, and when not simulating, taken
     * @deprecated use {@link #takeFromNeighboursOrPlayer(Level, BlockPos, Player, boolean, ItemStack...)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean consumeItemsFromAdjacentInventoryOrPlayer(Level level, BlockPos pos, Player player, boolean simulate, ItemStack... stacks) {
        return takeFromNeighboursOrPlayer(level, pos, player, simulate, stacks);
    }

    private static ItemStack push(Level level, BlockPos pos, Direction side, ItemStack stack, boolean handBack) {
        if (level.isClientSide()) {
            return stack;
        }
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ResourceHandler<ItemResource> neighbour = handlerAt(level, pos.relative(side), side.getOpposite());
        ItemStack refused = insertStack(neighbour, stack, false);
        if (refused.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (handBack && neighbour != null) {
            return refused;
        }
        spawnEjected(level, pos, side, refused.copy());
        return ItemStack.EMPTY;
    }

    private static void spawnEjected(Level level, BlockPos pos, Direction side, ItemStack stack) {
        BlockPos ahead = pos.relative(side);
        boolean blocked = level.getBlockState(ahead).isCollisionShapeFullBlock(level, ahead);
        Vec3 outward = new Vec3(side.getStepX(), side.getStepY(), side.getStepZ());
        Vec3 floorCentre = Vec3.atBottomCenterOf(pos);
        Vec3 spot = blocked ? floorCentre : floorCentre.add(outward.scale(EJECT_REACH));
        Vec3 motion = outward.scale(EJECT_SPEED);
        level.addFreshEntity(new ItemEntity(level, spot.x, spot.y, spot.z, stack, motion.x, motion.y, motion.z));
    }

    private static int acceptedCount(Level level, BlockPos pos, Direction side, ItemStack stack) {
        return stack.getCount() - insertAt(level, pos, side, stack, true).getCount();
    }

    private static int countNeighbours(Level level, BlockPos pos, ItemStack stack) {
        int total = 0;
        for (Direction direction : Direction.values()) {
            if (direction != Direction.UP) {
                total += count(handlerAt(level, pos.relative(direction), direction.getOpposite()), stack, ItemMatch.ITEM_OR_TAG);
            }
        }
        return total;
    }

    private static int drainNeighbours(Level level, BlockPos pos, ItemStack stack, TransactionContext transaction) {
        int taken = 0;
        for (Direction direction : Direction.values()) {
            if (taken >= stack.getCount()) {
                break;
            }
            if (direction == Direction.UP) {
                continue;
            }
            ResourceHandler<ItemResource> handler = handlerAt(level, pos.relative(direction), direction.getOpposite());
            if (handler != null) {
                taken += drain(handler, stack, ItemMatch.ITEM_OR_TAG, stack.getCount() - taken, transaction);
            }
        }
        return taken;
    }

    private static boolean drainPlayer(Player player, ItemStack wanted, boolean byTag, boolean keepPartial) {
        if (wanted.isEmpty()) {
            return false;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            boolean whole = drain(mainSlots(player), wanted, playerMatch(wanted, byTag), wanted.getCount(), transaction) == wanted.getCount();
            if (whole || keepPartial) {
                transaction.commit();
            }
            return whole;
        }
    }

    private static int drain(ResourceHandler<ItemResource> handler, ItemStack wanted, ItemMatch match, int amount, TransactionContext transaction) {
        int taken = 0;
        for (int slot = 0; slot < handler.size() && taken < amount; slot++) {
            ItemResource resource = handler.getResource(slot);
            if (!resource.isEmpty() && match.test(wanted, resource.toStack())) {
                taken += handler.extract(slot, resource, amount - taken, transaction);
            }
        }
        return taken;
    }

    private static ResourceHandler<ItemResource> mainSlots(Player player) {
        return PlayerInventoryWrapper.of(player).getMainSlots();
    }

    private static ItemMatch playerMatch(ItemStack wanted, boolean byTag) {
        return ItemMatch.EXACT.ignoringComponents(wanted.isComponentsPatchEmpty()).withComponentSubset(true).byTag(byTag);
    }

    private static ItemStack copyOrEmpty(ItemStack stack, int count) {
        return count > 0 ? stack.copyWithCount(count) : ItemStack.EMPTY;
    }

    /**
     * The result of a size-aware filter search.
     *
     * @param stack     the matching stack, or the empty stack
     * @param sizeLimit the size limit that goes with the match; zero when unlimited
     * @since 1.0.0
     */
    public record FilterMatch(ItemStack stack, int sizeLimit) {
    }

    /**
     * Mutable comparison flags for the 1.0 comparison members.
     *
     * @since 1.0.0
     * @deprecated use the immutable {@link ItemMatch}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static final class InvFilter {
        /**
         * No switch set.
         *
         * @deprecated use {@link ItemMatch#EXACT}
         */
        @Deprecated(since = "1.1.0", forRemoval = true)
        public static final InvFilter STRICT = new InvFilter(ItemMatch.EXACT, true);

        private final boolean shared;
        private ItemMatch match;

        /**
         * @param ignoreDamage     whether differing damage values still match
         * @param ignoreComponents whether differing data components still match
         * @param byTags           whether items sharing an item tag match
         * @param byMod            whether items from the same namespace match
         * @deprecated use {@link ItemMatch#ItemMatch(boolean, boolean, boolean, boolean, boolean)}
         */
        @Deprecated(since = "1.1.0", forRemoval = true)
        public InvFilter(boolean ignoreDamage, boolean ignoreComponents, boolean byTags, boolean byMod) {
            this(new ItemMatch(ignoreDamage, ignoreComponents, false, byTags, byMod), false);
        }

        private InvFilter(ItemMatch match, boolean shared) {
            this.match = match;
            this.shared = shared;
        }

        /**
         * Switches on the component subset rule. The switch is never cleared.
         *
         * @return this filter; for the shared {@link #STRICT} and {@link #BASE_TAGS} constants, a new relaxed filter instead, so the
         *         constants never change
         * @deprecated use {@link ItemMatch#withComponentSubset(boolean)}
         */
        @Deprecated(since = "1.1.0", forRemoval = true)
        public InvFilter setRelaxedComponents() {
            ItemMatch relaxed = match.withComponentSubset(true);
            if (shared) {
                return new InvFilter(relaxed, false);
            }
            match = relaxed;
            return this;
        }
    }
}
