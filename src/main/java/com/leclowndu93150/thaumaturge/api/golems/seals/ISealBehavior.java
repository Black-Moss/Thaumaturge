package com.leclowndu93150.thaumaturge.api.golems.seals;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import net.minecraft.server.level.ServerLevel;

/**
 * The job of one placed seal.
 *
 * <p>One instance exists per placement and may hold per-placement state. Every callback except
 * {@link #canPerform} runs on the server thread.
 *
 * @since 1.0.0
 */
public interface ISealBehavior {
    /**
     * Called once per server tick for each seal whose chunk is loaded and that redstone has not stopped.
     *
     * @param level the level
     * @param seal  the seal this behaviour belongs to
     */
    void tick(ServerLevel level, ISealEntity seal);

    /**
     * Called when a golem claims one of the seal's tasks, before the golem walks. Does nothing by default.
     *
     * @param level the level
     * @param seal  the seal this behaviour belongs to
     * @param golem the golem that claimed the task
     * @param task  the claimed task
     */
    default void onTaskStarted(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {}

    /**
     * Called when the golem has reached the task's target.
     *
     * @param level the level
     * @param seal  the seal this behaviour belongs to
     * @param golem the working golem
     * @param task  the task being worked
     * @return true when the task is finished; false to make the golem stay and call again a moment later
     */
    boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task);

    /**
     * Asked before a golem claims a task and while it works on it. The golem's own lock and trait rules are already checked.
     * Receives no level, so the implementation must rely only on what the golem exposes.
     *
     * @param seal  the seal this behaviour belongs to
     * @param golem the golem asking
     * @param task  the task
     * @return whether the golem may do this task
     */
    boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task);

    /**
     * Called when the task board drops one of the seal's tasks because it ended or ran out of life. Does nothing by default.
     *
     * @param level the level
     * @param seal  the seal this behaviour belongs to
     * @param task  the dropped task
     */
    default void onTaskSuspended(ServerLevel level, ISealEntity seal, Task task) {}

    /**
     * Called once when the seal is removed from the world. Does nothing by default.
     *
     * @param level the level
     * @param seal  the seal this behaviour belongs to
     */
    default void onRemoved(ServerLevel level, ISealEntity seal) {}
}
