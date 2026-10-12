package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import org.jspecify.annotations.Nullable;

/**
 * A block that takes part in essentia transport: tubes, jars, ports, buffers and every machine that
 * gives or takes essentia through its sides.
 *
 * <p>Each of the six sides answers the same questions on its own. A side may {@linkplain
 * #connectsOn connect} to a neighbour, {@linkplain #receivesOn receive} essentia, {@linkplain
 * #releasesOn release} essentia, exert a {@linkplain #pull pull}, and {@linkplain #heldAspect
 * hold} essentia it would give. Two blocks only interact when each connects on the side facing the
 * other.
 *
 * <p>Flow is always pulled, never pushed. A block draws from a neighbour when its own pull on the
 * touching side is stronger than the neighbour's pull on that side, the neighbour releases on that
 * side and holds essentia, and the drawing pull is at least the neighbour's {@linkplain
 * #pullThreshold pull threshold}. A pull that names an aspect only draws that aspect.
 *
 * <p>The interface is published as the sided block capability {@link
 * EssentiaCapabilities#CONDUIT}; look it up with {@link EssentiaAccess#conduit}. Every member is
 * called on the logical server thread, except the read-only queries, which client renderers and
 * tooltips may also call. Queries never change state. A committing {@link #insert} or {@link
 * #extract} changes the device by exactly the returned amount, marks the block entity dirty, and
 * sends a block update when the visible state changes. A simulating call never changes state.
 *
 * @apiNote Callers may pass a {@code null} side to {@link #heldAspect}, {@link #heldAmount} and
 *     {@link #pull} to mean "the whole device" rather than one face; implementations accept it.
 * @since 1.1.0
 */
public interface IEssentiaConduit {
    /**
     * Inserts up to {@code amount} units of an aspect through a side, or rehearses the insertion.
     *
     * <p>A simulation returns exactly what the same committing call would accept at this moment.
     * The caller keeps responsibility for any refused remainder.
     *
     * @param aspect   the aspect to insert
     * @param amount   the most units to insert; zero or negative inserts nothing
     * @param side     the side the essentia enters through
     * @param simulate {@code true} to leave the device unchanged
     * @return the units accepted, or that would be accepted, between zero and {@code amount}
     * @since 1.1.0
     */
    int insert(Holder<IAspect> aspect, int amount, Direction side, boolean simulate);

    /**
     * Removes up to {@code amount} units of an aspect through a side, or rehearses the removal.
     *
     * <p>A simulation returns exactly what the same committing call would release at this moment.
     *
     * @param aspect   the aspect to remove
     * @param amount   the most units to remove; zero or negative removes nothing
     * @param side     the side the essentia leaves through
     * @param simulate {@code true} to leave the device unchanged
     * @return the units removed, or that would be removed, between zero and {@code amount}
     * @since 1.1.0
     */
    int extract(Holder<IAspect> aspect, int amount, Direction side, boolean simulate);

    /**
     * Returns the aspect this device would give through a side.
     *
     * @param side the side to query, or {@code null} for any aspect the device holds
     * @return the aspect, or {@code null} when nothing is held there
     * @apiNote A device holding several aspects may name a different one on each call.
     * @since 1.1.0
     */
    @Nullable
    Holder<IAspect> heldAspect(@Nullable Direction side);

    /**
     * Returns how many units this device holds behind a side.
     *
     * @param side the side to query, or {@code null} for the device-wide total
     * @return the units held, never negative
     * @since 1.1.0
     */
    int heldAmount(@Nullable Direction side);

    /**
     * Returns the pull this device exerts through a side.
     *
     * @param side the side to query, or {@code null} for the device-wide pull
     * @return the pull, {@link EssentiaPull#NONE} when there is none; never {@code null}
     * @since 1.1.0
     */
    EssentiaPull pull(@Nullable Direction side);

    /**
     * Returns the weakest pull a neighbour needs before it may draw essentia out of this device.
     * The value is device-wide. It is what keeps tubes from draining one full container into
     * another of the same kind.
     *
     * @return the pull threshold, zero when any active pull is enough
     * @since 1.1.0
     */
    int pullThreshold();

    /**
     * Reports whether essentia may leave through a side. Transfers whose source side does not
     * release move nothing.
     *
     * @param side the side to test, never {@code null}
     * @return {@code true} when essentia may leave through that side
     * @since 1.1.0
     */
    boolean releasesOn(Direction side);

    /**
     * Reports whether essentia may enter through a side. A side may receive, release, both or
     * neither.
     *
     * @param side the side to test, never {@code null}
     * @return {@code true} when essentia may enter through that side
     * @since 1.1.0
     */
    boolean receivesOn(Direction side);

    /**
     * Reports whether a neighbour on a side links to this device: tubes draw a connection to it and
     * flow through that side is possible at all.
     *
     * @param side the side to test, never {@code null}
     * @return {@code true} when a neighbour may connect on that side
     * @since 1.1.0
     */
    boolean connectsOn(Direction side);

    /**
     * Returns how many more units of an aspect the device could take through a side. Devices with a
     * real capacity override this and keep it consistent with what {@link #insert} accepts.
     *
     * @param aspect the aspect to test
     * @param side   the side to test
     * @return the free room, {@link Integer#MAX_VALUE} by default
     * @since 1.1.0
     */
    default int roomFor(Holder<IAspect> aspect, Direction side) {
        return Integer.MAX_VALUE;
    }
}
