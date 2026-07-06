package com.leclowndu93150.thaumcraft.api.casters;

import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

/**
 * A node that alters how the rest of the package executes, such as splitting the spell into
 * branches or scattering its trajectories.
 *
 *
 * @since 1.0.0
 */
public abstract class FocusMod extends FocusNode {
    @Override
    public final EnumUnitType getType() {
        return EnumUnitType.MOD;
    }

    /**
     * Executes this modifier's transformation.
     *
     * @return true when the modifier did something
     */
    public abstract boolean execute();

    @Override
    public @Nullable ResourceKey<IAspect> getAspect() {
        return null;
    }
}
