package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.registry.TCBiomeTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TaintNaturalSpawnEvents {
    private TaintNaturalSpawnEvents() {}

    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        Mob mob = event.getEntity();
        EntitySpawnReason reason = event.getSpawnType();
        ServerLevelAccessor level = event.getLevel();
        if (!isNaturalWorldSpawn(reason) || mob.isSpawnCancelled() || !TaintMobConversion.canConvert(level.getLevel(), mob) || !level.getBiome(mob.blockPosition()).is(TCBiomeTags.IS_TAINTED)) {
            return;
        }
        TaintConversion conversion = TaintMobConversion.conversionFor(mob.getType());
        if (conversion == null || !conversion.naturalSpawns()) {
            return;
        }
        Mob replacement = TaintMobConversion.createReplacement(level.getLevel(), mob, conversion.into(), reason);
        if (replacement == null) {
            return;
        }
        event.setSpawnCancelled(true);
        EventHooks.finalizeMobSpawn(replacement, level, event.getDifficulty(), reason, null);
        level.addFreshEntityWithPassengers(replacement);
    }

    private static boolean isNaturalWorldSpawn(EntitySpawnReason reason) {
        return reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION || reason == EntitySpawnReason.STRUCTURE;
    }
}
