package com.leclowndu93150.thaumaturge.api.golems;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * A queued request to bring an item stack to a seal, a block face or an entity.
 *
 * <p>Every {@link SupplyOrder} the mod creates is also a {@code ProvisionRequest}, so code written against 1.0 keeps seeing
 * the same objects through {@link GolemHelper#getProvisionRequests} and {@link Task#linkedProvision()}.
 *
 * @since 1.0.0
 * @deprecated use {@link SupplyOrder}, placed through {@link GolemDispatch}; this type is removed in 2.0.0
 */
@Deprecated(since = "1.1.0", forRemoval = true)
public final class ProvisionRequest extends SupplyOrder {
    private int legacyId;

    ProvisionRequest(Level level, ItemStack stack, Destination destination, int batch) {
        super(level, stack, destination, batch);
    }

    /**
     * @return the game time after which the request leaves the queue
     * @deprecated use {@link #expiresAt()}
     */
    @Deprecated
    public long getTimeout() {
        return expiresAt();
    }

    /**
     * Moves the expiry to two minutes after the current game time.
     *
     * @deprecated the expiry moves by itself on {@link #assign(Task)} and {@link #release()}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public void extendTimeout() {
        prolong();
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
     * @param value a number the mod never reads
     * @deprecated carries no meaning
     */
    @Deprecated
    public void setId(int value) {
        legacyId = value;
    }

    /**
     * @param value the new batch number
     * @deprecated pass the batch number to {@link GolemDispatch#orderSupply}
     */
    @Deprecated
    public void setBatch(int value) {
        rebatch(value);
    }

    /**
     * @param batch the new batch number
     * @deprecated pass the batch number to {@link GolemDispatch#orderSupply}
     */
    @Deprecated
    public void setUI(int batch) {
        rebatch(batch);
    }

    /**
     * @return the seal the stack goes to, or null
     * @deprecated use {@link #requestingSeal()}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public @Nullable ISealEntity getSeal() {
        return requestingSeal();
    }

    /**
     * @return the entity the stack goes to, or null
     * @deprecated use {@link #recipient()}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public @Nullable Entity getEntity() {
        return recipient();
    }

    /**
     * @return the requested stack; a copy owned by this request
     * @deprecated use {@link #wanted()}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public ItemStack getStack() {
        return wanted();
    }

    /**
     * @return the block the stack goes to, or null
     * @deprecated use {@link #destinationBlock()}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public @Nullable BlockPos getPos() {
        return destinationBlock();
    }

    /**
     * @param newPos the new target block, or null
     * @deprecated place a new order through {@link GolemDispatch}
     */
    @Deprecated
    public void setPos(@Nullable BlockPos newPos) {
        moveBlock(newPos);
    }

    /**
     * @return the face of the target block the stack is inserted through, or null
     * @deprecated use {@link #destinationFace()}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public @Nullable Direction getSide() {
        return destinationFace();
    }

    /**
     * @param newSide the new target face, or null
     * @deprecated place a new order through {@link GolemDispatch}
     */
    @Deprecated
    public void setSide(@Nullable Direction newSide) {
        moveFace(newSide);
    }

    /**
     * @return the task serving this request, or null
     * @deprecated use {@link #servingTask()}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public @Nullable Task getLinkedTask() {
        return servingTask();
    }

    /**
     * Records the task as serving this request, tells the task, and extends the timeout.
     *
     * @param task the serving task
     * @deprecated use {@link #assign(Task)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public void link(Task task) {
        assign(task);
    }

    /**
     * Releases the serving task, if any, so the request can be claimed again, and extends the timeout.
     *
     * @deprecated use {@link #release()}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public void unlink() {
        release();
    }

    /**
     * Sets only this request's pointer to the serving task and extends the timeout.
     *
     * @param task the serving task, or null
     * @deprecated use {@link #assign(Task)}
     */
    @Deprecated
    public void setLinkedTask(@Nullable Task task) {
        pointServingTask(task);
        prolong();
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
            clearSpent();
        }
    }
}
