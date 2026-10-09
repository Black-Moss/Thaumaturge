package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

abstract class HuntBehavior implements ISealBehavior {
    private static final int MARK_LIFE = 10;
    private static final int ENGAGE_XP = 1;

    protected abstract boolean isQuarry(ServerLevel level, ISealEntity seal, LivingEntity target);

    protected void onHuntOver() {}

    protected static void mark(ServerLevel level, ISealEntity seal, LivingEntity target) {
        Task hunt = Task.onEntity(seal.pos(), target);
        hunt.setLife(MARK_LIFE);
        hunt.setPriority(seal.priority());
        GolemHelper.addGolemTask(level, hunt);
    }

    @Override
    public void onTaskStarted(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        engage(level, seal, golem, task);
        task.end();
        onHuntOver();
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        finish();
        return true;
    }

    @Override
    public void onTaskSuspended(ServerLevel level, ISealEntity seal, Task task) {
        finish();
    }

    @Override
    public void onRemoved(ServerLevel level, ISealEntity seal) {
        finish();
    }

    private void finish() {
        onHuntOver();
    }

    private void engage(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        if (!(task.entity() instanceof LivingEntity quarry) || !(golem.asEntity() instanceof Mob hunter)) {
            return;
        }
        if (isQuarry(level, seal, quarry)) {
            hunter.setTarget(quarry);
            golem.addRankXp(ENGAGE_XP);
        }
    }
}
