package com.leclowndu93150.thaumcraft.api.essentia;

import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;

/**
 * Sided essentia transport contract for blocks that can move essentia between neighbors.
 *
 * <p>Sides are given as {@link Direction} and aspects as {@code Holder<IAspect>} so that
 * implementations remain valid across datapack reloads.
 *
 * <p>Implementations expose the capability through {@link EssentiaCapabilities#TRANSPORT}.
 *
 * @since 1.0.0
 */
public interface IEssentiaTransport {
    /**
     * Whether this block can connect to a tube on the given side.
     *
     * @param face the side queried, never {@code null}
     * @return {@code true} when the side participates in essentia transport
     */
    boolean isConnectable(Direction face);

    /**
     * Whether the given side accepts incoming essentia.
     *
     * @param face the side queried, never {@code null}
     * @return {@code true} when the side is an inlet
     */
    boolean canInputFrom(Direction face);

    /**
     * Whether the given side emits essentia.
     *
     * @param face the side queried, never {@code null}
     * @return {@code true} when the side is an outlet
     */
    boolean canOutputTo(Direction face);

    /**
     * Sets the suction state for this device. Implementations may ignore the request when they
     * do not generate suction.
     *
     * @param aspect the aspect to draw, or {@code null} to clear
     * @param amount the suction strength
     */
    void setSuction(Holder<IAspect> aspect, int amount);

    /**
     * The aspect this device tries to draw on the given side, or {@code null} for any.
     *
     * @param face the side queried, never {@code null}
     * @return the aspect holder, or {@code null} when undirected
     */
    Holder<IAspect> getSuctionType(Direction face);

    /**
     * The suction strength on the given side.
     *
     * @param face the side queried, never {@code null}
     * @return the suction amount; zero or negative means no suction
     */
    int getSuctionAmount(Direction face);

    /**
     * Removes up to {@code amount} of {@code aspect} from this device through the given side.
     *
     * @param aspect the aspect requested
     * @param amount the maximum amount to extract
     * @param face   the side through which extraction happens
     * @return the amount actually removed
     */
    int takeEssentia(Holder<IAspect> aspect, int amount, Direction face);

    /**
     * Inserts up to {@code amount} of {@code aspect} into this device through the given side.
     *
     * @param aspect the aspect supplied
     * @param amount the maximum amount to insert
     * @param face   the side through which insertion happens
     * @return the amount accepted
     */
    int addEssentia(Holder<IAspect> aspect, int amount, Direction face);

    /**
     * The aspect currently stored or routed on the given side.
     *
     * @param face the side queried, never {@code null}
     * @return the aspect holder, or {@code null} when empty
     */
    Holder<IAspect> getEssentiaType(Direction face);

    /**
     * The amount of essentia currently stored or available on the given side.
     *
     * @param face the side queried, never {@code null}
     * @return the amount, never negative
     */
    int getEssentiaAmount(Direction face);

    /**
     * The minimum suction required by external pumps to draw essentia from this device.
     *
     * @return the minimum suction threshold
     */
    int getMinimumSuction();
}
