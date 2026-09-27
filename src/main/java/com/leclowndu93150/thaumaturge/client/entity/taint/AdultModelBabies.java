package com.leclowndu93150.thaumaturge.client.entity.taint;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public final class AdultModelBabies {
    private AdultModelBabies() {}

    public static void useAdultModel(LivingEntityRenderState state) {
        state.isBaby = false;
    }

    public static void scale(LivingEntityRenderState state, PoseStack poseStack) {
        poseStack.scale(state.ageScale, state.ageScale, state.ageScale);
    }
}
