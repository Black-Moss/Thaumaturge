package com.leclowndu93150.thaumcraft.data.worldgen.feature;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.world.mound.MoundStructure;
import com.leclowndu93150.thaumcraft.registry.TCBiomeTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

public final class TCStructureBootstrap {
    public static final ResourceKey<Structure> MOUND =
            ResourceKey.create(Registries.STRUCTURE, TCIds.rl("mound"));
    public static final ResourceKey<StructureSet> MOUND_SET =
            ResourceKey.create(Registries.STRUCTURE_SET, TCIds.rl("mounds"));

    private static final int MOUND_SPACING = 10;
    private static final int MOUND_SEPARATION = 5;
    private static final int MOUND_SALT = 41626157;

    private TCStructureBootstrap() {}

    public static void bootstrapStructures(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        context.register(MOUND, new MoundStructure(
                new Structure.StructureSettings(biomes.getOrThrow(TCBiomeTags.HAS_MOUND))));
    }

    public static void bootstrapSets(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
        context.register(MOUND_SET, new StructureSet(structures.getOrThrow(MOUND),
                new RandomSpreadStructurePlacement(MOUND_SPACING, MOUND_SEPARATION,
                        RandomSpreadType.LINEAR, MOUND_SALT)));
    }
}
