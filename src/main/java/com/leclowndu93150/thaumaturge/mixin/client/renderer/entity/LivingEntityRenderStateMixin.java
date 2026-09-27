package com.leclowndu93150.thaumaturge.mixin.client.renderer.entity;

import com.leclowndu93150.thaumaturge.client.champion.ChampionRenderState;
import com.leclowndu93150.thaumaturge.client.taint.overlay.TaintOverlayRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements ChampionRenderState, TaintOverlayRenderState {
    @Unique
    private int thaumaturge$championType = -2;

    @Unique
    private int thaumaturge$entityId;

    @Unique
    private boolean thaumaturge$taintOverlay;

    @Override
    public int thaumaturge$championType() {
        return thaumaturge$championType;
    }

    @Override
    public void thaumaturge$setChampionType(int type) {
        this.thaumaturge$championType = type;
    }

    @Override
    public int thaumaturge$entityId() {
        return thaumaturge$entityId;
    }

    @Override
    public void thaumaturge$setEntityId(int id) {
        this.thaumaturge$entityId = id;
    }

    @Override
    public boolean thaumaturge$taintOverlay() {
        return thaumaturge$taintOverlay;
    }

    @Override
    public void thaumaturge$setTaintOverlay(boolean overlay) {
        this.thaumaturge$taintOverlay = overlay;
    }
}
