package com.leclowndu93150.thaumcraft.api.casters;

import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The implicit first node of every executed package. It supplies the initial trajectory and
 * target, normally derived from the caster's eyes and look direction. The engine inserts one
 * automatically when a package starts without it.
 *
 *
 * @since 1.0.0
 */
public class FocusMediumRoot extends FocusMedium {
    private static final Identifier KEY = Identifier.fromNamespaceAndPath("thaumcraft", "root");
    private static final float SOURCE_EYE_OFFSET = 0.1F;

    private Trajectory[] trajectories;
    private HitResult[] targets;

    /**
     * Creates an unconfigured root; call one of the {@code setupFrom...} methods or let
     * {@link FocusPackage#initialize(LivingEntity)} configure it from the caster.
     */
    public FocusMediumRoot() {
    }

    /**
     * Creates a root pre-loaded with explicit supplies.
     *
     * @param trajectories the trajectories to supply, or null
     * @param targets      the targets to supply, or null
     */
    public FocusMediumRoot(Trajectory @Nullable [] trajectories, HitResult @Nullable [] targets) {
        this.trajectories = trajectories;
        this.targets = targets;
    }

    @Override
    public Identifier getKey() {
        return KEY;
    }

    @Override
    public int getComplexity() {
        return 0;
    }

    @Override
    public EnumSupplyType @Nullable [] mustBeSupplied() {
        return null;
    }

    @Override
    public EnumSupplyType[] willSupply() {
        return new EnumSupplyType[]{EnumSupplyType.TARGET, EnumSupplyType.TRAJECTORY};
    }

    @Override
    public HitResult @Nullable [] supplyTargets() {
        return targets;
    }

    @Override
    public Trajectory @Nullable [] supplyTrajectories() {
        return trajectories;
    }

    @Override
    public boolean execute(Trajectory trajectory) {
        return true;
    }

    /**
     * Supplies one trajectory from just below the caster's eyes along the look vector, and
     * the caster itself as the initial target.
     *
     * @param caster the casting entity
     */
    public void setupFromCaster(LivingEntity caster) {
        trajectories = new Trajectory[]{new Trajectory(sourceVector(caster), caster.getLookAngle())};
        targets = new HitResult[]{new EntityHitResult(caster)};
    }

    /**
     * Supplies one trajectory from the caster aimed at the vertical center of a target
     * entity.
     *
     * @param caster the casting entity
     * @param target the entity to aim at
     */
    public void setupFromCasterToTarget(LivingEntity caster, Entity target) {
        setupFromCasterToTarget(caster, target, 0.0);
    }

    /**
     * Supplies one trajectory from the caster aimed at the vertical center of a target
     * entity, adjusted by a vertical offset.
     *
     * @param caster the casting entity
     * @param target the entity to aim at
     * @param offset the vertical aim adjustment in blocks
     */
    public void setupFromCasterToTarget(LivingEntity caster, Entity target, double offset) {
        Vec3 source = sourceVector(caster);
        double dx = target.getX() - source.x;
        double dy = target.getBoundingBox().minY + target.getBbHeight() / 2.0F - source.y;
        double dz = target.getZ() - source.z;
        trajectories = new Trajectory[]{new Trajectory(source, new Vec3(dx, dy + offset, dz).normalize())};
        targets = new HitResult[]{new EntityHitResult(caster)};
    }

    /**
     * Supplies one trajectory from the caster aimed at a world position.
     *
     * @param caster the casting entity
     * @param loc    the position to aim at
     */
    public void setupFromCasterToTargetLoc(LivingEntity caster, Vec3 loc) {
        Vec3 source = sourceVector(caster);
        trajectories = new Trajectory[]{new Trajectory(source, loc.subtract(source).normalize())};
        targets = new HitResult[]{new EntityHitResult(caster)};
    }

    private static Vec3 sourceVector(LivingEntity entity) {
        return entity.position().add(0.0, entity.getEyeHeight() - SOURCE_EYE_OFFSET, 0.0);
    }

    @Override
    public @Nullable ResourceKey<IAspect> getAspect() {
        return null;
    }
}
