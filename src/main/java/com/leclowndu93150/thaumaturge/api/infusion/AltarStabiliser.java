package com.leclowndu93150.thaumaturge.api.infusion;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Lets a block take part in the infusion altar's stability survey as a stabiliser.
 *
 * <p>The survey looks at every column within 8 blocks of the matrix on both horizontal axes, except the matrix column
 * itself, from 3 blocks above the matrix down to 7 blocks below it. A position counts as a stabiliser when its block is
 * in the block tag {@code thaumaturge:infusion_stabilisers}, or when its block implements this interface and
 * {@link #stabilisesNow} returns {@code true}. This test only selects the positions the survey scans as candidates.
 * Tagged blocks that do not implement this interface are worth {@link #STANDARD_PAIR_VALUE} and never ask for a
 * symmetry penalty.
 *
 * <p>Each stabiliser is paired with its mirror, the position reflected through the matrix column at the same height,
 * and each pair is scored once. The mirror is scored by its own values, whether or not it stabilises at the moment
 * of the survey: a mirror block that implements this interface reports its {@link #countsAs identity},
 * {@link #pairValue pair value} and penalty hooks, and any other mirror block is worth {@link #STANDARD_PAIR_VALUE}.
 * <ul>
 * <li>When the two sides do not {@link #countsAs count as} the same block, or their {@link #pairValue pair values}
 * differ, the pair subtracts the larger value and the stabiliser is reported as a problem block. A mirror that does
 * not implement this interface, or that lies in an unloaded chunk, is worth {@link #STANDARD_PAIR_VALUE}.</li>
 * <li>When the sides match and either side {@link #penalisesPairWith penalises the pair}, the pair subtracts the larger
 * of the two {@link #penaltyValue penalty values} and is reported as a problem block.</li>
 * <li>Otherwise the pair adds its value, multiplied by 0.75 for every earlier matched, unpenalised pair of the same
 * block in the same survey.</li>
 * </ul>
 *
 * @apiNote Only {@link #stabilisesNow} has no default, so the smallest implementation overrides that one method. The
 * interface is meant for {@link Block} subclasses; the survey only looks for it on the block at each position.
 * @implNote Every member is called on the server thread that ticks the matrix, for up to several thousand positions
 * per survey. Implementations must be cheap, must only read the world, must not schedule ticks and must not keep the
 * {@link Level} they are given.
 * @since 1.1.0
 */
public interface AltarStabiliser {
    /**
     * Value of one matched pair of ordinary stabilisers before diminishing returns, and what {@link #pairValue} returns
     * by default.
     *
     * @since 1.1.0
     */
    float STANDARD_PAIR_VALUE = 0.1F;

    /**
     * Penalty returned by {@link #penaltyValue} by default.
     *
     * @since 1.1.0
     */
    float NO_PENALTY = 0.0F;

    /**
     * Tests whether the block at the position acts as a stabiliser at the moment of the survey.
     *
     * <p>A block that returns {@code false} is not scanned as a candidate. It can still be scored as the mirror of
     * another candidate, using its own {@link #countsAs identity} and {@link #pairValue pair value}.
     *
     * @param level the level the block is in
     * @param pos the position of this block
     * @return {@code true} when the block counts as a stabiliser right now
     * @since 1.1.0
     */
    boolean stabilisesNow(Level level, BlockPos pos);

    /**
     * Returns the stability one matched pair of this block adds per replenish cycle.
     *
     * <p>Two sides match only when their values are bit-identical, so blocks meant to pair with each other must return
     * exactly the same value.
     *
     * @param level the level the block is in
     * @param pos the position of this block
     * @return a finite, non-negative stability per replenish cycle; {@link #STANDARD_PAIR_VALUE} by default
     * @since 1.1.0
     */
    default float pairValue(Level level, BlockPos pos) {
        return STANDARD_PAIR_VALUE;
    }

    /**
     * Returns the block this stabiliser is treated as when the survey pairs mirrored positions and counts repeats for
     * diminishing returns.
     *
     * <p>A container can report the block it holds so that it pairs and stacks like that block.
     *
     * @param level the level the block is in
     * @param pos the position of this block
     * @return the block to count as, never {@code null}; the block at the position by default
     * @since 1.1.0
     */
    default Block countsAs(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock();
    }

    /**
     * Tests whether the pair this block forms with its mirror is penalised even though both sides already match.
     *
     * <p>The survey asks both sides of a matched pair, each with the other side's position. It never asks about a
     * mismatched pair.
     *
     * @param level the level the block is in
     * @param pos the position of this block
     * @param mirror the position of the mirrored partner
     * @return {@code true} to penalise the pair; {@code false} by default
     * @since 1.1.0
     */
    default boolean penalisesPairWith(Level level, BlockPos pos, BlockPos mirror) {
        return false;
    }

    /**
     * Returns the stability taken away when a pair involving this block is penalised.
     *
     * <p>The survey asks both sides once a penalty was reported and subtracts the larger value.
     *
     * @param level the level the block is in
     * @param pos the position of this block
     * @return a finite, non-negative stability per replenish cycle; {@link #NO_PENALTY} by default
     * @since 1.1.0
     */
    default float penaltyValue(Level level, BlockPos pos) {
        return NO_PENALTY;
    }
}
