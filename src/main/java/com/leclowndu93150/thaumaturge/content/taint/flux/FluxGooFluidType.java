package com.leclowndu93150.thaumaturge.content.taint.flux;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.fluids.FluidType;

public final class FluxGooFluidType extends FluidType {
    private static final double DRAG = 0.0001D;

    public FluxGooFluidType(Properties properties) {
        super(properties);
    }

    @Override
    public double motionScale(Entity entity) {
        return DRAG;
    }
}
