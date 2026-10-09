package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import org.jspecify.annotations.Nullable;

/**
 * Sided essentia transport contract implemented by every block able to move essentia through
 * tubes, jars, ports and machines.
 *
 * <p>The interface is published as the block capability {@link EssentiaCapabilities#TRANSPORT},
 * looked up with a nullable {@link Direction} context. It holds no state. All members are called
 * on the logical server thread, except the read-only queries, which client renderers and tooltips
 * may also call.
 *
 * <p>Query methods are pure. The committing operations ({@link #takeEssentia(Holder, int,
 * Direction)}, {@link #addEssentia(Holder, int, Direction)} and {@link #setSuction(Holder, int)})
 * change device state, and the implementation marks its block entity dirty and sends block updates
 * when visible state changes. Simulating calls never change state.
 *
 * @apiNote Tubes pass a {@code null} side to {@link #getSuctionType(Direction)}, {@link
 *     #getSuctionAmount(Direction)}, {@link #getEssentiaType(Direction)} and {@link
 *     #getEssentiaAmount(Direction)} to mean "any side" or "the whole device". Implementations
 *     accept a {@code null} side for those four queries.
 * @since 1.0.0
 */
public interface IEssentiaTransport {
    /**
     * Inserts up to the requested count of an aspect through the given side. The device state
     * changes by exactly the returned count, and the caller keeps responsibility for any refused
     * remainder.
     *
     * @param type the aspect to insert
     * @param count the maximum number of units to insert
     * @param side the side to insert through
     * @return the number of units accepted, between zero and {@code count}
     */
    int addEssentia(Holder<IAspect> type, int count, Direction side);

    /**
     * Removes up to the requested count of an aspect through the given side. The device state
     * changes by exactly the returned count.
     *
     * @param type the aspect to remove
     * @param count the maximum number of units to remove
     * @param side the side to remove through
     * @return the number of units actually removed, between zero and {@code count}
     */
    int takeEssentia(Holder<IAspect> type, int count, Direction side);

    /**
     * Returns the aspect stored or routed on the given side.
     *
     * @param side the side to query, or {@code null} for any aspect the device holds
     * @return the aspect, or {@code null} when empty
     */
    @Nullable
    Holder<IAspect> getEssentiaType(@Nullable Direction side);

    /**
     * Returns how many units are present on the given side.
     *
     * @param side the side to query, or {@code null} for the device-wide total
     * @return the number of units, never negative
     */
    int getEssentiaAmount(@Nullable Direction side);

    /**
     * Replaces the suction of this device. Devices that do not generate suction may ignore the
     * request. An count of zero clears suction entirely.
     *
     * @param type the wanted aspect, or {@code null} for any aspect
     * @param count the suction strength, zero or negative meaning no suction
     */
    void setSuction(@Nullable Holder<IAspect> type, int count);

    /**
     * Returns the aspect this device wants on the given side.
     *
     * @param side the side to query, or {@code null} for the device-wide value
     * @return the wanted aspect, or {@code null} when any aspect is accepted
     */
    @Nullable
    Holder<IAspect> getSuctionType(@Nullable Direction side);

    /**
     * Returns the suction strength on the given side.
     *
     * @param side the side to query, or {@code null} for the device-wide value
     * @return the suction strength, zero when there is none
     */
    int getSuctionAmount(@Nullable Direction side);

    /**
     * Returns the smallest suction strength an outside puller needs to draw essentia out of this
     * device. The value is device-wide, and a puller below it must not extract.
     *
     * @return the minimum suction, zero when any positive suction is enough
     */
    int getMinimumSuction();

    /**
     * Reports whether the given side emits essentia. Transfer helpers refuse a move whose source
     * side is not an outlet.
     *
     * @param side the side to test, never null
     * @return {@code true} when essentia may leave through that side
     */
    boolean canOutputTo(Direction side);

    /**
     * Reports whether the given side accepts incoming essentia. A side may be an inlet, an outlet,
     * both or neither.
     *
     * @param side the side to test, never null
     * @return {@code true} when essentia may enter through that side
     */
    boolean canInputFrom(Direction side);

    /**
     * Reports whether the given side can attach to a tube. Tubes only ask suction questions of
     * sides that are connectable.
     *
     * @param side the side to test, never null
     * @return {@code true} when a tube may connect on that side
     */
    boolean isConnectable(Direction side);

    /**
     * Removes essentia, or rehearses the removal without changing state.
     *
     * <p>When {@code simulate} is {@code false} this behaves exactly like {@link
     * #takeEssentia(Holder, int, Direction)}. When it is {@code true} the result is zero for a
     * non-positive {@code count}, for an empty side, or when the stored aspect differs from the
     * requested one by holder equality. Otherwise it is the smaller of {@code count} and the
     * stored count on that side.
     *
     * @param type the aspect to remove
     * @param count the maximum number of units to remove
     * @param side the side to remove through
     * @param simulate {@code true} to leave the device unchanged
     * @return the number of units removed, or that would be removed
     * @implNote Mixed-aspect stores and devices whose extraction depends on more than the exposed
     *     type and count override this so that a simulation matches the real transfer.
     */
    default int takeEssentia(Holder<IAspect> type, int count, Direction side, boolean simulate) {
        if (simulate) {
            Holder<IAspect> held = count > 0 ? getEssentiaType(side) : null;
            return held != null && held.equals(type) ? Math.min(count, getEssentiaAmount(side)) : 0;
        }
        return takeEssentia(type, count, side);
    }

    /**
     * Inserts essentia, or rehearses the insertion without changing state.
     *
     * <p>When {@code simulate} is {@code false} this behaves exactly like {@link
     * #addEssentia(Holder, int, Direction)}. When it is {@code true} the result is the smaller of
     * {@code count} and {@link #spaceFor(Holder, Direction)}. A non-positive {@code count} is not
     * clamped, so callers guard against it.
     *
     * @param type the aspect to insert
     * @param count the maximum number of units to insert
     * @param side the side to insert through
     * @param simulate {@code true} to leave the device unchanged
     * @return the number of units accepted, or that would be accepted
     * @implNote Devices whose acceptance is not a function of free space override this so that a
     *     simulation matches the real transfer.
     */
    default int addEssentia(Holder<IAspect> type, int count, Direction side, boolean simulate) {
        return simulate ? Math.min(count, spaceFor(type, side)) : addEssentia(type, count, side);
    }

    /**
     * Reports how many more units of an aspect the device could take through the given side.
     * Devices with a real capacity override this and keep it consistent with what {@link
     * #addEssentia(Holder, int, Direction)} accepts.
     *
     * @param type the aspect to test
     * @param side the side to test
     * @return the free space, {@link Integer#MAX_VALUE} by default
     */
    default int spaceFor(Holder<IAspect> type, Direction side) {
        return Integer.MAX_VALUE;
    }
}
