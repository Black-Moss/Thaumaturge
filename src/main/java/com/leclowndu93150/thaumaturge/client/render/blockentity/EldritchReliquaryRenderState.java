package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.ReliquaryView;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public final class EldritchReliquaryRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public ReliquaryView view = ReliquaryView.INELIGIBLE;
    public float ticks;
    public float rewardLift;
    public @Nullable ItemStackRenderState reward;
}
