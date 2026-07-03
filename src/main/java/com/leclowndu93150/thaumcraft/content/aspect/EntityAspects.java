package com.leclowndu93150.thaumcraft.content.aspect;

import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public final class EntityAspects {
    private EntityAspects() {}

    public static AspectList of(Entity entity) {
        return of(entity.getType());
    }

    public static AspectList of(EntityType<?> type) {
        AspectList aspects = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type).getData(AspectDataMaps.ENTITY_ASPECTS);
        return aspects == null ? AspectList.EMPTY : aspects;
    }
}
