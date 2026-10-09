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
 * A queued request to bring an item stack to a seal, a block face or an entity.
 *
 * <p>Requests are created through {@link GolemHelper}. They live on the server only, are never saved or synchronised, and
 * must be used from the server thread. All timing follows the level's game time.
 *
 * <p>Two requests are equal when their batch numbers, stack counts, items with data components and targets match. Expiry,
 * links and the spent flag do not take part.
 *
 * @since 1.0.0
 */
public final class ProvisionRequest {
    private static final long INITIAL_LIFETIME_TICKS = 200L;
    private static final long EXTENDED_LIFETIME_TICKS = 2400L;

    private final Level level;
    private final ItemStack stack;
    private Target target;
    private @Nullable Task linkedTask;
    private long expiresAt;
    private int batch;
    private int legacyId;
    private boolean spent;

    ProvisionRequest(Level level, ISealEntity seal, ItemStack stack) {
        this(level, stack, new Target(seal, null, null, null));
    }

    ProvisionRequest(Level level, BlockPos pos, Direction side, ItemStack stack) {
        this(level, stack, new Target(null, pos.immutable(), side, null));
    }

    ProvisionRequest(Level level, Entity entity, ItemStack stack) {
        this(level, stack, new Target(null, null, null, entity));
    }

    private ProvisionRequest(Level level, ItemStack stack, Target target) {
        this.target = target;
        this.stack = stack.copy();
        this.level = level;
        this.expiresAt = deadline(INITIAL_LIFETIME_TICKS);
    }

    ProvisionRequest withBatch(int number) {
        batch = number;
        return this;
    }

    private long deadline(long lifetimeTicks) {
        return level.getGameTime() + lifetimeTicks;
    }

    /**
     * @return the game time after which the request leaves the queue
     */
    public long expiresAt() {
        return expiresAt;
    }

    /**
     * @return the game time after which the request leaves the queue
     * @deprecated use {@link #expiresAt()}
     */
    @Deprecated
    public long getTimeout() {
        return expiresAt;
    }

    /**
     * Moves the expiry to two minutes after the current game time.
     */
    public void extendTimeout() {
        expiresAt = deadline(EXTENDED_LIFETIME_TICKS);
    }

    /**
     * @return a number the mod never reads
     * @deprecated carries no meaning
     */
    @Deprecated
    public int getId() {
        return legacyId;
    }

    /**
     * @param id a number the mod never reads
     * @deprecated carries no meaning
     */
    @Deprecated
    public void setId(int value) {
        legacyId = value;
    }

    /**
     * @return the batch number, which is part of the request's identity
     */
    public int batch() {
        return batch;
    }

    /**
     * @param batch the new batch number
     * @deprecated pass the batch number to {@link GolemHelper#requestProvisioning}
     */
    @Deprecated
    public void setBatch(int value) {
        batch = value;
    }

    /**
     * @param batch the new batch number
     * @deprecated pass the batch number to {@link GolemHelper#requestProvisioning}
     */
    @Deprecated
    public void setUI(int batch) {
        setBatch(batch);
    }

    /**
     * @return the seal the stack goes to, or null
     */
    public @Nullable ISealEntity getSeal() {
        return target.seal();
    }

    /**
     * @return the entity the stack goes to, or null
     */
    public @Nullable Entity getEntity() {
        return target.entity();
    }

    /**
     * @return the requested stack; a copy owned by this request
     */
    public ItemStack getStack() {
        return stack;
    }

    /**
     * @return the block the stack goes to, or null
     */
    public @Nullable BlockPos getPos() {
        return target.pos();
    }

    /**
     * @param pos the new target block, or null
     * @deprecated request a new target through {@link GolemHelper}
     */
    @Deprecated
    public void setPos(@Nullable BlockPos newPos) {
        target = target.withPos(newPos);
    }

    /**
     * @return the face of the target block the stack is inserted through, or null
     */
    public @Nullable Direction getSide() {
        return target.side();
    }

    /**
     * @param side the new target face, or null
     * @deprecated request a new target through {@link GolemHelper}
     */
    @Deprecated
    public void setSide(@Nullable Direction newSide) {
        target = target.withSide(newSide);
    }

    /**
     * @return the task serving this request, or null
     */
    public @Nullable Task getLinkedTask() {
        return linkedTask;
    }

    /**
     * Records the task as serving this request, tells the task, and extends the timeout.
     *
     * @param task the serving task
     */
    public void link(Task task) {
        linkedTask = task;
        task.linkProvision(this);
        extendTimeout();
    }

    /**
     * Releases the serving task, if any, so the request can be claimed again, and extends the timeout.
     */
    public void unlink() {
        Task served = linkedTask;
        if (served != null && served.linkedProvision() == this) {
            served.linkProvision(null);
        }
        linkedTask = null;
        extendTimeout();
    }

    /**
     * Sets only this request's pointer to the serving task and extends the timeout.
     *
     * @param task the serving task, or null
     * @deprecated use {@link #link(Task)}
     */
    @Deprecated
    public void setLinkedTask(@Nullable Task task) {
        linkedTask = task;
        extendTimeout();
    }

    /**
     * @return whether the stack has been delivered and the request awaits removal
     */
    public boolean isSpent() {
        return spent;
    }

    /**
     * Marks the stack as delivered.
     */
    public void markSpent() {
        spent = true;
    }

    /**
     * @return whether the request is spent
     * @deprecated use {@link #isSpent()}
     */
    @Deprecated
    public boolean isInvalid() {
        return isSpent();
    }

    /**
     * @param invalid true marks the request spent, false clears the flag
     * @deprecated use {@link #markSpent()}
     */
    @Deprecated
    public void setInvalid(boolean invalid) {
        if (invalid) {
            markSpent();
        } else {
            spent = false;
        }
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof ProvisionRequest request && batch == request.batch && stack.getCount() == request.stack.getCount() && target.matches(request.target)
                && ItemStack.isSameItemSameComponents(stack, request.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target.identityHash(), target.side(), batch, stack.getCount(), ItemStack.hashItemAndComponents(stack));
    }

    private record Target(@Nullable ISealEntity seal, @Nullable BlockPos pos, @Nullable Direction side, @Nullable Entity entity) {
        Target withPos(@Nullable BlockPos newPos) {
            return new Target(seal, newPos, side, entity);
        }

        Target withSide(@Nullable Direction newSide) {
            return new Target(seal, pos, newSide, entity);
        }

        boolean matches(Target other) {
            if (seal != null || other.seal != null) {
                return seal != null && other.seal != null && seal.pos().equals(other.seal.pos());
            }
            if (entity != null || other.entity != null) {
                return entity == other.entity;
            }
            return side == other.side && Objects.equals(pos, other.pos);
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
