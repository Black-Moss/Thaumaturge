package com.leclowndu93150.thaumaturge.api.golems;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Static facade for handing work to golems: seal lookup, posting tasks on a level's task board, and supply orders.
 *
 * <p>The mod binds the implementation once during construction; calls made before that throw
 * {@link IllegalStateException}. Posting tasks and placing supply orders are server-side and must run on the server thread;
 * on a client they do nothing. Seal type lookups work anywhere, and {@link #sealAt} also answers on a client from the copy
 * of nearby seals the server sends.
 *
 * <p>Each level has one task board and one supply queue. Neither is saved: after a restart both start empty and seals post
 * fresh work as they tick.
 *
 * @since 1.1.0
 */
public final class GolemDispatch {
    private static final int SUPPLY_QUEUE_LIMIT = 1000;
    private static final int NO_BATCH = 0;
    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("GolemDispatch");

    private GolemDispatch() {}

    /**
     * Installs the implementation. Called once by the mod.
     *
     * @param bindings the implementation
     * @throws IllegalStateException when an implementation is already bound
     * @since 1.1.0
     */
    public static void bind(Bindings bindings) {
        BINDING.bind(bindings);
    }

    /**
     * @param id a seal type id
     * @return the registered seal type, or empty for an unknown id
     * @since 1.1.0
     */
    public static Optional<SealType> sealType(Identifier id) {
        return BINDING.get().sealType(id);
    }

    /**
     * @param id a seal type id
     * @return a fresh stack of one item that places a seal of the type, or {@link ItemStack#EMPTY} for an unknown id
     * @since 1.1.0
     */
    public static ItemStack sealItem(Identifier id) {
        return sealType(id).map(type -> new ItemStack(type.placer())).orElse(ItemStack.EMPTY);
    }

    /**
     * Finds the seal placed on a block face.
     *
     * @param level the level
     * @param pos   the block and face
     * @return the seal, or empty when there is none or the position is not loaded
     * @since 1.1.0
     */
    public static Optional<ISealEntity> sealAt(Level level, SealPos pos) {
        return Optional.ofNullable(BINDING.get().sealAt(level, pos));
    }

    /**
     * Posts a task on the level's board, which gives it a fresh id. The task's life, priority and seal-owned data should
     * be set before posting. Does nothing on a client.
     *
     * @param level the level
     * @param task  the task; must not have been posted before
     * @since 1.1.0
     */
    public static void postTask(Level level, Task task) {
        if (!level.isClientSide()) {
            BINDING.get().postTask(level, task);
        }
    }

    /**
     * Orders a stack into the hands of a golem working for the seal. Provide seals in range fetch it and the golem that
     * collects it keeps it for the seal's work.
     *
     * @param level the level
     * @param seal  the seal whose golems need the stack
     * @param stack the stack wanted; copied
     * @return the queued order, which is an equal order that was already queued when there is one; empty on a client
     * @since 1.1.0
     */
    public static Optional<SupplyOrder> orderSupply(Level level, ISealEntity seal, ItemStack stack) {
        return enqueue(level, SupplyOrder.toSeal(seal), stack, NO_BATCH);
    }

    /**
     * Orders a stack delivered to a block face, with batch number 0.
     *
     * @param level the level
     * @param pos   the block to deliver to; items go into its inventory through {@code face}, or are dropped in front of
     *              that face when it has none
     * @param face  the face to deliver through
     * @param stack the stack wanted; copied
     * @return the queued order, which is an equal order that was already queued when there is one; empty on a client
     * @since 1.1.0
     */
    public static Optional<SupplyOrder> orderSupply(Level level, BlockPos pos, Direction face, ItemStack stack) {
        return orderSupply(level, pos, face, stack, NO_BATCH);
    }

    /**
     * Orders a stack delivered to a block face. Give each part of a large order its own batch number so identical stacks
     * are not folded together.
     *
     * @param level the level
     * @param pos   the block to deliver to
     * @param face  the face to deliver through
     * @param stack the stack wanted; copied
     * @param batch the batch number
     * @return the queued order, which is an equal order that was already queued when there is one; empty on a client
     * @since 1.1.0
     */
    public static Optional<SupplyOrder> orderSupply(Level level, BlockPos pos, Direction face, ItemStack stack, int batch) {
        return enqueue(level, SupplyOrder.toBlock(pos, face), stack, batch);
    }

    /**
     * Orders a stack dropped at an entity's feet, with batch number 0.
     *
     * @param level  the level
     * @param entity the recipient, normally a player
     * @param stack  the stack wanted; copied
     * @return the queued order, which is an equal order that was already queued when there is one; empty on a client
     * @since 1.1.0
     */
    public static Optional<SupplyOrder> orderSupply(Level level, Entity entity, ItemStack stack) {
        return orderSupply(level, entity, stack, NO_BATCH);
    }

    /**
     * Orders a stack dropped at an entity's feet. Give each part of a large order its own batch number so identical stacks
     * are not folded together.
     *
     * @param level  the level
     * @param entity the recipient, normally a player
     * @param stack  the stack wanted; copied
     * @param batch  the batch number
     * @return the queued order, which is an equal order that was already queued when there is one; empty on a client
     * @since 1.1.0
     */
    public static Optional<SupplyOrder> orderSupply(Level level, Entity entity, ItemStack stack, int batch) {
        return enqueue(level, SupplyOrder.toEntity(entity), stack, batch);
    }

    /**
     * The level's supply queue, oldest first. Provide seals serve it in this order and tidy it of spent and expired orders.
     * When an order would take the queue past 1000 entries, the oldest are dropped first.
     *
     * @param level the level
     * @return the live queue, not a copy; it is safe to iterate while it changes and supports removal
     * @apiNote add orders through {@link #orderSupply} rather than to this list, so duplicates fold and the limit holds
     * @since 1.1.0
     */
    public static List<SupplyOrder> supplyQueue(Level level) {
        return BINDING.get().supplyQueue(level);
    }

    @SuppressWarnings("removal")
    private static Optional<SupplyOrder> enqueue(Level level, SupplyOrder.Destination destination, ItemStack stack, int batch) {
        if (level.isClientSide()) {
            return Optional.empty();
        }
        SupplyOrder order = new ProvisionRequest(level, stack, destination, batch);
        List<SupplyOrder> queue = supplyQueue(level);
        for (SupplyOrder queued : queue) {
            if (queued.equals(order)) {
                return Optional.of(queued);
            }
        }
        int overflow = queue.size() - SUPPLY_QUEUE_LIMIT + 1;
        for (int i = 0; i < overflow && !queue.isEmpty(); i++) {
            queue.remove(0);
        }
        queue.add(order);
        return Optional.of(order);
    }

    /**
     * The implementation behind {@link GolemDispatch}, bound by the mod once.
     *
     * @since 1.1.0
     */
    public interface Bindings {
        /**
         * @param id a seal type id
         * @return the seal type, or empty
         * @since 1.1.0
         */
        Optional<SealType> sealType(Identifier id);

        /**
         * @param level the level
         * @param pos   the placement
         * @return the seal, or null when there is none
         * @since 1.1.0
         */
        @Nullable
        ISealEntity sealAt(Level level, SealPos pos);

        /**
         * @param level the server level
         * @param task  the task to post
         * @since 1.1.0
         */
        void postTask(Level level, Task task);

        /**
         * @param level the level
         * @return the level's live supply queue
         * @since 1.1.0
         */
        List<SupplyOrder> supplyQueue(Level level);
    }
}
