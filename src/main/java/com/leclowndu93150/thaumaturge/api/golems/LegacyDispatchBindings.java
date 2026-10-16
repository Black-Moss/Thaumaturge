package com.leclowndu93150.thaumaturge.api.golems;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

@Deprecated(since = "1.1.0", forRemoval = true)
final class LegacyDispatchBindings implements GolemDispatch.Bindings {
    private final GolemHelper.Bindings legacy;

    LegacyDispatchBindings(GolemHelper.Bindings legacy) {
        this.legacy = legacy;
    }

    @Override
    public Optional<SealType> sealType(Identifier id) {
        return legacy.sealType(id);
    }

    @Override
    public @Nullable ISealEntity sealAt(Level level, SealPos pos) {
        return legacy.getSealEntity(level, pos);
    }

    @Override
    public void postTask(Level level, Task task) {
        legacy.addGolemTask(level, task);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SupplyOrder> supplyQueue(Level level) {
        return (List<SupplyOrder>) (List<?>) legacy.getProvisionRequests(level);
    }
}
