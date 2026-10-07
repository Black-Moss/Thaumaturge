package com.leclowndu93150.thaumaturge.content.entity.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record NoNearbyKinCondition(double radius) implements LootItemCondition {
    public static final MapCodec<NoNearbyKinCondition> MAP_CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(Codec.doubleRange(0.0, Double.MAX_VALUE).fieldOf("radius").forGetter(NoNearbyKinCondition::radius)).apply(instance, NoNearbyKinCondition::new));

    public static LootItemCondition.Builder noNearbyKin(double radius) {
        return () -> new NoNearbyKinCondition(radius);
    }

    @Override
    public MapCodec<NoNearbyKinCondition> codec() {
        return MAP_CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.THIS_ENTITY);
    }

    @Override
    public boolean test(LootContext context) {
        Entity entity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (entity == null) {
            return true;
        }
        return entity.level().getEntities(entity.getType(), entity.getBoundingBox().inflate(radius), other -> other != entity).isEmpty();
    }
}
