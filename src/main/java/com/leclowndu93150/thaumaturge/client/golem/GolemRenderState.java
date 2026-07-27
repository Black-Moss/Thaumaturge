package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class GolemRenderState extends EntityRenderState {
    public GolemProperties props;
    public byte color;
    public float bodyRot;
    public float headYawDelta;
    public float pitch;
    public float walkPos;
    public float walkSpeed;
    public float attackTime;
    public double speedSq;
    public float yawDelta;
    public float wheelRotation;
    public float grinderRot;
    public boolean combat;
    public boolean invisible;
    public boolean ghost;
    public boolean xray;
    public final ItemStackRenderState heldItem = new ItemStackRenderState();
    public boolean holdingItem;
    public final ItemStackRenderState haulerItem = new ItemStackRenderState();
    public boolean haulingItem;
    public boolean heldItemIsBlock;
    public boolean haulerItemIsBlock;
    public String accessories = "";
}
