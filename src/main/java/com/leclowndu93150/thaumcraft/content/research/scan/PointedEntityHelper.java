package com.leclowndu93150.thaumcraft.content.research.scan;

import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class PointedEntityHelper {
    private static final float MIN_PICK_INFLATE = 0.8F;

    private PointedEntityHelper() {}

    public static @Nullable Entity getPointedEntity(Level level, Entity looker,
                                                    double minRange, double range, float padding, boolean nonCollide) {
        Vec3 origin = looker.getEyePosition();
        Vec3 look = looker.getLookAngle();
        Vec3 end = origin.add(look.x * range, look.y * range, look.z * range);
        AABB search = looker.getBoundingBox()
                .expandTowards(look.x * range, look.y * range, look.z * range)
                .inflate(padding, padding, padding);
        List<Entity> candidates = level.getEntities(looker, search);
        Entity pointed = null;
        double closest = 0.0;
        for (Entity candidate : candidates) {
            if (origin.distanceTo(candidate.position()) < minRange) {
                continue;
            }
            if (!candidate.isPickable() && !nonCollide) {
                continue;
            }
            Vec3 candidateEye = new Vec3(candidate.getX(), candidate.getY() + candidate.getEyeHeight(), candidate.getZ());
            HitResult blocked = level.clip(new ClipContext(
                    origin, candidateEye, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, looker));
            if (blocked.getType() != HitResult.Type.MISS) {
                continue;
            }
            float inflate = Math.max(MIN_PICK_INFLATE, candidate.getPickRadius());
            AABB box = candidate.getBoundingBox().inflate(inflate, inflate, inflate);
            if (box.contains(origin)) {
                pointed = candidate;
                closest = 0.0;
            } else {
                Vec3 hit = box.clip(origin, end).orElse(null);
                if (hit != null) {
                    double distance = origin.distanceTo(hit);
                    if (distance < closest || closest == 0.0) {
                        pointed = candidate;
                        closest = distance;
                    }
                }
            }
        }
        return pointed;
    }
}
