package com.leclowndu93150.thaumaturge.content.research.scan;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.research.scan.IScannable;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanTarget;
import com.leclowndu93150.thaumaturge.api.research.scan.ScannedSky;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

public final class ScanAspectDiscovery implements IScannable {
    private final ResourceKey<IAspect> target;

    public ScanAspectDiscovery(ResourceKey<IAspect> aspect) {
        target = aspect;
    }

    private Optional<Holder.Reference<IAspect>> resolve(Player player) {
        return player.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(target);
    }

    private static boolean unlocksFor(Player player, ScanTarget scanned, Holder<IAspect> holder) {
        boolean present = ScanningManager.aspectsOf(player, scanned).amountOf(holder) > 0;
        return present && AspectPools.hasDiscoveredComponents(player, holder);
    }

    @Override
    public boolean matches(Player player, ScanTarget scanned) {
        return !(scanned instanceof ScannedSky) && resolve(player).filter(holder -> unlocksFor(player, scanned, holder)).isPresent();
    }

    @Override
    public Identifier research(Player player, ScanTarget scanned) {
        return ScanKeys.aspect(target);
    }
}
