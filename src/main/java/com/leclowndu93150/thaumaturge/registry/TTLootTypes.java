package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.entity.loot.DifficultyValue;
import com.leclowndu93150.thaumaturge.content.entity.loot.LabyrinthBoundCondition;
import com.leclowndu93150.thaumaturge.content.entity.loot.NoNearbyKinCondition;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TTLootTypes {
    public static final DeferredRegister<MapCodec<? extends LootItemCondition>> CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, TTIds.MODID);

    public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<LabyrinthBoundCondition>> LABYRINTH_BOUND = CONDITIONS.register("labyrinth_bound",
            () -> LabyrinthBoundCondition.MAP_CODEC);
    public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<NoNearbyKinCondition>> NO_NEARBY_KIN = CONDITIONS.register("no_nearby_kin",
            () -> NoNearbyKinCondition.MAP_CODEC);

    public static final DeferredRegister<MapCodec<? extends NumberProvider>> NUMBER_PROVIDERS = DeferredRegister.create(Registries.LOOT_NUMBER_PROVIDER_TYPE, TTIds.MODID);

    public static final DeferredHolder<MapCodec<? extends NumberProvider>, MapCodec<DifficultyValue>> BY_DIFFICULTY = NUMBER_PROVIDERS.register("by_difficulty", () -> DifficultyValue.MAP_CODEC);

    private TTLootTypes() {}

    public static void register(IEventBus modBus) {
        CONDITIONS.register(modBus);
        NUMBER_PROVIDERS.register(modBus);
    }
}
