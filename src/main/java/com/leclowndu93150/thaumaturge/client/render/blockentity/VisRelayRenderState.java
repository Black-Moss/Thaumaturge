package com.leclowndu93150.thaumaturge.client.render.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class VisRelayRenderState extends BlockEntityRenderState {
    public @Nullable Vec3 beamTarget;
    public float ticks;
    public long time;
}
