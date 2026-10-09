package com.leclowndu93150.thaumaturge.content.world.objects;

import com.leclowndu93150.thaumaturge.content.entity.EntityCultistPortalLesser;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.event.EventHooks;

public final class CrimsonPortalFeature extends Feature<NoneFeatureConfiguration> {
    private static final int HEADROOM = 3;
    private static final double CENTER_OFFSET = 0.5;

    public CrimsonPortalFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos spawnPos = context.origin();
        if (!isSolidFloor(level, spawnPos) || !isOpenAbove(level, spawnPos)) {
            return false;
        }
        EntityCultistPortalLesser portal = TTEntities.CULTIST_PORTAL_LESSER.get().create(level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (portal == null) {
            return false;
        }
        portal.setPersistenceRequired();
        portal.snapTo(spawnPos.getX() + CENTER_OFFSET, spawnPos.getY(), spawnPos.getZ() + CENTER_OFFSET, 0.0F, 0.0F);
        spawn(level, portal, spawnPos);
        return true;
    }

    private static boolean isSolidFloor(WorldGenLevel level, BlockPos spawnPos) {
        BlockPos floor = spawnPos.below();
        return level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP);
    }

    private static boolean isOpenAbove(WorldGenLevel level, BlockPos spawnPos) {
        BlockPos.MutableBlockPos cursor = spawnPos.mutable();
        for (int step = 0; step < HEADROOM; step++) {
            if (!level.getBlockState(cursor).isAir()) {
                return false;
            }
            cursor.move(Direction.UP);
        }
        return true;
    }

    private static void spawn(WorldGenLevel level, EntityCultistPortalLesser portal, BlockPos spawnPos) {
        EntitySpawnReason reason = EntitySpawnReason.STRUCTURE;
        EventHooks.finalizeMobSpawn(portal, level, level.getCurrentDifficultyAt(spawnPos), reason, null);
        level.addFreshEntityWithPassengers(portal);
    }
}
