package com.leclowndu93150.thaumcraft.client.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.Vec3;

public final class FluxRiftRenderState extends EntityRenderState {
    public final List<Vec3> points = new ArrayList<>();
    public final List<Float> widths = new ArrayList<>();
    public float stability;
    public float animationTime;
    public boolean goggles;
}
