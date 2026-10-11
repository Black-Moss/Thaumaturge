package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;

public interface GolemPartRenderHook {
    GolemPartRenderHook NONE = Noop.INSTANCE;

    private static float unchanged(float rotation) {
        return rotation;
    }

    default void posePiece(String piece, GolemRenderState state, PoseStack pose, GolemPartModel.LimbSide side) {}

    default void decoratePiece(String piece, GolemRenderState state, PoseStack pose, SubmitNodeCollector collector, GolemPartModel.LimbSide side) {}

    default float aimArmPitch(GolemRenderState state, GolemPartModel.LimbSide side, float rigPitch) {
        return unchanged(rigPitch);
    }

    default float aimArmYaw(GolemRenderState state, GolemPartModel.LimbSide side, float rigYaw) {
        return unchanged(rigYaw);
    }

    default float aimArmRoll(GolemRenderState state, GolemPartModel.LimbSide side, float rigRoll) {
        return unchanged(rigRoll);
    }

    enum Noop implements GolemPartRenderHook {
        INSTANCE
    }
}
