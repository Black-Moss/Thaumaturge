package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.AspectDataMaps;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IEntityAspectSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public final class EntityAspects {
    private EntityAspects() {}

    public static AspectList of(Entity entity) {
        if (entity instanceof IEntityAspectSource source) {
            return source.entityAspects();
        }
        return of(entity.getType());
    }

    public static AspectList of(EntityType<?> type) {
        AspectList aspects = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type).getData(AspectDataMaps.ENTITY_ASPECTS);
        return aspects == null ? AspectList.EMPTY : aspects;
    }
}
