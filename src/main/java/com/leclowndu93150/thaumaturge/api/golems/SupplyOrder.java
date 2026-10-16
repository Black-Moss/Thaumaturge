package com.leclowndu93150.thaumaturge.api.golems;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * A queued order for golems to bring an item stack to a seal, a block face or an entity.
 *
 * <p>Orders are placed through {@link GolemDispatch#orderSupply} and answered by provide seals in the same level. They exist
 * on the server only, are never saved or synchronised, and must be used from the server thread. Expiry follows the level's
 * game time: an order leaves the queue ten seconds after it is placed unless a task takes it, and every
 * {@link #assign(Task) assignment} or {@link #release() release} moves its expiry to two minutes from that moment.
 *
 * <p>Exactly one destination is set: a {@link #requestingSeal() seal}, a {@link #destinationBlock() block} with its
 * {@link #destinationFace() face}, or a {@link #recipient() recipient} entity. A seal destination means a golem working for
 * that seal needs to hold the stack; nothing is put into the seal.
 *
 * <p>Two orders are equal when their destinations, wanted items with data components, stack counts and batch numbers match.
 * Expiry, the serving task and the spent flag take no part, so placing an order equal to a queued one, claimed or not, adds
 * nothing.
 *
 * <p>Addons cannot create or subclass orders directly; the constructor is package-private.
 *
 * @since 1.1.0
 */
public class SupplyOrder {
    private static final long UNCLAIMED_LIFETIME_TICKS = 200L;
    private static final long ASSIGNED_LIFETIME_TICKS = 2400L;

    private final Level level;
    private final ItemStack wanted;
    private Destination destination;
    private int batch;
    private @Nullable Task servingTask;
    private long expiresAt;
    private boolean spent;

    SupplyOrder(Level level, ItemStack wanted, Destination destination, int batch) {
        this.level = level;
        this.wanted = wanted.copy();
        this.destination = destination;
        this.batch = batch;
        this.expiresAt = level.getGameTime() + UNCLAIMED_LIFETIME_TICKS;
    }

    static Destination toSeal(ISealEntity seal) {
        return new Destination(seal, null, null, null);
    }

    static Destination toBlock(BlockPos pos, Direction face) {
        return new Destination(null, pos.immutable(), face, null);
    }

    static Destination toEntity(Entity entity) {
        return new Destination(null, null, null, entity);
    }

    /**
     * The stack the order asks for. It is a copy taken when the order was placed and owned by the order; callers must not
     * modify it.
     *
     * @return the wanted stack, with its count
     * @since 1.1.0
     */
    public ItemStack wanted() {
        return wanted;
    }

    /**
     * @return the seal whose golems need the stack, or null when the order goes to a block or an entity
     * @since 1.1.0
     */
    public @Nullable ISealEntity requestingSeal() {
        return destination.seal();
    }

    /**
     * @return the block the stack is delivered to, or null when the order goes to a seal or an entity
     * @since 1.1.0
     */
    public @Nullable BlockPos destinationBlock() {
        return destination.pos();
    }

    /**
     * @return the face of {@link #destinationBlock()} the stack is inserted through, or null when the order has no block
     *     destination
     * @since 1.1.0
     */
    public @Nullable Direction destinationFace() {
        return destination.face();
    }

    /**
     * @return the entity the stack is dropped at, or null when the order goes to a seal or a block
     * @since 1.1.0
     */
    public @Nullable Entity recipient() {
        return destination.entity();
    }

    /**
     * The batch number keeps identical stacks of one large order apart, so they are not folded together as duplicates.
     *
     * @return the batch number, 0 unless the order was placed with one
     * @since 1.1.0
     */
    public int batch() {
        return batch;
    }

    /**
     * @return the game time after which provide seals drop the order from the queue
     * @since 1.1.0
     */
    public long expiresAt() {
        return expiresAt;
    }

    /**
     * @return the task currently serving the order (its collection task, later its delivery task), or null when no task has
     *     taken it
     * @since 1.1.0
     */
    public @Nullable Task servingTask() {
        return servingTask;
    }

    /**
     * Makes the task the order's server, points the task back at the order, and moves the expiry to two minutes from now.
     * An order is served by one task at a time; a previous server is replaced without being told.
     *
     * @param task the serving task
     * @since 1.1.0
     */
    public void assign(Task task) {
        servingTask = task;
        task.serveOrder(this);
        prolong();
    }

    /**
     * Drops the serving task, so a provide seal may take the order again, and moves the expiry to two minutes from now. The
     * task's pointer is cleared only when it still points at this order.
     *
     * @since 1.1.0
     */
    public void release() {
        Task served = servingTask;
        if (served != null && served.servedOrder() == this) {
            served.serveOrder(null);
        }
        servingTask = null;
        prolong();
    }

    /**
     * @return whether the stack has been delivered; a spent order is removed at the next tidy
     * @since 1.1.0
     */
    public boolean isSpent() {
        return spent;
    }

    /**
     * Marks the stack as delivered. This cannot be undone through this type.
     *
     * @since 1.1.0
     */
    public void markSpent() {
        spent = true;
    }

    void prolong() {
        expiresAt = level.getGameTime() + ASSIGNED_LIFETIME_TICKS;
    }

    void pointServingTask(@Nullable Task task) {
        servingTask = task;
    }

    void rebatch(int number) {
        batch = number;
    }

    void moveBlock(@Nullable BlockPos pos) {
        destination = new Destination(destination.seal(), pos, destination.face(), destination.entity());
    }

    void moveFace(@Nullable Direction face) {
        destination = new Destination(destination.seal(), destination.pos(), face, destination.entity());
    }

    void clearSpent() {
        spent = false;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof SupplyOrder order && batch == order.batch && wanted.getCount() == order.wanted.getCount() && destination.sameAs(order.destination)
                && ItemStack.isSameItemSameComponents(wanted, order.wanted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destination.identityHash(), destination.face(), batch, wanted.getCount(), ItemStack.hashItemAndComponents(wanted));
    }

    record Destination(@Nullable ISealEntity seal, @Nullable BlockPos pos, @Nullable Direction face, @Nullable Entity entity) {
        boolean sameAs(Destination other) {
            if (seal != null || other.seal != null) {
                return seal != null && other.seal != null && seal.pos().equals(other.seal.pos());
            }
            if (entity != null || other.entity != null) {
                return entity == other.entity;
            }
            return face == other.face && Objects.equals(pos, other.pos);
        }

        int identityHash() {
            if (seal != null) {
                return seal.pos().hashCode();
            }
            if (entity != null) {
                return System.identityHashCode(entity);
            }
            return Objects.hashCode(pos);
        }
    }
}
