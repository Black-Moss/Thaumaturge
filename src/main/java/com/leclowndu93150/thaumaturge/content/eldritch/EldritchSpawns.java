package com.leclowndu93150.thaumaturge.content.eldritch;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public final class EldritchSpawns {
    private static final float FULL_TURN_DEGREES = 360.0F;

    private EldritchSpawns() {}

    public static Optional<Mob> prepare(ServerLevel level, EntityType<?> type, BlockPos at, BlockPos home, int leash, RandomSource random) {
        Entity entity = type.create(level, EntitySpawnReason.EVENT);
        if (!(entity instanceof Mob mob)) {
            if (entity != null) {
                entity.discard();
            }
            return Optional.empty();
        }
        mob.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, random.nextFloat() * FULL_TURN_DEGREES, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(at), EntitySpawnReason.EVENT, null);
        mob.setPersistenceRequired();
        mob.setHomeTo(home, leash);
        return Optional.of(mob);
    }
}
