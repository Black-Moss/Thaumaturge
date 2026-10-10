package com.leclowndu93150.thaumaturge.content.warp.spawn;

import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchGuardian;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;

public final class GuardianSummons {
    private GuardianSummons() {}

    public static void summon(ServerLevel level, ServerPlayer player, RandomSource random) {
        EntityEldritchGuardian guardian = TTEntities.ELDRITCH_GUARDIAN.get().create(level, EntitySpawnReason.EVENT);
        if (guardian == null) {
            return;
        }
        BlockPos origin = player.blockPosition();
        for (int attempt = 0; attempt < SpawnSpots.PLACEMENT_ATTEMPTS; attempt++) {
            BlockPos cell = SpawnSpots.ringCell(origin, random);
            if (!SpawnSpots.standsOnFullBlock(level, cell)) {
                continue;
            }
            guardian.snapTo(cell.getX() + SpawnSpots.CELL_CENTER, cell.getY(), cell.getZ() + SpawnSpots.CELL_CENTER, random.nextFloat() * SpawnSpots.FULL_TURN_DEGREES, 0.0F);
            if (!SpawnSpots.hasFreeSpace(level, guardian)) {
                continue;
            }
            if (level.addFreshEntity(guardian)) {
                guardian.setTarget(player);
            }
            return;
        }
    }
}
