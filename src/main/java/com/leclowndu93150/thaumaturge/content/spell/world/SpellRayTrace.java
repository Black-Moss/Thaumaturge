package com.leclowndu93150.thaumaturge.content.spell.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

public final class SpellRayTrace {
    private static final double ENTITY_PADDING = 0.3;

    private SpellRayTrace() {}

    public static BlockHitResult clipBlocks(Level level, @Nullable Entity source, Vec3 from, Vec3 to) {
        return level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, contextFor(source)));
    }

    public static List<EntityHitResult> entitiesAlong(Level level, @Nullable Entity source, Vec3 from, Vec3 to) {
        AABB sweep = new AABB(from, to).inflate(ENTITY_PADDING + 1.0);
        List<EntityHitResult> hits = new ArrayList<>();
        for (Entity candidate : level.getEntities(source, sweep, SpellRayTrace::canBeHit)) {
            AABB box = candidate.getBoundingBox().inflate(ENTITY_PADDING);
            Optional<Vec3> entry = box.contains(from) ? Optional.of(from) : box.clip(from, to);
            entry.ifPresent(point -> hits.add(new EntityHitResult(candidate, point)));
        }
        hits.sort(Comparator.comparingDouble(hit -> hit.getLocation().distanceToSqr(from)));
        return hits;
    }

    public static HitResult trace(Level level, @Nullable Entity source, Vec3 from, Vec3 direction, double range) {
        BlockHitResult block = clipBlocks(level, source, from, pointAlong(from, direction, range));
        List<EntityHitResult> entities = entitiesAlong(level, source, from, stopPoint(block, from, direction, range));
        return entities.isEmpty() ? block : entities.getFirst();
    }

    public static List<HitResult> pierce(Level level, @Nullable Entity source, Vec3 from, Vec3 direction, double range, int entityCount) {
        BlockHitResult block = clipBlocks(level, source, from, pointAlong(from, direction, range));
        List<EntityHitResult> entities = entitiesAlong(level, source, from, stopPoint(block, from, direction, range));
        boolean blockReached = entities.size() <= entityCount && block.getType() != HitResult.Type.MISS;
        Stream<HitResult> struck = entities.stream().limit(Math.max(entityCount, 0)).map(HitResult.class::cast);
        return Stream.concat(struck, blockReached ? Stream.<HitResult>of(block) : Stream.<HitResult>empty()).collect(Collectors.toCollection(ArrayList::new));
    }

    public static Vec3 endOf(HitResult hit, Vec3 from, Vec3 direction, double range) {
        return stopPoint(hit, from, direction, range);
    }

    private static Vec3 stopPoint(HitResult hit, Vec3 from, Vec3 direction, double range) {
        return hit.getType() == HitResult.Type.MISS ? pointAlong(from, direction, range) : hit.getLocation();
    }

    private static Vec3 pointAlong(Vec3 from, Vec3 direction, double range) {
        return from.add(direction.normalize().scale(range));
    }

    private static boolean canBeHit(Entity entity) {
        return entity.isPickable() && entity.isAlive() && !entity.isSpectator();
    }

    private static CollisionContext contextFor(@Nullable Entity source) {
        if (source == null) {
            return CollisionContext.empty();
        }
        return CollisionContext.of(source);
    }
}
