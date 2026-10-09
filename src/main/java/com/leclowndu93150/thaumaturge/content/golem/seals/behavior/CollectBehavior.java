package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealArea;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskHandoff;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public final class CollectBehavior implements ISealBehavior {
    private static final int STAGGER = 100;
    private static final int SCAN_PERIOD = 5;

    private final SealClock clock = new SealClock(STAGGER);
    private final TaskLedger<ItemEntity> targets = new TaskLedger<>();

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        if (clock.advance() % SCAN_PERIOD != 0) {
            return;
        }
        targets.dropFinished(level);
        targets.dropValues(item -> item.isRemoved() || !item.isAlive());
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, SealArea.bounds(seal))) {
            if (isCollectable(seal, item)) {
                Task task = Task.onEntity(seal.pos(), item);
                task.setPriority(seal.priority());
                GolemHelper.addGolemTask(level, task);
                targets.record(task, item);
                return;
            }
        }
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        ItemEntity item = targets.get(task);
        if (item != null && !item.isRemoved() && !item.getItem().isEmpty() && ItemMatchSettings.accepts(seal, item.getItem())) {
            ItemStack rest = golem.hands().hold(item.getItem().copy());
            if (rest.isEmpty()) {
                item.discard();
            } else {
                item.setItem(rest);
            }
            HandlingSound.play(golem, HandlingSound.HIGH);
            golem.swingArm();
        }
        targets.forget(task);
        TaskHandoff.continueWith(level, golem, targets::has);
        return true;
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        ItemEntity item = targets.get(task);
        if (item == null || !item.isAlive()) {
            task.end();
            return false;
        }
        ItemStack stack = item.getItem();
        return !stack.isEmpty() && golem.hands().canTake(stack, true);
    }

    private boolean isCollectable(ISealEntity seal, ItemEntity item) {
        return item.onGround() && !item.hasPickUpDelay() && !item.getItem().isEmpty() && !targets.tracks(item) && ItemMatchSettings.accepts(seal, item.getItem());
    }
}
