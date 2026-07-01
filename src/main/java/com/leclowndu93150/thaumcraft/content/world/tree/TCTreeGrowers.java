package com.leclowndu93150.thaumcraft.content.world.tree;

import com.leclowndu93150.thaumcraft.TCIds;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class TCTreeGrowers {
    public static final ResourceKey<ConfiguredFeature<?, ?>> GREATWOOD_TREE_GROWN =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, TCIds.rl("greatwood_tree_grown"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> SILVERWOOD_TREE_GROWN =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, TCIds.rl("silverwood_tree_grown"));

    public static final TreeGrower GREATWOOD = new TreeGrower(
            "thaumcraft:greatwood", Optional.of(GREATWOOD_TREE_GROWN), Optional.empty(), Optional.empty()
    );
    public static final TreeGrower SILVERWOOD = new TreeGrower(
            "thaumcraft:silverwood", Optional.empty(), Optional.of(SILVERWOOD_TREE_GROWN), Optional.empty()
    );

    private TCTreeGrowers() {}

    public static void touch() {}
}
