package com.leclowndu93150.thaumcraft.content.focus.medium;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.casters.FocusEffect;
import com.leclowndu93150.thaumcraft.api.casters.FocusEngine;
import com.leclowndu93150.thaumcraft.api.casters.Trajectory;
import com.leclowndu93150.thaumcraft.content.focus.FocusRayTrace;
import com.leclowndu93150.thaumcraft.content.fx.FX;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FocusMediumBolt extends FocusMediumTouch {
    private static final Identifier KEY = TCIds.rl("bolt");

    static final double BOLT_RANGE = 16.0;

    private static final int COMPLEXITY = 5;
    private static final float BOLT_WIDTH_FACTOR = 0.66F;

    @Override
    public Identifier getKey() {
        return KEY;
    }

    @Override
    public int getComplexity() {
        return COMPLEXITY;
    }

    @Override
    public ResourceKey<IAspect> getAspect() {
        return TCAspects.POTENTIA;
    }

    @Override
    protected double range() {
        return BOLT_RANGE;
    }

    @Override
    public boolean execute(Trajectory trajectory) {
        Vec3 end = trajectory.direction().normalize();
        HitResult ray = FocusRayTrace.pointedEntity(getPackage().getLevel(), getPackage().getCaster(),
                trajectory.source(), end, RAY_MIN_RANGE, BOLT_RANGE, RAY_PADDING, false);
        if (ray == null) {
            end = end.scale(BOLT_RANGE).add(trajectory.source());
            HitResult blockRay = FocusRayTrace.clipBlocks(getPackage().getLevel(), getPackage().getCaster(),
                    trajectory.source(), end);
            if (blockRay.getType() != HitResult.Type.MISS) {
                end = blockRay.getLocation();
            }
        } else if (ray instanceof EntityHitResult entityHit) {
            end = end.scale(trajectory.source().distanceTo(entityHit.getEntity().position()));
            end = end.add(trajectory.source());
        }
        List<FocusEffect> effects = getPackage().getFocusEffects();
        if (!effects.isEmpty() && getPackage().getLevel() instanceof ServerLevel level) {
            int r = 0;
            int g = 0;
            int b = 0;
            for (FocusEffect effect : effects) {
                int color = FocusEngine.getElementColor(effect.getKey());
                r += (color >> 16) & 0xFF;
                g += (color >> 8) & 0xFF;
                b += color & 0xFF;
            }
            r /= effects.size();
            g /= effects.size();
            b /= effects.size();
            FX.arcBolt(level, trajectory.source())
                    .to(end)
                    .color((r << 16) | (g << 8) | b)
                    .width(getPackage().getPower() * BOLT_WIDTH_FACTOR)
                    .send();
        }
        return true;
    }
}
