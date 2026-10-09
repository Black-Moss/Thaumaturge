package com.leclowndu93150.thaumaturge.api.golems.tasks;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.ProvisionRequest;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

/**
 * One unit of golem work on a level's task board.
 *
 * <p>Tasks are created with the static factories and posted through {@link GolemHelper#addGolemTask}, which gives them
 * their id. They live on the server only, are never saved or synchronised, and must be used from the server thread. Life
 * counts board sweeps, one per second.
 *
 * <p>Equality uses the board-assigned id alone, so tasks that were never posted (id 0) are equal to each other.
 *
 * @since 1.0.0
 */
public final class Task {
    private static final int INITIAL_LIFE = 300;
    private static final int CLAIM_LIFE_BONUS = 120;
    private static final int ATTEMPT_LIFE_BONUS = 1;

    private static final byte NO_COLOR = 0;

    private final TaskTarget target;
    private final @Nullable SealPos origin;
    private int remainingLife = INITIAL_LIFE;
    private int boardId;
    private int sealOwnedValue;
    private byte rank;
    private final Set<Status> status = EnumSet.noneOf(Status.class);
    private @Nullable UUID reservedFor;
    private @Nullable ProvisionRequest servedRequest;

    private Task(TaskTarget target, @Nullable SealPos origin) {
        this.target = target;
        this.origin = origin;
    }

    /**
     * @param origin the placement of the posting seal, or null for free-standing work that counts as finished when a golem attempts it
     * @param pos    the block to work on
     * @return a new task aimed at the block
     */
    public static Task atBlock(@Nullable SealPos origin, BlockPos pos) {
        return new Task(new BlockTarget(pos.immutable()), origin);
    }

    /**
     * @param origin the placement of the posting seal, or null for free-standing work that counts as finished when a golem attempts it
     * @param entity the entity to work on
     * @return a new task aimed at the entity
     */
    public static Task onEntity(@Nullable SealPos origin, Entity entity) {
        return new Task(new EntityTarget(entity), origin);
    }

    /**
     * @return the placement of the posting seal, or null
     */
    public @Nullable SealPos origin() {
        return origin;
    }

    /**
     * @return what the task points at
     */
    public TaskTarget target() {
        return target;
    }

    /**
     * @return the block the golem walks to; for an entity task, the block the entity stands in
     */
    public BlockPos pos() {
        return target.pos();
    }

    /**
     * @return the target entity, or null for a block task
     */
    public @Nullable Entity entity() {
        return switch (target) {
            case EntityTarget entityTarget -> entityTarget.entity();
            case BlockTarget ignored -> null;
        };
    }

    /**
     * @return whether the task is aimed at an entity
     */
    public boolean isEntityTask() {
        return entity() != null;
    }

    /**
     * @return the board-assigned id, or 0 before the task is posted
     */
    public int id() {
        return boardId;
    }

    /**
     * Stores the id handed out by the task board. Called when the task is posted.
     *
     * @param id the id to store
     */
    public void assignId(int id) {
        boardId = id;
    }

    /**
     * @return the unique id of the only golem allowed to take this task, or null when it is open to every golem
     */
    public @Nullable UUID assignedGolem() {
        return reservedFor;
    }

    /**
     * @param golem the unique id of the golem the task is reserved for, or null
     */
    public void assignTo(@Nullable UUID golem) {
        reservedFor = golem;
    }

    /**
     * @return the assigned golem
     * @deprecated use {@link #assignedGolem()}
     */
    @Deprecated
    public @Nullable UUID claimant() {
        return assignedGolem();
    }

    /**
     * @param claimant the assigned golem
     * @deprecated use {@link #assignTo(UUID)}
     */
    @Deprecated
    public void setClaimant(@Nullable UUID claimant) {
        assignTo(claimant);
    }

    /**
     * @return the priority, normally copied from the seal, from -5 to 5
     */
    public byte priority() {
        return rank;
    }

    /**
     * @param priority the new priority
     */
    public void setPriority(byte priority) {
        rank = priority;
    }

    /**
     * @return the remaining sweeps before the board removes the task
     */
    public int life() {
        return remainingLife;
    }

    /**
     * @param life the new life
     */
    public void setLife(int life) {
        remainingLife = life;
    }

    /**
     * @return the life, capped at the largest 16-bit signed value
     * @deprecated use {@link #life()}
     */
    @Deprecated
    public short lifespan() {
        return (short) Math.min(remainingLife, Short.MAX_VALUE);
    }

