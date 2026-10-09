package com.leclowndu93150.thaumaturge.api.infusion;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Declares a block as a stabiliser for the infusion matrix and tunes how it is counted in the
 * stability survey.
 *
 * <p>The survey inspects a 17 x 17 footprint of columns around the matrix, with the matrix column
 * itself excluded, from 3 blocks above the matrix down to 7 blocks below it. A block counts as a
 * stabiliser when it is in the block tag {@code thaumaturge:infusion_stabilisers}, or when it
 * implements this interface and {@link #canStabiliseInfusion} returns {@code true} at its
 * position. The tag check runs first. Tagged blocks that do not implement this interface use
 * {@link #DEFAULT_STABILIZATION} and never report a symmetry penalty.
 *
 * <p>Stabilisers are scored in mirrored pairs, where the partner of a position is its point
 * reflection across the matrix column at equal height. Two sides match when their
 * {@link #stabiliserIdentity identities} are the same block and their
 * {@link #getStabilizationAmount amounts} are bit-identical. Each pair is scored once:
 * <ul>
 * <li>a mismatched pair subtracts the larger of the two amounts and is reported as a problem
 * block</li>
 * <li>a matched pair with no symmetry penalty adds its amount, multiplied by 0.75 raised to the
 * number of earlier matched, unpenalised pairs of the same identity in the same survey</li>
 * <li>a matched pair where either side reports a {@link #hasSymmetryPenalty symmetry penalty}
 * subtracts the larger of the two {@link #getSymmetryPenalty penalties} and is reported as a
 * problem block</li>
 * </ul>
 *
 * <p>A stabiliser whose mirrored position holds no stabiliser counts as a mismatch, with the
 * missing side contributing {@link #DEFAULT_STABILIZATION}.
 *
 * @apiNote Only {@link #canStabiliseInfusion} has no default, so a minimal implementor overrides
 * that method alone. Implementations are intended for {@link Block} subclasses.
 * @implNote Every member is called from the server tick thread of the matrix, potentially for up
 * to 16 x 16 x 11 positions per survey. Implementations must be cheap, must read world state only,
 * must not mutate the world or schedule ticks, and must not retain the passed {@link Level}.
 * @since 1.0.0
 */
public interface IInfusionStabiliser {
    /**
     * Stability restored per replenish cycle by one matched pair of default stabilisers, before
     * diminishing returns. Equals the value {@link #getStabilizationAmount} returns by default.
     *
     * @since 1.0.0
     */
    float DEFAULT_STABILIZATION = 0.1F;

    /**
     * Symmetry penalty returned by {@link #getSymmetryPenalty} by default.
     *
     * @since 1.0.0
     */
    float NO_SYMMETRY_PENALTY = 0.0F;

    /**
     * Tests whether the block at the position counts as a stabiliser right now.
     *
     * <p>State-dependent blocks return {@code false} while they cannot stabilise, and are then
     * ignored by the survey entirely: they neither add nor subtract.
     *
     * @param level the level containing the block
     * @param pos the position of this block itself
     * @return {@code true} if the block counts toward the stability survey
     * @since 1.0.0
     */
    boolean canStabiliseInfusion(Level level, BlockPos pos);

    /**
     * Returns the stability this block contributes per replenish cycle when it is part of a
     * matched pair.
     *
     * <p>Two blocks pair only when the amounts they return are bit-identical, so implementors
     * meant to pair with each other must return identical values.
     *
     * @param level the level containing the block
     * @param pos the position of this block itself
     * @return a finite, non-negative amount in stability per replenish cycle; defaults to
     * {@link #DEFAULT_STABILIZATION}
     * @since 1.0.0
     */
    default float getStabilizationAmount(Level level, BlockPos pos) {
        return DEFAULT_STABILIZATION;
    }

    /**
     * Returns the block this stabiliser counts as when the survey pairs mirrored positions and
     * groups positions for diminishing returns.
     *
     * <p>A container may report the block it holds, so that it pairs and stacks exactly like that
     * block. The default is the block at the position.
     *
     * @param level the level containing the block
     * @param pos the position of this block itself
     * @return the identity block, never {@code null}
     * @since 1.0.0
     */
    default Block stabiliserIdentity(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock();
    }

    /**
     * Tests whether the pair formed with the mirrored position must be penalised even though
     * identity and amount already matched.
     *
     * <p>The survey asks once per ordered direction of a pair, giving each side the other side's
     * position as {@code mirror}. The survey never asks about a pair whose sides do not match.
     *
     * @param level the level containing the block
     * @param pos the position of this block itself
     * @param mirror the position of the mirrored partner
     * @return {@code true} if the pair must be penalised; defaults to {@code false}
     * @since 1.0.0
     */
    default boolean hasSymmetryPenalty(Level level, BlockPos pos, BlockPos mirror) {
        return false;
    }

    /**
     * Returns the stability subtracted when this block reported a symmetry penalty.
     *
     * <p>The survey asks only after a penalty was reported, for both members of the pair, and
     * subtracts the larger of the two values.
     *
     * @param level the level containing the block
     * @param pos the position of this block itself
     * @return a finite, non-negative penalty in stability per replenish cycle; defaults to
     * {@code 0}
     * @since 1.0.0
     */
    default float getSymmetryPenalty(Level level, BlockPos pos) {
        return NO_SYMMETRY_PENALTY;
    }
}
