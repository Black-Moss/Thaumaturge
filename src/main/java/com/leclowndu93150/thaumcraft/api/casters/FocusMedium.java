package com.leclowndu93150.thaumcraft.api.casters;

/**
 * A node that carries the spell from a trajectory to one or more targets, such as touch,
 * projectile, or bolt.
 *
 *
 * @since 1.0.0
 */
public abstract class FocusMedium extends FocusNode {
    @Override
    public final EnumUnitType getType() {
        return EnumUnitType.MEDIUM;
    }

    @Override
    public EnumSupplyType[] mustBeSupplied() {
        return new EnumSupplyType[]{EnumSupplyType.TRAJECTORY};
    }

    @Override
    public EnumSupplyType[] willSupply() {
        return new EnumSupplyType[]{EnumSupplyType.TARGET};
    }

    /**
     * Whether this medium resolves its targets later through an intermediary such as a
     * spawned projectile. When true, the engine stops after this medium and the intermediary
     * continues the package on arrival via {@link FocusNode#getRemainingPackage()}.
     *
     * @return true when target resolution is deferred
     */
    public boolean hasIntermediary() {
        return false;
    }

    /**
     * Executes this medium along one incoming trajectory, resolving targets or launching an
     * intermediary.
     *
     * @param trajectory the incoming trajectory
     * @return true when execution succeeded
     */
    public abstract boolean execute(Trajectory trajectory);
}
