package com.leclowndu93150.thaumaturge.client.render.blockentity;

public final class RechargePedestalRenderState extends PedestalRenderState {
    public boolean draining;
    public double drainFromX;
    public double drainFromY;
    public double drainFromZ;
    public int drainColor = 0xFFFFFF;
    public long time;
    public float partial;
}
