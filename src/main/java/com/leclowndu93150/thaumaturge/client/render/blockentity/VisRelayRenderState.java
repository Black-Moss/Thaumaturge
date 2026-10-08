package com.leclowndu93150.thaumaturge.client.render.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class VisRelayRenderState extends BlockEntityRenderState {
    public Vec3 beamOrigin = Vec3.ZERO;
    public @Nullable Vec3 beamTarget;
    public float scroll;
    public float red = 1.0F;
    public float green = 1.0F;
    public float blue = 1.0F;
    public float opacity;
    public boolean revealing;
    public int flareFrame;
}
