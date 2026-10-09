package com.leclowndu93150.thaumaturge.content.golem.tasks;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public final class TaskHandoff {
    private static final byte TASK_EMOTE_EVENT = 5;

    private TaskHandoff() {}

    public static void assign(EntityThaumaturgeGolem golem, Task task) {
        task.claim();
        golem.assignJob(task);
        if (ThaumaturgeCommonConfig.SHOW_GOLEM_EMOTES.get()) {
            golem.level().broadcastEntityEvent(golem, TASK_EMOTE_EVENT);
        }
    }

    public static void continueWith(ServerLevel level, IGolemAPI golemApi, Predicate<Task> accepts) {
        if (!(golemApi instanceof EntityThaumaturgeGolem golem)) {
            return;
        }
        for (Task candidate : TaskBoard.of(level).openEntityTasks(null, golem)) {
            if (isChainable(golem, golemApi, candidate, accepts)) {
                assign(golem, candidate);
                return;
            }
        }
        golem.assignJob(null);
    }

    private static boolean isChainable(EntityThaumaturgeGolem golem, IGolemAPI golemApi, Task candidate, Predicate<Task> accepts) {
        if (!accepts.test(candidate) || !candidate.canBePerformedBy(golemApi)) {
            return false;
        }
        Entity target = candidate.entity();
        return target != null && golem.isWithinHome(target.blockPosition());
    }
}
