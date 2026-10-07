package com.leclowndu93150.thaumaturge.content.entity.loot;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthHelper;
import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public final class LabyrinthBoundCondition implements LootItemCondition {
    private static final LabyrinthBoundCondition INSTANCE = new LabyrinthBoundCondition();
    public static final MapCodec<LabyrinthBoundCondition> MAP_CODEC = MapCodec.unit(INSTANCE);

    private LabyrinthBoundCondition() {}

    public static LootItemCondition.Builder labyrinthBound() {
        return () -> INSTANCE;
    }

    @Override
    public MapCodec<LabyrinthBoundCondition> codec() {
        return MAP_CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.THIS_ENTITY);
    }

    @Override
    public boolean test(LootContext context) {
        Entity entity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        return entity != null && LabyrinthHelper.isLabyrinthBound(entity);
    }
}
