package com.leclowndu93150.thaumcraft.client.render.blockentity;

import com.leclowndu93150.thaumcraft.api.nodes.NodeModifier;
import com.leclowndu93150.thaumcraft.api.nodes.NodeType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.jspecify.annotations.Nullable;

public final class NodeRenderState extends BlockEntityRenderState {
    public static final class AspectLayer {
        public int color;
        public int amount;
        public int blend;
    }

    public final List<AspectLayer> layers = new ArrayList<>();
    public NodeType type = NodeType.NORMAL;
    public @Nullable NodeModifier modifier;
    public boolean visible;
    public boolean depthIgnore;
    public float alpha;
    public float ticks;
    public long time;
    public int frameSeed;
    public float size = 1.0F;
    public boolean jarred;
    public boolean draining;
    public double drainFromX;
    public double drainFromY;
    public double drainFromZ;
    public int drainColor = 0xFFFFFF;
    public float drainTime;
}
