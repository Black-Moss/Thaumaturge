package com.leclowndu93150.thaumcraft.api.casters;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

/**
 * A node that applies the spell's payload to a resolved target, such as damage, a potion
 * effect, or block interaction.
 *
 *
 * @since 1.0.0
 */
public abstract class FocusEffect extends FocusNode {
    @Override
    public final EnumUnitType getType() {
        return EnumUnitType.EFFECT;
    }

    @Override
    public final EnumSupplyType[] mustBeSupplied() {
        return new EnumSupplyType[]{EnumSupplyType.TARGET};
    }

    @Override
    public EnumSupplyType @Nullable [] willSupply() {
        return null;
    }

    /**
     * Applies this effect to one target. Called once per supplied target.
     *
     * @param target     the resolved hit to affect
     * @param trajectory the trajectory that produced the hit, or null when unknown
     * @param finalPower the package's accumulated power at execution time
     * @param num        the index of this target within the supplied batch
     * @return true when the effect did something
     */
    public abstract boolean execute(HitResult target, @Nullable Trajectory trajectory, float finalPower, int num);

    /**
     * The damage figure shown for this effect in the focal manipulator.
     *
     * @param finalPower the power the display assumes
     * @return the displayed damage, or 0 when the effect deals none
     */
    public float getDamageForDisplay(float finalPower) {
        return 0.0F;
    }

    /**
     * Spawns this effect's travel or impact particles without carrier drift. Called on the
     * client only.
     *
     * @param level the client level
     * @param x     the particle x position
     * @param y     the particle y position
     * @param z     the particle z position
     * @param mx    the particle x motion
     * @param my    the particle y motion
     * @param mz    the particle z motion
     */
    public final void renderParticleFX(Level level, double x, double y, double z, double mx, double my, double mz) {
        renderParticleFX(level, x, y, z, mx, my, mz, 0.0, 0.0, 0.0);
    }

    /**
     * Spawns this effect's travel or impact particles. Called on the client only.
     *
     * <p>The drift is a carrier velocity the particle applies every tick of its life,
     * unaffected by its own friction or gravity. Cast bursts pass the caster's velocity here
     * so the flash keeps pace with a moving caster.
     *
     * @param level the client level
     * @param x     the particle x position
     * @param y     the particle y position
     * @param z     the particle z position
     * @param mx    the particle x motion
     * @param my    the particle y motion
     * @param mz    the particle z motion
     * @param dx    the per-tick x drift
     * @param dy    the per-tick y drift
     * @param dz    the per-tick z drift
     */
    public void renderParticleFX(Level level, double x, double y, double z, double mx, double my, double mz,
            double dx, double dy, double dz) {
    }

    /**
     * Called once on the server when the package containing this effect is cast, before any
     * node executes.
     *
     * @param caster the casting entity
     */
    public void onCast(Entity caster) {
    }
}
