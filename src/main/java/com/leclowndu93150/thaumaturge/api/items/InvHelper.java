package com.leclowndu93150.thaumaturge.api.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

/**
 * Static inventory access and ghost-stack filter matching, built on the NeoForge item resource transfer API.
 *
 * <p>Calls that touch a world handler, an item entity or a player inventory belong on the logical server. The pure comparison and
 * filter functions and {@link #isPlayerCarryingAmount} are safe on the client. Every transaction is opened and closed inside a single
 * call, so no member may be called from inside a caller's own root transaction.
 *
 * @since 1.0.0
 */
public final class InvHelper {
    /** Matches by item or shared item tag, with no damage or component relaxation. */
    public static final InvFilter BASE_TAGS = new InvFilter(false, false, true, false);

    private static final double EJECT_SPEED = 0.3;
    private static final double CELL_CENTER = 0.5;
    private static final double EYE_FRACTION = 0.5;
    private static final int NO_MATCH = -1;
    private static final int LEAVE_ONE_MINIMUM = 2;
    private static final int PLAYER_FILTER_COMPONENTS_BIT = 1;
    private static final int PLAYER_FILTER_TAGS_BIT = 1 << 1;
    private static final int PLAYER_FILTER_VARIANTS = 4;
    private static final int[] MAIN_INVENTORY_SLOTS = IntStream.range(0, Inventory.INVENTORY_SIZE).toArray();
    private static final InvFilter[] PLAYER_FILTERS = buildPlayerFilters();

    private InvHelper() {}

