package com.leclowndu93150.thaumcraft.content.essentia;

import com.leclowndu93150.thaumcraft.api.aspect.Aspects;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class EssentiaTransportHelper {
    private EssentiaTransportHelper() {}

    public static @Nullable Holder<IAspect> resolve(@Nullable Level level, ResourceKey<IAspect> key) {
        return Aspects.resolve(level, key);
    }

    public static @Nullable Holder<IAspect> resolve(HolderLookup.Provider registries, ResourceKey<IAspect> key) {
        return Aspects.resolve(registries, key);
    }

    public static @Nullable Holder<IAspect> resolve(Registry<IAspect> registry, ResourceKey<IAspect> key) {
        return Aspects.resolve(registry, key);
    }
}
