package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.golems.GolemDispatch;
import com.leclowndu93150.thaumaturge.api.golems.SupplyOrder;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.registry.TTSeals;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class GolemBindings implements GolemDispatch.Bindings {
    @Override
    public Optional<SealType> sealType(Identifier id) {
        return TTSeals.registry().getOptional(id);
    }

    @Override
    public @Nullable ISealEntity sealAt(Level level, SealPos pos) {
        return SealHandler.lookup(level, pos);
    }

    @Override
    public void postTask(Level level, Task task) {
        TaskBoard.of(level).post(task);
    }

    @Override
    public List<SupplyOrder> supplyQueue(Level level) {
        return TaskBoard.of(level).supplyOrders();
    }
}
