package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.compat.dynamictrees.DynamicTreesWorldgenBiomeModifier;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class TCBiomeModifierSerializers {
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, TCIds.MODID);

    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<DynamicTreesWorldgenBiomeModifier>> DYNAMIC_TREES_WORLDGEN = BIOME_MODIFIER_SERIALIZERS
            .register("dynamic_trees_worldgen", () -> DynamicTreesWorldgenBiomeModifier.CODEC);

    private TCBiomeModifierSerializers() {}

    public static void register(IEventBus bus) {
        BIOME_MODIFIER_SERIALIZERS.register(bus);
    }
}
