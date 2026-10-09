package com.leclowndu93150.thaumaturge.content.entity;

import java.util.List;
import net.minecraft.server.level.ServerLevel;

final class RiftTaskSchedule {
    private final List<RiftTask> tasks;

    RiftTaskSchedule(List<RiftTask> tasks) {
        this.tasks = List.copyOf(tasks);
    }

    void tick(ServerLevel level, EntityFluxRift rift) {
        int age = rift.tickCount;
        int id = rift.getId();
        for (RiftTask task : this.tasks) {
            int interval = task.interval();
            if (age % interval == id % interval) {
                task.run(level, rift);
            }
        }
    }
}
