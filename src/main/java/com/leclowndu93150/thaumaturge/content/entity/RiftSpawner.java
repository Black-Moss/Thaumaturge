package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.warp.WarpNotices;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class RiftSpawner {
    private static final Identifier FLUX_AWARENESS_RESEARCH = TTIds.rl("f_toomuchflux");
    private static final String FORMATION_NOTICE = "warp.thaumaturge.fluxevent.3";

    private static final int CHUNK_WIDTH = 16;
    private static final int UNDERGROUND_START_Y = 10;
    private static final int CLIMB_MIN_STEP = 1;
    private static final int CLIMB_STEP_SPREAD = 5;
    private static final int CLIMB_CEILING_MARGIN = 5;
    private static final int PLACEMENT_CEILING_MARGIN = 4;
    private static final double NEIGHBOUR_REACH = 32.0;
    private static final double NOTICE_REACH = 32.0;
    private static final double NOTICE_REACH_SQR = NOTICE_REACH * NOTICE_REACH;
    private static final float FLUX_SIZE_FACTOR = 3.0F;
    private static final double MIN_STRENGTH = 5.0;
    private static final double BLOCK_CENTRE = 0.5;
    private static final float FULL_TURN_DEGREES = 360.0F;

    private RiftSpawner() {}

    static void tryForm(ServerLevel level, BlockPos chunkCorner) {
        RandomSource random = level.getRandom();
        int x = chunkCorner.getX() + random.nextInt(CHUNK_WIDTH);
        int z = chunkCorner.getZ() + random.nextInt(CHUNK_WIDTH);
        if (!level.hasChunkAt(new BlockPos(x, chunkCorner.getY(), z))) {
            return;
        }
        OptionalInt height = level.dimensionType().hasSkyLight() ? openSkyHeight(level, x, z) : undergroundHeight(level, random, x, z);
        if (height.isEmpty() || height.getAsInt() >= level.getMaxY() - PLACEMENT_CEILING_MARGIN) {
            return;
        }
        BlockPos spot = new BlockPos(x, height.getAsInt(), z);
        Vec3 centre = Vec3.atCenterOf(spot);
        if (!level.getEntitiesOfClass(EntityFluxRift.class, AABB.ofSize(centre, NEIGHBOUR_REACH * 2.0, NEIGHBOUR_REACH * 2.0, NEIGHBOUR_REACH * 2.0)).isEmpty()) {
            return;
        }
        float strength = (float) Math.sqrt(FLUX_SIZE_FACTOR * AuraHelper.getFlux(level, spot));
        if (strength <= MIN_STRENGTH) {
            return;
        }
        if (place(level, random, centre, (int) strength)) {
            AuraHelper.drainFlux(level, spot, strength, false);
            warnNewcomers(level, centre);
        }
    }

    private static OptionalInt openSkyHeight(ServerLevel level, int x, int z) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z), z);
        while (cursor.getY() > level.getMinY()) {
            cursor.move(0, -1, 0);
            BlockState state = level.getBlockState(cursor);
            if (state.blocksMotion() && !state.is(BlockTags.LEAVES)) {
                return OptionalInt.of(cursor.getY() + 1);
            }
        }
        return OptionalInt.empty();
    }

    private static OptionalInt undergroundHeight(ServerLevel level, RandomSource random, int x, int z) {
        int ceiling = level.getMaxY() - CLIMB_CEILING_MARGIN;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, Math.max(UNDERGROUND_START_Y, level.getMinY()), z);
        while (!level.isEmptyBlock(cursor)) {
            cursor.move(0, CLIMB_MIN_STEP + random.nextInt(CLIMB_STEP_SPREAD), 0);
            if (cursor.getY() > ceiling) {
                return OptionalInt.empty();
            }
        }
        return OptionalInt.of(cursor.getY());
    }

    private static boolean place(ServerLevel level, RandomSource random, Vec3 centre, int size) {
        EntityFluxRift rift = TTEntities.FLUX_RIFT.get().create(level, EntitySpawnReason.EVENT);
        if (rift == null) {
            return false;
        }
        rift.reseed(EntityFluxRift.nonZeroSeed(random));
        rift.resize(size);
        rift.snapTo(centre.x, centre.y, centre.z, random.nextFloat() * FULL_TURN_DEGREES, 0.0F);
        return level.addFreshEntity(rift);
    }

    private static void warnNewcomers(ServerLevel level, Vec3 centre) {
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(centre) > NOTICE_REACH_SQR || KnowledgeAccess.of(player).isResearchComplete(FLUX_AWARENESS_RESEARCH)) {
                continue;
            }
            if (ResearchManager.complete(player, FLUX_AWARENESS_RESEARCH)) {
                WarpNotices.send(player, FORMATION_NOTICE);
            }
        }
    }
}
