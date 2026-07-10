package com.leclowndu93150.thaumcraft.content.focus.medium;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.leclowndu93150.thaumcraft.api.casters.FocusMedium;
import com.leclowndu93150.thaumcraft.api.casters.Trajectory;
import com.leclowndu93150.thaumcraft.content.focus.FocusFX;
import com.leclowndu93150.thaumcraft.content.focus.FocusRayTrace;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FocusMediumTouch extends FocusMedium {
    private static final Identifier KEY = TCIds.rl("touch");

    static final double RAY_MIN_RANGE = 0.25;
    static final float RAY_PADDING = 0.25F;

    private static final int COMPLEXITY = 2;
    private static final double MOTION_HALF = 2.0;

    @Override
    public Identifier getKey() {
        return KEY;
    }

    @Override
    public ResearchGate getResearch() {
        return new ResearchGate(TCIds.rl("base_auromancy"), Optional.empty(), false);
    }

    @Override
    public int getComplexity() {
        return COMPLEXITY;
    }

    @Override
    public EnumSupplyType[] willSupply() {
        return new EnumSupplyType[]{EnumSupplyType.TRAJECTORY, EnumSupplyType.TARGET};
    }

    @Override
    public ResourceKey<IAspect> getAspect() {
        return TCAspects.AVERSIO;
    }

    protected double range() {
        LivingEntity caster = getPackage().getCaster();
        return caster instanceof Player player ? player.blockInteractionRange() : 0.0;
    }

    @Override
    public Trajectory[] supplyTrajectories() {
        if (getParent() == null) {
            return new Trajectory[0];
        }
        List<Trajectory> trajectories = new ArrayList<>();
        double range = range();
        Trajectory[] supplied = getParent().supplyTrajectories();
        if (supplied != null) {
            for (Trajectory sT : supplied) {
                Vec3 direction = sT.direction().normalize();
                Vec3 end = direction;
                HitResult ray = FocusRayTrace.pointedEntity(getPackage().getLevel(), getPackage().getCaster(),
                        sT.source(), end, RAY_MIN_RANGE, range, RAY_PADDING, false);
                if (ray == null) {
                    end = end.scale(range).add(sT.source());
                    HitResult blockRay = FocusRayTrace.clipBlocks(getPackage().getLevel(), getPackage().getCaster(),
                            sT.source(), end);
                    if (blockRay.getType() != HitResult.Type.MISS) {
                        end = blockRay.getLocation();
                    }
                } else if (ray instanceof EntityHitResult entityHit) {
                    end = end.scale(sT.source().distanceTo(entityHit.getEntity().position()));
                    end = end.add(sT.source());
                }
                trajectories.add(new Trajectory(end, direction));
            }
        }
        return trajectories.toArray(new Trajectory[0]);
    }

    @Override
    public HitResult[] supplyTargets() {
        if (getParent() == null || !(getPackage().getCaster() instanceof Player)) {
            return new HitResult[0];
        }
        List<HitResult> targets = new ArrayList<>();
        double range = range();
        Trajectory[] supplied = getParent().supplyTrajectories();
        if (supplied != null) {
            for (Trajectory sT : supplied) {
                HitResult ray = FocusRayTrace.pointedOrBlock(getPackage().getLevel(), getPackage().getCaster(),
                        sT.source(), sT.direction().normalize(), RAY_MIN_RANGE, range, RAY_PADDING);
                if (ray != null) {
                    targets.add(ray);
                }
            }
        }
        return targets.toArray(new HitResult[0]);
    }

    @Override
    public boolean execute(Trajectory trajectory) {
        if (getPackage().getLevel() instanceof ServerLevel level) {
            FocusFX.burst(level, trajectory.source(), trajectory.direction().scale(1.0 / MOTION_HALF), getPackage());
        }
        return true;
    }
}
