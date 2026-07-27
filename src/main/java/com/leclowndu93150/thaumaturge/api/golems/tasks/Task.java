package com.leclowndu93150.thaumaturge.api.golems.tasks;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.ProvisionRequest;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

/**
 * A unit of golem work issued by a seal or the provisioning system. Tasks target either a
 * block position or an entity, live in the per-level task list and expire when their
 * lifespan runs out. Lifespan ticks down once per second.
 *
 * @since 1.0.0
 */
public final class Task {
    /** Task targets a block position. */
    public static final byte TYPE_BLOCK = 0;
    /** Task targets an entity. */
    public static final byte TYPE_ENTITY = 1;

    private static final short DEFAULT_LIFESPAN = 300;
    private static final short RESERVE_LIFESPAN_BONUS = 120;

    private UUID golemUUID;
    private int id;
    private final byte type;
    private final SealPos sealPos;
    private final BlockPos pos;
    private final Entity entity;
    private boolean reserved;
    private boolean suspended;
    private boolean completed;
    private int data;
    private ProvisionRequest linkedProvision;
    private short lifespan;
    private byte priority;

    /**
     * Creates a block-targeting task.
     *
     * @param sealPos the issuing seal's placement, or null for seal-less tasks
     * @param pos     the target block
     */
    public Task(@Nullable SealPos sealPos, BlockPos pos) {
        this.sealPos = sealPos;
        this.pos = pos;
        this.entity = null;
        this.type = TYPE_BLOCK;
        this.lifespan = DEFAULT_LIFESPAN;
    }

    /**
     * Creates an entity-targeting task.
     *
     * @param sealPos the issuing seal's placement, or null for seal-less tasks
     * @param entity  the target entity
     */
    public Task(@Nullable SealPos sealPos, Entity entity) {
        this.sealPos = sealPos;
        this.pos = null;
        this.entity = entity;
        this.type = TYPE_ENTITY;
        this.lifespan = DEFAULT_LIFESPAN;
    }

    public byte getPriority() {
        return priority;
    }

    public void setPriority(byte priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    /**
     * Marks the task fulfilled or unfulfilled and extends its lifespan by one tick-down.
     *
     * @param fulfilled the new completion state
     */
    public void setCompletion(boolean fulfilled) {
        this.completed = fulfilled;
        this.lifespan++;
    }

    public @Nullable UUID getGolemUUID() {
        return golemUUID;
    }

    public void setGolemUUID(@Nullable UUID golemUUID) {
        this.golemUUID = golemUUID;
    }

    /**
     * @return the target position; entity tasks report the entity's current position
     */
    public BlockPos getPos() {
        return type == TYPE_ENTITY ? entity.blockPosition() : pos;
    }

    public byte getType() {
        return type;
    }

    public @Nullable Entity getEntity() {
        return entity;
    }

    /**
     * @return the task's unique id, assigned when the task is added to the task list
     */
    public int getId() {
        return id;
    }

    /**
     * Assigns the task id. Intended for the task list implementation only.
     *
     * @param id the unique id
     */
    public void setId(int id) {
        this.id = id;
    }

    public boolean isReserved() {
        return reserved;
    }

    /**
     * Reserves or releases the task for a specific golem and extends its lifespan.
     *
     * @param reserved the new reservation state
     */
    public void setReserved(boolean reserved) {
        this.reserved = reserved;
        this.lifespan += RESERVE_LIFESPAN_BONUS;
    }

    public boolean isSuspended() {
        return suspended;
    }

    /**
     * Suspends or resumes the task. Suspending clears any linked provision request.
     *
     * @param suspended the new suspension state
     */
    public void setSuspended(boolean suspended) {
        setLinkedProvision(null);
        this.suspended = suspended;
    }

    public @Nullable SealPos getSealPos() {
        return sealPos;
    }

    public long getLifespan() {
        return lifespan;
    }

    public void setLifespan(short lifespan) {
        this.lifespan = lifespan;
    }

    /**
     * @param golem the candidate golem
     * @return whether the golem's color matches and the issuing seal accepts it
     */
    public boolean canGolemPerformTask(IGolemAPI golem) {
        ISealEntity seal = GolemHelper.getSealEntity(golem.getGolemWorld(), sealPos);
        if (seal == null) {
            return true;
        }
        if (golem.getGolemColor() > 0 && seal.getColor() > 0 && golem.getGolemColor() != seal.getColor()) {
            return false;
        }
        return seal.getSeal().canGolemPerformTask(golem, this);
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public @Nullable ProvisionRequest getLinkedProvision() {
        return linkedProvision;
    }

    /**
     * Links or clears the provision request this task fulfills. Linking extends the
     * request's timeout.
     *
     * @param linkedProvision the request, or null to clear
     */
    public void setLinkedProvision(@Nullable ProvisionRequest linkedProvision) {
        this.linkedProvision = linkedProvision;
        if (linkedProvision != null) {
            linkedProvision.extendTimeout();
        }
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Task task && task.id == id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
