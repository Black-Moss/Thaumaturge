package com.leclowndu93150.thaumcraft.content.focus;

import java.util.List;
import java.util.Optional;
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

public final class FocusRayTrace {
    private static final float MIN_COLLISION_BORDER = 0.8F;
    private static final double CONTAINED_START_GROW = 0.5;

    private FocusRayTrace() {}

    public static @Nullable EntityHitResult pointedEntity(Level level, @Nullable Entity ignore, Vec3 start,
            Vec3 look, double minRange, double range, float padding, boolean nonCollide) {
        EntityHitResult pointed = null;
        Vec3 end = start.add(look.x * range, look.y * range, look.z * range);
        AABB bounds = new AABB(start.x, start.y, start.z, start.x, start.y, start.z).inflate(CONTAINED_START_GROW);
        if (ignore != null && ignore.getBoundingBox().contains(start)) {
            bounds = bounds.minmax(ignore.getBoundingBox());
        }
        List<Entity> candidates = level.getEntities(ignore,
                bounds.expandTowards(look.x * range, look.y * range, look.z * range).inflate(padding, padding, padding));
        double closest = 0.0;
        for (Entity entity : candidates) {
            if (start.distanceTo(entity.position()) < minRange) {
                continue;
            }
            if (!entity.isPickable() && !nonCollide) {
                continue;
            }
            Vec3 eyes = new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ());
            if (level.clip(new ClipContext(start, eyes, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity))
                    .getType() != HitResult.Type.MISS) {
                continue;
            }
            float border = Math.max(MIN_COLLISION_BORDER, entity.getPickRadius());
            AABB box = entity.getBoundingBox().inflate(border, border, border);
            Optional<Vec3> intercept = box.clip(start, end);
            if (box.contains(start)) {
                if (closest >= 0.0) {
                    pointed = new EntityHitResult(entity, intercept.orElse(start));
                    closest = 0.0;
                }
            } else if (intercept.isPresent()) {
                double distance = start.distanceTo(intercept.get());
                if (distance < closest || closest == 0.0) {
                    pointed = new EntityHitResult(entity, intercept.get());
                    closest = distance;
                }
            }
        }
        return pointed;
    }

    public static @Nullable HitResult pointedOrBlock(Level level, @Nullable Entity caster, Vec3 start, Vec3 look,
            double minRange, double range, float padding) {
        HitResult ray = pointedEntity(level, caster, start, look, minRange, range, padding, false);
        if (ray == null) {
            Vec3 end = look.normalize().scale(range).add(start);
            BlockHitResult blockHit = clipBlocks(level, caster, start, end);
            if (blockHit.getType() != HitResult.Type.MISS) {
                ray = blockHit;
            }
        }
        return ray;
    }

    public static BlockHitResult clipBlocks(Level level, @Nullable Entity caster, Vec3 start, Vec3 end) {
        CollisionContext context = caster != null ? CollisionContext.of(caster) : CollisionContext.empty();
        return level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, context));
    }
}