    /**
     * @param level the level to query
     * @param pos   the block position
     * @param side  the face to access, or null for unsided access
     * @return the item handler at the position, or null when there is none
     */
    public static @Nullable ResourceHandler<ItemResource> getItemHandlerAt(Level level, BlockPos pos, @Nullable Direction side) {
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
     * @param level    the level
     * @param pos      the block holding the inventory
     * @param side     the face to insert through
     * @param stack    the stack to insert; not modified
     * @param simulate whether to leave the inventory unchanged
     * @return the unplaced remainder; the input stack when no handler exists at the position
     */
    public static ItemStack insertStackAt(Level level, BlockPos pos, Direction side, ItemStack stack, boolean simulate) {
        ResourceHandler<ItemResource> handler = getItemHandlerAt(level, pos, side);
        return handler == null ? stack : insertStack(handler, stack, simulate);
    }

    /**
     * Reports how much of the stack an inventory would accept without changing it.
     *
     * @param level the level
     * @param pos   the block holding the inventory
     * @param side  the face to insert through
     * @param stack the stack to test
     * @return a copy of the stack when all of it fits, a copy carrying only the accepted count when part fits, otherwise the empty
     *         stack
     */
    public static ItemStack hasRoomFor(Level level, BlockPos pos, Direction side, ItemStack stack) {
        return copyOrEmpty(stack, acceptableCount(level, pos, side, stack));
    }

    /**
     * @param level the level
     * @param pos   the block holding the inventory
     * @param side  the face to insert through
     * @param stack the stack to test
     * @return true when the stack is empty or at least one item of it would be accepted
     */
    public static boolean hasRoomForSome(Level level, BlockPos pos, Direction side, ItemStack stack) {
        return stack.isEmpty() || acceptableCount(level, pos, side, stack) > 0;
    }

    /**
     * @param level the level
     * @param pos   the block holding the inventory
     * @param side  the face to insert through
     * @param stack the stack to test
     * @return true when the whole stack would be accepted
     */
    public static boolean hasRoomForAll(Level level, BlockPos pos, Direction side, ItemStack stack) {
        return acceptableCount(level, pos, side, stack) >= stack.getCount();
    }

    /**
     * @param handler the inventory, or null
     * @param wanted  the stack to count, the first operand of every comparison
     * @param filter  the comparison flags
     * @return the number of matching items across all slots; zero for a null handler
     */
    public static int countTotalItemsIn(@Nullable ResourceHandler<ItemResource> handler, ItemStack wanted, InvFilter filter) {
        return handler == null ? 0 : IntStream.range(0, handler.size()).map(slot -> matchingAmount(handler, slot, wanted, filter)).sum();
    }

    /**
     * @param level  the level
     * @param pos    the block holding the inventory
     * @param side   the face to access
     * @param wanted the stack to count
     * @param filter the comparison flags
     * @return the number of matching items in the inventory at the position
     */
    public static int countTotalItemsIn(Level level, BlockPos pos, Direction side, ItemStack wanted, InvFilter filter) {
        ResourceHandler<ItemResource> handler = getItemHandlerAt(level, pos, side);
        return handler == null ? 0 : countTotalItemsIn(handler, wanted, filter);
    }

    /**
     * Compares two stacks under a filter. Counts never take part. The first applicable rule decides: an empty stack equals only
     * another empty stack; then mod match, tag match, item identity, damage, ignored components, relaxed components, and finally exact
     * component equality (leaving out damage when damage is ignored). Only the tag rule and the relaxed rule are directional, with
     * {@code first} as the tested side.
     *
     * @param first  the first stack
     * @param second the second stack
     * @param filter the comparison flags
     * @return whether the stacks are equal under the filter
     */
    public static boolean areItemStacksEqual(ItemStack first, ItemStack second, InvFilter filter) {
        if (first.isEmpty() != second.isEmpty()) {
            return false;
        }
        if (first.isEmpty()) {
            return true;
        }
        boolean equal = sameItemAndData(first, second, filter);
        if (filter.has(InvFilter.BY_TAGS)) {
            equal = sharesItemOrTag(first, second);
        }
        if (filter.has(InvFilter.BY_MOD)) {
            equal = namespaceOf(first).equals(namespaceOf(second));
        }
        return equal;
    }

    /**
     * Searches a handler for the first slot content that passes a whitelist or blacklist of ghost stacks. The source is unchanged.
     *
     * @param ghosts    the ghost stacks; empty ones are ignored
     * @param blacklist true to forbid a matching ghost, false to require one
     * @param handler   the inventory to search, or null
     * @param filter    the comparison flags between a ghost and a candidate
     * @param leaveOne  whether a candidate is offered only when the handler holds at least two matching items
     * @return a candidate with its count capped at the item's maximum stack size, or the empty stack
     */
    public static ItemStack findFirstMatchFromFilter(List<ItemStack> ghosts, boolean blacklist, @Nullable ResourceHandler<ItemResource> handler, InvFilter filter, boolean leaveOne) {
        if (handler == null) {
            return ItemStack.EMPTY;
        }
        GhostFilter ghostFilter = new GhostFilter(ghosts, filter);
        for (int slot = 0; slot < handler.size(); slot++) {
            ItemResource resource = handler.getResource(slot);
            if (resource.isEmpty()) {
                continue;
            }
            ItemStack candidate = resource.toStack(Math.min(handler.getAmountAsInt(slot), resource.getMaxStackSize()));
            if (ghostFilter.passes(blacklist, candidate) && (!leaveOne || countTotalItemsIn(handler, candidate, filter) >= LEAVE_ONE_MINIMUM)) {
                return candidate;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * @param ghosts    the ghost stacks; empty ones are ignored
     * @param blacklist true for a blacklist, false for a whitelist
     * @param stack     the candidate
     * @param filter    the comparison flags between a ghost and the candidate
     * @return whether the candidate passes; an empty candidate never does
     */
    public static boolean matchesFilters(List<ItemStack> ghosts, boolean blacklist, ItemStack stack, InvFilter filter) {
        return new GhostFilter(ghosts, filter).passes(blacklist, stack);
    }

    /**
     * @param ghosts     the ghost stacks; empty ones are ignored
     * @param sizes      the size limits, aligned by position with the ghosts
     * @param blacklist  true for a blacklist, false for a whitelist
     * @param candidates the stacks to search in order
     * @param filter     the comparison flags between a ghost and a candidate
     * @return the first passing candidate, or the empty stack
     * @throws IndexOutOfBoundsException when a matching whitelist ghost has no entry in {@code sizes}
     */
    public static ItemStack findFirstMatchFromFilter(List<ItemStack> ghosts, List<Integer> sizes, boolean blacklist, List<ItemStack> candidates, InvFilter filter) {
        return findFirstMatchFromFilterWithSize(ghosts, sizes, blacklist, candidates, filter).stack();
    }

    /**
     * @param ghosts     the ghost stacks; empty ones are ignored
     * @param sizes      the size limits, aligned by position with the ghosts
     * @param blacklist  true for a blacklist, false for a whitelist
     * @param candidates the stacks to search in order
     * @param filter     the comparison flags between a ghost and a candidate
     * @return the first passing candidate with its size limit; the limit is the matching ghost's entry under a whitelist and zero
     *         under a blacklist or when nothing passes
     * @throws IndexOutOfBoundsException when a matching whitelist ghost has no entry in {@code sizes}
     */
    public static FilterMatch findFirstMatchFromFilterWithSize(List<ItemStack> ghosts, List<Integer> sizes, boolean blacklist, List<ItemStack> candidates, InvFilter filter) {
        GhostFilter ghostFilter = new GhostFilter(ghosts, filter);
        for (ItemStack candidate : candidates) {
            if (candidate.isEmpty()) {
                continue;
            }
            int index = ghostFilter.firstMatch(candidate);
            if (blacklist ? index == NO_MATCH : index != NO_MATCH) {
                return new FilterMatch(candidate, blacklist ? 0 : sizes.get(index));
            }
        }
        return new FilterMatch(ItemStack.EMPTY, 0);
    }

    /**
     * @param stack the stack to copy
     * @param limit the largest count of the copy
     * @return a copy with the smaller of the stack's count and the limit, or the empty stack for an empty input
     */
    public static ItemStack copyLimitedStack(ItemStack stack, int limit) {
        return stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(Math.min(stack.getCount(), limit));
    }

    /**
     * @param level    the level
     * @param pos      the block holding the inventory
     * @param side     the face to access
     * @param wanted   the stack to extract, its count being the amount requested
     * @param filter   the comparison flags
     * @param simulate whether to leave the inventory unchanged
     * @return a copy of the wanted stack carrying the removed count, or the empty stack when nothing was removed
     */
    public static ItemStack removeStackFrom(Level level, BlockPos pos, Direction side, ItemStack wanted, InvFilter filter, boolean simulate) {
        ResourceHandler<ItemResource> handler = getItemHandlerAt(level, pos, side);
        return handler == null ? ItemStack.EMPTY : removeStackFrom(handler, wanted, filter, simulate);
    }

    /**
     * Drains matching stacks from the slots in index order within one root transaction.
     *
     * @param handler  the inventory, or null
     * @param wanted   the stack to extract, the first operand of every comparison
     * @param filter   the comparison flags
     * @param simulate whether to leave the inventory unchanged
     * @return a copy of the wanted stack carrying the removed count, or the empty stack when nothing was removed or the handler is
     *         null or the wanted stack is empty
     */
    public static ItemStack removeStackFrom(@Nullable ResourceHandler<ItemResource> handler, ItemStack wanted, InvFilter filter, boolean simulate) {
        if (handler == null || wanted.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int needed = wanted.getCount();
        int removed = 0;
        try (Transaction transaction = Transaction.openRoot()) {
            for (int slot = 0; slot < handler.size() && removed < needed; slot++) {
                ItemResource resource = handler.getResource(slot);
                if (!resource.isEmpty() && areItemStacksEqual(wanted, resource.toStack(), filter)) {
                    removed += handler.extract(slot, resource, needed - removed, transaction);
                }
            }
            if (!simulate) {
                transaction.commit();
            }
        }
        return removed == 0 ? ItemStack.EMPTY : wanted.copyWithCount(removed);
    }

    /**
     * Ejects a stack, discarding the result. Behaves as {@link #ejectStackAt(Level, BlockPos, Direction, ItemStack, boolean)} with
     * the smart flag off.
     *
     * @param level the server level
     * @param pos   the origin position
     * @param side  the direction to eject toward
     * @param stack the stack to eject
     */
    public static void ejectStackAt(Level level, BlockPos pos, Direction side, ItemStack stack) {
        ejectStackAt(level, pos, side, stack, false);
    }

    /**
     * Places the stack into the inventory of the neighbouring block on a side and spawns what does not fit as an item entity moving
     * outward. The caller guarantees a server level.
     *
     * @param level the server level
     * @param pos   the origin position
     * @param side  the direction to eject toward
     * @param stack the stack to eject
     * @param smart whether a neighbouring inventory takes responsibility for the leftover
     * @return the leftover when smart is set and an inventory exists on that side; otherwise the empty stack
     */
    public static ItemStack ejectStackAt(Level level, BlockPos pos, Direction side, ItemStack stack, boolean smart) {
        EjectPlacement placement = EjectPlacement.compute(level, pos, side);
        EjectHandoff handoff = EjectHandoff.perform(level, pos, side, stack);
        if (handoff.returnsLeftover(smart)) {
            return handoff.leftover();
        }
        if (!handoff.leftover().isEmpty()) {
            level.addFreshEntity(placement.createEntity(level, handoff.leftover()));
        }
        return ItemStack.EMPTY;
    }

    /**
     * @param level  the level
     * @param pos    the block whose cell is the search centre
     * @param wanted the stack to count, the first operand of every comparison
     * @param range  the distance in blocks added on each axis in both directions
     * @param filter the comparison flags
     * @return the combined count of matching dropped items touching the grown cell
     */
    public static int countStackInWorld(Level level, BlockPos pos, ItemStack wanted, double range, InvFilter filter) {
        int total = 0;
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(range))) {
            ItemStack dropped = entity.getItem();
            if (!dropped.isEmpty() && areItemStacksEqual(wanted, dropped, filter)) {
                total += dropped.getCount();
            }
        }
        return total;
    }

    /**
     * Drops a copy of the stack at the entity's horizontal position and at its feet plus half its eye height. Does nothing on the
     * client or for an empty stack.
     *
     * @param level  the level
     * @param stack  the stack to drop; not modified
     * @param entity the entity to drop at
     */
    public static void dropItemAtEntity(Level level, ItemStack stack, Entity entity) {
        if (level.isClientSide() || stack.isEmpty()) {
            return;
        }
        level.addFreshEntity(new ItemEntity(level, entity.getX(), entity.getY() + entity.getEyeHeight() * EYE_FRACTION, entity.getZ(), stack.copy()));
    }

    /**
     * @param level the level
     * @param pos   the origin position
     * @param stack the stack to find
     * @return whether the neighbours other than the one above together hold at least the stack's count under {@link #BASE_TAGS}
     */
    public static boolean checkAdjacentChests(Level level, BlockPos pos, ItemStack stack) {
        int available = Direction.stream().filter(direction -> direction != Direction.UP)
                .mapToInt(direction -> countTotalItemsIn(level, pos.relative(direction), direction.getOpposite(), stack, BASE_TAGS)).sum();
        return available >= stack.getCount();
    }

    /**
     * Removes the stack's count from the neighbours other than the one above, in direction order. A partial removal is not undone
     * when the pool runs out.
     *
     * @param level the level
     * @param pos   the origin position
     * @param stack the stack to remove; not modified
     * @return whether the whole count was removed
     */
    public static boolean consumeFromAdjacentChests(Level level, BlockPos pos, ItemStack stack) {
        List<Removal> plan = new ArrayList<>();
        boolean covered = planAdjacentRemovals(level, pos, stack, plan) <= 0;
        applyPlan(plan);
        return covered;
    }

    /**
     * Counts the main inventory only, not armour and not the off-hand.
     *
     * @param player the player
     * @param wanted the wanted stack
     * @param tags   whether to match by shared item tags
     * @return true when matching held stacks together hold at least the wanted count; false for an empty wanted stack
     */
    public static boolean isPlayerCarryingAmount(Player player, ItemStack wanted, boolean tags) {
        if (wanted.isEmpty()) {
            return false;
        }
        InvFilter filter = playerFilter(wanted, tags);
        List<ItemStack> inventory = player.getInventory().getNonEquipmentItems();
        int total = 0;
        for (int slot : MAIN_INVENTORY_SLOTS) {
            ItemStack held = inventory.get(slot);
            if (!held.isEmpty() && areItemStacksEqual(held, wanted, filter)) {
                total += held.getCount();
            }
        }
        return total >= wanted.getCount();
    }

    /**
     * Removes the wanted count from matching main-inventory stacks in slot order. With the check skipped a false result can leave a
     * partial removal in place. No sync call is made; container synchronisation carries the change.
     *
     * @param player    the player
     * @param wanted    the wanted stack
     * @param skipCheck whether to skip the carrying check that otherwise runs first
     * @param tags      whether to match by shared item tags
     * @return whether the whole count was removed
     */
    public static boolean consumePlayerItem(Player player, ItemStack wanted, boolean skipCheck, boolean tags) {
        if (!skipCheck && !isPlayerCarryingAmount(player, wanted, tags)) {
            return false;
        }
        InvFilter filter = playerFilter(wanted, tags);
        List<ItemStack> inventory = player.getInventory().getNonEquipmentItems();
        int remaining = wanted.getCount();
        for (int slot : MAIN_INVENTORY_SLOTS) {
            if (remaining <= 0) {
                break;
            }
            ItemStack held = inventory.get(slot);
            if (held.isEmpty() || !areItemStacksEqual(held, wanted, filter)) {
                continue;
            }
            int taken = Math.min(remaining, held.getCount());
            if (taken >= held.getCount()) {
                inventory.set(slot, ItemStack.EMPTY);
            } else {
                held.shrink(taken);
            }
            remaining -= taken;
        }
        return remaining <= 0;
    }

    /**
     * Succeeds only when each stack, judged alone, is fully available in the adjacent containers or fully in the player's inventory
     * with tags on. Unless simulating, each stack is then consumed from the containers, and from the player when the containers fall
     * short; a shortfall may already have removed part of the stack from the containers.
     *
     * @param level    the level
     * @param pos      the origin position
     * @param player   the player
     * @param simulate whether to only report availability
     * @param stacks   the stacks to consume
     * @return false, with nothing touched, when any stack is unavailable; otherwise true
     */
    public static boolean consumeItemsFromAdjacentInventoryOrPlayer(Level level, BlockPos pos, Player player, boolean simulate, ItemStack... stacks) {
        for (ItemStack stack : stacks) {
            if (!checkAdjacentChests(level, pos, stack) && !isPlayerCarryingAmount(player, stack, true)) {
                return false;
            }
        }
        if (!simulate) {
            for (ItemStack stack : stacks) {
                List<Removal> plan = new ArrayList<>();
                int uncovered = planAdjacentRemovals(level, pos, stack, plan);
                if (uncovered > 0) {
                    plan.add(new PlayerRemoval(player, stack));
                }
                applyPlan(plan);
            }
        }
        return true;
    }

    private static InvFilter[] buildPlayerFilters() {
        InvFilter[] filters = new InvFilter[PLAYER_FILTER_VARIANTS];
        for (int variant = 0; variant < PLAYER_FILTER_VARIANTS; variant++) {
            boolean ignoreComponents = (variant & PLAYER_FILTER_COMPONENTS_BIT) != 0;
            boolean tags = (variant & PLAYER_FILTER_TAGS_BIT) != 0;
            filters[variant] = new InvFilter(false, ignoreComponents, tags, false).setRelaxedComponents();
        }
        return filters;
    }

    private static InvFilter playerFilter(ItemStack wanted, boolean tags) {
        int variant = (wanted.isComponentsPatchEmpty() ? PLAYER_FILTER_COMPONENTS_BIT : 0) | (tags ? PLAYER_FILTER_TAGS_BIT : 0);
        return PLAYER_FILTERS[variant];
    }

    private static int planAdjacentRemovals(Level level, BlockPos pos, ItemStack stack, List<Removal> plan) {
        int remaining = stack.getCount();
        for (Direction direction : Direction.values()) {
            if (remaining <= 0) {
                break;
            }
            if (direction == Direction.UP) {
                continue;
            }
            ResourceHandler<ItemResource> handler = getItemHandlerAt(level, pos.relative(direction), direction.getOpposite());
            int available = removeStackFrom(handler, stack.copyWithCount(remaining), BASE_TAGS, true).getCount();
            if (available > 0) {
                plan.add(new HandlerRemoval(handler, stack.copyWithCount(available)));
                remaining -= available;
            }
        }
        return remaining;
    }

    private static void applyPlan(List<Removal> plan) {
        for (Removal removal : plan) {
            removal.apply();
        }
    }

    private static boolean sharesItemOrTag(ItemStack first, ItemStack second) {
        return first.getItem() == second.getItem() || first.typeHolder().tags().anyMatch(second::is);
    }

    private static int matchingAmount(ResourceHandler<ItemResource> handler, int slot, ItemStack wanted, InvFilter filter) {
        ItemResource resource = handler.getResource(slot);
        if (resource.isEmpty() || !areItemStacksEqual(wanted, resource.toStack(), filter)) {
            return 0;
        }
        return handler.getAmountAsInt(slot);
    }

    private static boolean sameItemAndData(ItemStack first, ItemStack second, InvFilter filter) {
        if (first.getItem() != second.getItem()) {
            return false;
        }
        boolean ignoreDamage = filter.has(InvFilter.IGNORE_DAMAGE);
        if (!ignoreDamage && first.getDamageValue() != second.getDamageValue()) {
            return false;
        }
        if (filter.has(InvFilter.IGNORE_COMPONENTS)) {
            return true;
        }
        if (filter.has(InvFilter.RELAXED)) {
            return isSubset(first.getComponents(), second.getComponents());
        }
        if (ignoreDamage) {
            return sameComponentsExceptDamage(first.getComponents(), second.getComponents());
        }
        return ItemStack.isSameItemSameComponents(first, second);
    }

    private static ItemStack copyOrEmpty(ItemStack stack, int count) {
        return count > 0 ? stack.copyWithCount(count) : ItemStack.EMPTY;
    }

    private static int acceptableCount(Level level, BlockPos pos, Direction side, ItemStack stack) {
        return stack.getCount() - insertStackAt(level, pos, side, stack, true).getCount();
    }

    private static String namespaceOf(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
    }

    private static boolean isSubset(DataComponentMap subset, DataComponentMap superset) {
        return subset.stream().allMatch(component -> Objects.equals(component.value(), superset.get(component.type())));
    }

    private static boolean sameComponentsExceptDamage(DataComponentMap first, DataComponentMap second) {
        DataComponentMap left = first.filter(InvHelper::isNotDamage);
        DataComponentMap right = second.filter(InvHelper::isNotDamage);
        return left.keySet().equals(right.keySet()) && isSubset(left, right);
    }

    private static boolean isNotDamage(DataComponentType<?> type) {
        return type != DataComponents.DAMAGE;
    }

    private interface Removal {
        void apply();
    }

    private record HandlerRemoval(ResourceHandler<ItemResource> handler, ItemStack stack) implements Removal {
        @Override
        public void apply() {
            removeStackFrom(handler, stack, BASE_TAGS, false);
        }
    }

    private record PlayerRemoval(Player player, ItemStack stack) implements Removal {
        @Override
        public void apply() {
            consumePlayerItem(player, stack, true, true);
        }
    }

    private record EjectPlacement(double x, double y, double z, Vec3 velocity) {
        static EjectPlacement compute(Level level, BlockPos pos, Direction side) {
            BlockPos neighbor = pos.relative(side);
            BlockPos base = level.getBlockState(neighbor).isCollisionShapeFullBlock(level, neighbor) ? pos.relative(side.getOpposite()) : pos;
            Vec3 velocity = new Vec3(side.getStepX() * EJECT_SPEED, side.getStepY() * EJECT_SPEED, side.getStepZ() * EJECT_SPEED);
            return new EjectPlacement(base.getX() + CELL_CENTER + side.getStepX(), base.getY() + side.getStepY(), base.getZ() + CELL_CENTER + side.getStepZ(), velocity);
        }

        ItemEntity createEntity(Level level, ItemStack stack) {
            ItemEntity entity = new ItemEntity(level, x, y, z, stack);
            entity.setDeltaMovement(velocity);
            return entity;
        }
    }

    private record EjectHandoff(boolean inventoryPresent, ItemStack leftover) {
        static EjectHandoff perform(Level level, BlockPos pos, Direction side, ItemStack stack) {
            ResourceHandler<ItemResource> handler = getItemHandlerAt(level, pos.relative(side), side.getOpposite());
            return new EjectHandoff(handler != null, insertStack(handler, stack, false));
        }

        boolean returnsLeftover(boolean smart) {
            return smart && inventoryPresent;
        }
    }

    private static final class GhostFilter {
        private final List<ItemStack> ghosts = new ArrayList<>();
        private final List<Integer> positions = new ArrayList<>();
        private final InvFilter filter;

        private GhostFilter(List<ItemStack> source, InvFilter filter) {
            this.filter = filter;
            for (int index = 0; index < source.size(); index++) {
                ItemStack ghost = source.get(index);
                if (!ghost.isEmpty()) {
                    ghosts.add(ghost);
                    positions.add(index);
                }
            }
        }

        private int firstMatch(ItemStack candidate) {
            int entry = 0;
            while (entry < ghosts.size() && !areItemStacksEqual(ghosts.get(entry), candidate, filter)) {
                entry++;
            }
            return entry < ghosts.size() ? positions.get(entry) : NO_MATCH;
        }

        private boolean passes(boolean blacklist, ItemStack candidate) {
            return !candidate.isEmpty() && (firstMatch(candidate) == NO_MATCH) == blacklist;
        }
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
     * Comparison flags for {@link #areItemStacksEqual}. Counts never take part. The relaxed switch is off by default and, once set,
     * is never cleared.
     *
     * @since 1.0.0
     */
    public static final class InvFilter {
        /** Compares item, damage and components exactly. */
        public static final InvFilter STRICT = new InvFilter(false, false, false, false);

        private static final int IGNORE_DAMAGE = 1;
        private static final int IGNORE_COMPONENTS = 1 << 1;
        private static final int BY_TAGS = 1 << 2;
        private static final int BY_MOD = 1 << 3;
        private static final int RELAXED = 1 << 4;

        private int flags;

        /**
         * @param ignoreDamage     whether differing damage values still match
         * @param ignoreComponents whether differing components still match
         * @param byTags           whether items sharing an item tag match
         * @param byMod            whether items from the same namespace match
         */
        public InvFilter(boolean ignoreDamage, boolean ignoreComponents, boolean byTags, boolean byMod) {
            flags = bit(byMod, BY_MOD) | bit(byTags, BY_TAGS) | bit(ignoreComponents, IGNORE_COMPONENTS) | bit(ignoreDamage, IGNORE_DAMAGE);
        }

        /**
         * Lets the first stack's components be a subset of the second stack's. Callers must not apply this to {@link #STRICT}.
         *
         * @return this filter
         */
        public InvFilter setRelaxedComponents() {
            flags |= RELAXED;
            return this;
        }

        private boolean has(int bit) {
            return (flags & bit) != 0;
        }

        private static int bit(boolean enabled, int bit) {
            return enabled ? bit : 0;
        }
    }
}
