package com.leclowndu93150.thaumaturge.content.golem.tasks;

import com.leclowndu93150.thaumaturge.api.golems.ProvisionRequest;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class GolemTasks {
    private Map<Integer, Task> tasks = new ConcurrentHashMap<>();
    private final List<ProvisionRequest> provisionRequests = new CopyOnWriteArrayList<>();
    private int nextTaskId;

    public Map<Integer, Task> tasks() {
        return tasks;
    }

    public void replaceTasks(Map<Integer, Task> tasks) {
        this.tasks = tasks;
    }

    public List<ProvisionRequest> provisionRequests() {
        return provisionRequests;
    }

    public int nextTaskId() {
        return ++nextTaskId;
    }
}
