package com.leclowndu93150.thaumcraft.client.entity;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TaintacleRenderState extends LivingEntityRenderState {
    public float flail = 1.0F;
    public float hurt;
}
