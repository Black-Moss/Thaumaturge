package com.leclowndu93150.thaumaturge.mixin.client.renderer.entity;

import com.leclowndu93150.thaumaturge.client.taint.overlay.TaintOverlayRenderState;
import com.leclowndu93150.thaumaturge.client.trait.TraitRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements TraitRenderState, TaintOverlayRenderState {
    @Unique
    private int thaumaturge$traitTint = -1;

    @Unique
    private boolean thaumaturge$taintOverlay;

    @Override
    public int thaumaturge$traitTint() {
        return thaumaturge$traitTint;
    }

    @Override
    public void thaumaturge$setTraitTint(int tint) {
        this.thaumaturge$traitTint = tint;
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
