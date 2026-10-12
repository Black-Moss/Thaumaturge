package com.leclowndu93150.thaumaturge.api.client.golems;

import net.minecraft.client.renderer.item.ItemStackRenderState;

/**
 * What a {@link GolemPartRenderHook} can read about the golem being drawn. The view describes one
 * frame of one golem and is only valid for the duration of the hook call that receives it; hooks must
 * not keep it. Client only.
 *
 * <p>When a golem is drawn as an item (in an inventory or the golem press preview), the golem is at
 * rest: the swing progress, age and wheel rotation are zero, it is not fighting and its second hand
 * is empty.
 *
 * @since 1.1.0
 */
public interface GolemPartRenderView {
    /**
     * The progress of the golem's current arm swing.
     *
     * @return the swing progress, from 0 when not swinging up to 1 at the end of a swing
     * @since 1.1.0
     */
    float swingProgress();

    /**
     * The golem's age, including the partial tick, for continuous animation.
     *
     * @return the age in ticks
     * @since 1.1.0
     */
    float ageInTicks();

    /**
     * The golem's head pitch, interpolated for the frame. Negative values look up.
     *
     * @return the pitch in degrees
     * @since 1.1.0
     */
    float headPitch();

    /**
     * Whether the golem is currently fighting a target.
     *
     * @return true while the golem is in combat
     * @since 1.1.0
     */
    boolean fighting();

    /**
     * How far a wheel of half a block diameter has turned from the distance the golem has moved along
     * its facing. Moving forward increases it and moving backward decreases it.
     *
     * @return the wheel rotation in degrees, wrapped to the range -180 to 180
     * @since 1.1.0
     */
    float wheelRotation();

    /**
     * The packed light at the golem, for anything a hook submits itself.
     *
     * @return the packed light coordinates
     * @since 1.1.0
     */
    int lightCoords();

    /**
     * The outline colour the golem is drawn with, for anything a hook submits itself.
     *
     * @return the ARGB outline colour, or 0 when the golem has no outline
     * @since 1.1.0
     */
    int outlineColor();

    /**
     * Whether the golem holds an item in its second hand.
     *
     * @return true when {@link #secondHandItem()} has something to draw
     * @since 1.1.0
     */
    boolean hasSecondHandItem();

    /**
     * Whether the item in the golem's second hand is a block item.
     *
     * @return true when the second hand item places a block
     * @since 1.1.0
     */
    boolean secondHandItemIsBlock();

    /**
     * The item in the golem's second hand, resolved for the fixed display context. Submit it with
     * {@link ItemStackRenderState#submit}; do not modify it.
     *
     * @return the item render state, empty when the hand is empty
     * @since 1.1.0
     */
    ItemStackRenderState secondHandItem();
}
