package com.leclowndu93150.thaumaturge.api.client.golems;

import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;

/**
 * Animates the mesh of a golem part model. Attached to a mesh location through
 * {@link RegisterGolemPartRenderHooksEvent}; a mesh without a hook is drawn as it is. Client only.
 *
 * <p>Every method has a default that changes nothing, so a hook overrides only what it needs. Hooks
 * are called on the render thread for every golem that wears a part using the mesh, including golems
 * drawn as items, so they must not keep per-golem state; everything a hook knows about the golem comes
 * from the {@link GolemPartRenderView} it is handed.
 *
 * <p>Arm and leg models are drawn twice, once per side, and the {@link GolemPartModel.LimbSide} tells
 * the hook which copy it is working on. Head and body models are drawn once with
 * {@link GolemPartModel.LimbSide#MIDDLE}.
 *
 * @since 1.1.0
 */
public interface GolemPartRenderHook {
    /**
     * Adjusts the placement of one named piece of the mesh just before it is drawn, for example to open
     * a jaw or spin a wheel. Called for every piece of the mesh in every visible pass, including the pass
     * that shows golems through walls.
     *
     * @param piece the name of the mesh object group about to be drawn
     * @param golem the golem being drawn
     * @param pose  the pose the piece is drawn with; it is pushed before the call and popped after the
     *              piece is drawn, so the hook transforms it directly and must not pop it
     * @param side  which copy of a paired limb model is drawn, or {@code MIDDLE} for an unpaired model
     * @since 1.1.0
     */
    default void posePiece(String piece, GolemPartRenderView golem, PoseStack pose, GolemPartModel.LimbSide side) {}

    /**
     * Submits extra geometry right after one named piece of the mesh is drawn, for example an item
     * carried in a basket. The pose still carries any change made by {@link #posePiece}. Not called for
     * the pass that shows golems through walls.
     *
     * @param piece     the name of the mesh object group that was just drawn
     * @param golem     the golem being drawn
     * @param pose      the pose the piece was drawn with; anything pushed must be popped before returning
     * @param collector the collector to submit geometry to
     * @param side      which copy of a paired limb model was drawn, or {@code MIDDLE} for an unpaired model
     * @since 1.1.0
     */
    default void decoratePiece(String piece, GolemPartRenderView golem, PoseStack pose, SubmitNodeCollector collector, GolemPartModel.LimbSide side) {}

    /**
     * Changes the pitch the golem's arm is drawn at. Called once per frame for each arm of a golem that
     * wears a part using the mesh, whatever the model's attach point, after the body rig has posed the
     * arms. When several worn models carry hooks, each receives the result of the previous one.
     *
     * @param golem    the golem being drawn
     * @param side     {@code LEFT} or {@code RIGHT}
     * @param rigPitch the pitch the arm would otherwise be drawn at, in radians
     * @return the pitch to draw the arm at, in radians
     * @since 1.1.0
     */
    default float aimArmPitch(GolemPartRenderView golem, GolemPartModel.LimbSide side, float rigPitch) {
        return rigPitch;
    }

    /**
     * Changes the yaw the golem's arm is drawn at. Called alongside {@link #aimArmPitch}.
     *
     * @param golem  the golem being drawn
     * @param side   {@code LEFT} or {@code RIGHT}
     * @param rigYaw the yaw the arm would otherwise be drawn at, in radians
     * @return the yaw to draw the arm at, in radians
     * @since 1.1.0
     */
    default float aimArmYaw(GolemPartRenderView golem, GolemPartModel.LimbSide side, float rigYaw) {
        return rigYaw;
    }

    /**
     * Changes the roll the golem's arm is drawn at. Called alongside {@link #aimArmPitch}.
     *
     * @param golem   the golem being drawn
     * @param side    {@code LEFT} or {@code RIGHT}
     * @param rigRoll the roll the arm would otherwise be drawn at, in radians
     * @return the roll to draw the arm at, in radians
     * @since 1.1.0
     */
    default float aimArmRoll(GolemPartRenderView golem, GolemPartModel.LimbSide side, float rigRoll) {
        return rigRoll;
    }
}
