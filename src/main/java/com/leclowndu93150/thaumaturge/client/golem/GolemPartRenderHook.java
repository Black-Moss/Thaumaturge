package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;

public interface GolemPartRenderHook {
    GolemPartRenderHook NONE = Noop.INSTANCE;

    private static float unchanged(float rotation) {
        return rotation;
    }

    default float armRotationZ(GolemRenderState golem, GolemPartModel.LimbSide limb, float current) {
        return unchanged(current);
    }

    default float armRotationY(GolemRenderState golem, GolemPartModel.LimbSide limb, float current) {
        return unchanged(current);
    }

    default float armRotationX(GolemRenderState golem, GolemPartModel.LimbSide limb, float current) {
        return unchanged(current);
    }

    default void postRenderObjectPart(String partName, GolemRenderState state, PoseStack poseStack, SubmitNodeCollector collector, GolemPartModel.LimbSide side) {}

    default void preRenderObjectPart(String partName, GolemRenderState state, PoseStack poseStack, GolemPartModel.LimbSide side, float partialTick) {}

    enum Noop implements GolemPartRenderHook {
        INSTANCE
    }
}
