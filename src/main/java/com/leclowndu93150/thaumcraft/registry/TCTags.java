package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class TCTags {
    public static final TagKey<Biome> HAS_GREATWOOD = biome("has_greatwood");
    public static final TagKey<Biome> HAS_GREATWOOD_RARE = biome("has_greatwood_rare");
    public static final TagKey<Biome> HAS_SILVERWOOD = biome("has_silverwood");
    public static final TagKey<Biome> HAS_CINDERPEARL = biome("has_cinderpearl");

    private static TagKey<Biome> biome(String path) {
        return TagKey.create(Registries.BIOME, TCIds.rl(path));
    }

    private TCTags() {}
}
