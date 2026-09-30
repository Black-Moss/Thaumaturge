package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.HierophantAction;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public final class HierophantRenderState extends LivingEntityRenderState {
    public HierophantAction action = HierophantAction.IDLE;
    public float actionTicks;
    public float movement;
    public float recoil;
    public boolean awakened;
}