    /**
     * @param lifespan the new life
     * @deprecated use {@link #setLife(int)}
     */
    @Deprecated
    public void setLifespan(short lifespan) {
        remainingLife = lifespan;
    }

    /**
     * @return the integer the posting seal keeps on the task for its own state; the task never interprets it
     */
    public int data() {
        return sealOwnedValue;
    }

    /**
     * @param data the new seal-owned integer
     */
    public void setData(int data) {
        sealOwnedValue = data;
    }

    /**
     * @return whether a golem has claimed the task
     */
    public boolean isClaimed() {
        return status.contains(Status.CLAIMED);
    }

    /**
     * Marks the task as claimed and adds 120 life. Calling it twice adds twice.
     */
    public void claim() {
        status.add(Status.CLAIMED);
        addLife(CLAIM_LIFE_BONUS);
    }

    /**
     * Clears the claim and adds 120 life.
     */
    public void release() {
        status.remove(Status.CLAIMED);
        addLife(CLAIM_LIFE_BONUS);
    }

    /**
     * @return whether the task is claimed
     * @deprecated use {@link #isClaimed()}
     */
    @Deprecated
    public boolean isReserved() {
        return isClaimed();
    }

    /**
     * @param reserved true behaves as {@link #claim()}, false as {@link #release()}
     * @deprecated use {@link #claim()} or {@link #release()}
     */
    @Deprecated
    public void setReserved(boolean reserved) {
        if (reserved) {
            claim();
        } else {
            release();
        }
    }

    /**
     * @return whether the task has ended
     */
    public boolean isEnded() {
        return status.contains(Status.ENDED);
    }

    /**
     * Ends the task for good and clears its link to a provisioning request. The board drops it at the next sweep.
     */
    public void end() {
        servedRequest = null;
        status.add(Status.ENDED);
    }

    /**
     * @return whether the task has ended
     * @deprecated use {@link #isEnded()}
     */
    @Deprecated
    public boolean isSuspended() {
        return isEnded();
    }

    /**
     * Ends the task.
     *
     * @deprecated use {@link #end()}
     */
    @Deprecated
    public void suspend() {
        end();
    }

    /**
     * @return whether the last attempt finished the work
     */
    public boolean isCompleted() {
        return status.contains(Status.COMPLETED);
    }

    /**
     * Stores the result of a golem's attempt, replacing the previous result, and adds 1 life. The value is stored as given,
     * including for a task without a posting seal; the board's arrival attempt reports such a task as finished.
     *
     * @param finished whether the work finished
     */
    public void recordAttempt(boolean finished) {
        if (finished) {
            status.add(Status.COMPLETED);
        } else {
            status.remove(Status.COMPLETED);
        }
        addLife(ATTEMPT_LIFE_BONUS);
    }

    /**
     * @return the provisioning request this task serves, or null
     */
    public @Nullable ProvisionRequest linkedProvision() {
        return servedRequest;
    }

    /**
     * Sets only this task's pointer; {@link ProvisionRequest#link} sets both sides.
     *
     * @param request the request this task serves, or null
     */
    public void linkProvision(@Nullable ProvisionRequest request) {
        servedRequest = request;
    }

    /**
     * Checks the golem colour against the posting seal, then asks the seal's behaviour. A task without a posting seal, or whose
     * seal no longer exists, passes. The golem's own lock and trait rules are checked by the golem before this.
     *
     * @param golem the golem asking
     * @return whether the golem may perform the task
     */
    public boolean canBePerformedBy(IGolemAPI golem) {
        ISealEntity seal = origin == null ? null : GolemHelper.getSealEntity(golem.level(), origin);
        if (seal == null) {
            return true;
        }
        if (colorsClash(golem.color(), seal.color())) {
            return false;
        }
        return seal.behavior().canPerform(seal, golem, this);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof Task task && task.boardId == boardId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(boardId);
    }

    private enum Status {
        CLAIMED, ENDED, COMPLETED
    }

    private static boolean colorsClash(byte golemColor, byte sealColor) {
        return golemColor != NO_COLOR && sealColor != NO_COLOR && golemColor != sealColor;
    }

    private void addLife(int bonus) {
        remainingLife = (int) Math.min((long) remainingLife + bonus, Integer.MAX_VALUE);
    }
}
