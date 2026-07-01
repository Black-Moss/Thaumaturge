package com.leclowndu93150.thaumcraft.client.entity;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TaintSeedRenderState extends LivingEntityRenderState {
    public float hurt;
    public float attackAnim;
}
