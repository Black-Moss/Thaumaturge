package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.world.crystal.CrystalClusterConfig;
import com.leclowndu93150.thaumcraft.content.world.crystal.CrystalClusterFeature;
import com.leclowndu93150.thaumcraft.content.world.plant.MagicForestFloraConfig;
import com.leclowndu93150.thaumcraft.content.world.plant.MagicForestFloraFeature;
import com.leclowndu93150.thaumcraft.content.world.tree.BigMagicTreeConfig;
import com.leclowndu93150.thaumcraft.content.world.tree.BigMagicTreeFeature;
import com.leclowndu93150.thaumcraft.content.world.tree.BigTreeConfig;
import com.leclowndu93150.thaumcraft.content.world.tree.BigTreeFeature;
import com.leclowndu93150.thaumcraft.content.world.tree.SilverwoodTreeConfig;
import com.leclowndu93150.thaumcraft.content.world.tree.SilverwoodTreeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, TCIds.MODID);

    public static final DeferredHolder<Feature<?>, BigTreeFeature> BIG_TREE =
            FEATURES.register("big_tree", () -> new BigTreeFeature(BigTreeConfig.CODEC));

    public static final DeferredHolder<Feature<?>, BigMagicTreeFeature> BIG_MAGIC_TREE =
            FEATURES.register("big_magic_tree", () -> new BigMagicTreeFeature(BigMagicTreeConfig.CODEC));

    public static final DeferredHolder<Feature<?>, SilverwoodTreeFeature> SILVERWOOD_TREE =
            FEATURES.register("silverwood_tree", () -> new SilverwoodTreeFeature(SilverwoodTreeConfig.CODEC));

    public static final DeferredHolder<Feature<?>, CrystalClusterFeature> CRYSTAL_CLUSTER =
            FEATURES.register("crystal_cluster", () -> new CrystalClusterFeature(CrystalClusterConfig.CODEC));

    public static final DeferredHolder<Feature<?>, MagicForestFloraFeature> MAGIC_FOREST_FLORA =
            FEATURES.register("magic_forest_flora", () -> new MagicForestFloraFeature(MagicForestFloraConfig.CODEC));

    private TCFeatures() {}

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
