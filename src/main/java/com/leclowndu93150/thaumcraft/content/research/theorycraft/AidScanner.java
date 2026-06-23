package com.leclowndu93150.thaumcraft.content.research.theorycraft;

import com.leclowndu93150.thaumcraft.api.research.theorycraft.ITheorycraftAid;
import com.leclowndu93150.thaumcraft.registry.TCTheorycraft;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelAccessor;

public final class AidScanner {
    private AidScanner() {}

    public static Set<ResourceKey<ITheorycraftAid>> scan(LevelAccessor level, BlockPos tablePos) {
        Set<ResourceKey<ITheorycraftAid>> matched = new HashSet<>();
        level.registryAccess().lookup(TCTheorycraft.AIDS_REGISTRY_KEY).ifPresent(lookup ->
                lookup.listElements().forEach(holder -> {
                    if (holder.value().matches(level, tablePos)) {
                        holder.unwrapKey().ifPresent(matched::add);
                    }
                }));
        return matched;
    }
}
