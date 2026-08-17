package com.leclowndu93150.thaumaturge.api.golems.seals;

import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

/**
 * The behavior of a seal kind. A fresh instance is created per placement; per-placement configuration such as item
 * filters lives on the instance and round-trips through {@link #readCustomNBT} and {@link #writeCustomNBT}.
 *
 * @since 1.0.0
 */
public interface ISeal {
    /**
     * @return the registry id of this seal kind
     */
    Identifier getKey();

    /**
     * @param level the level
     * @param pos the block the seal would attach to
     * @param face the face it would occupy
     * @return whether the seal may exist there
     */
    boolean canPlaceAt(Level level, BlockPos pos, Direction face);

    /**
     * Runs once per level tick while the seal is placed, loaded and not redstone suppressed.
     *
     * @param level the level
     * @param seal the placed seal
     */
    void tickSeal(Level level, ISealEntity seal);

    /**
     * Called when a golem accepts a task issued by this seal.
     */
    void onTaskStarted(Level level, IGolemAPI golem, Task task);

    /**
     * Called when a golem reaches a task's destination.
     *
     * @return whether the task counts as fulfilled
     */
    boolean onTaskCompletion(Level level, IGolemAPI golem, Task task);

    /**
     * Called when a task issued by this seal is suspended or expires.
     */
    void onTaskSuspension(Level level, Task task);

    /**
     * @return whether the given golem is allowed to work the given task
     */
    boolean canGolemPerformTask(IGolemAPI golem, Task task);

    /**
     * Restores per-placement configuration.
     */
    void readCustomNBT(CompoundTag nbt);

    /**
     * Persists per-placement configuration.
     */
    void writeCustomNBT(CompoundTag nbt);

    /**
     * @return the icon texture rendered on the seal in the world
     */
    Identifier getSealIcon();

    /**
     * Called when the seal is removed from the world.
     */
    void onRemoval(Level level, BlockPos pos, Direction face);

    /**
     * @return traits a golem must have to work this seal, or an empty array
     */
    GolemTrait[] getRequiredTags();

    /**
     * @return traits that disqualify a golem from working this seal, or an empty array
     */
    GolemTrait[] getForbiddenTags();
}
