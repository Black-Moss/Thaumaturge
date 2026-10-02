package com.leclowndu93150.thaumaturge.mixin.client.renderer.entity;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.client.taint.overlay.TaintOverlayRenderState;
import com.leclowndu93150.thaumaturge.client.trait.MobTraitVisuals;
import com.leclowndu93150.thaumaturge.client.trait.TraitRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.Holder;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void thaumaturge$extractTraits(T entity, S state, float partialTicks, CallbackInfo ci) {
        int tint = -1;
        boolean tainted = false;
        for (Holder<MobTrait> trait : MobTraits.traits(entity)) {
            tint = ARGB.multiply(tint, MobTraitVisuals.tint(trait));
            tainted |= trait.value().isTaint();
        }
        ((TraitRenderState) state).thaumaturge$setTraitTint(tint);
        ((TaintOverlayRenderState) state).thaumaturge$setTaintOverlay(tainted);
    }

    @Inject(method = "getModelTint", at = @At("RETURN"), cancellable = true)
    private void thaumaturge$traitTint(S state, CallbackInfoReturnable<Integer> cir) {
        int tint = ((TraitRenderState) state).thaumaturge$traitTint();
        if (tint != -1) {
            cir.setReturnValue(ARGB.multiply(cir.getReturnValue(), tint));
        }
    }
}
