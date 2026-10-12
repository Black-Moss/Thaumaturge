package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.api.infusion.AltarStabiliser;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class InfusionStabilitySurvey {
    static final int HALF_WIDTH = 8;
    static final int HEIGHT_ABOVE = 3;
    static final int DEPTH_BELOW = 7;
    private static final float DIMINISHING_FACTOR = 0.75F;

    private InfusionStabilitySurvey() {}

    public static Result survey(Level level, BlockPos matrix) {
        Set<BlockPos> settled = new HashSet<>();
        Map<Block, Integer> ranks = new HashMap<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        float total = 0.0F;
        for (int dx = -HALF_WIDTH; dx <= HALF_WIDTH; dx++) {
            for (int dz = -HALF_WIDTH; dz <= HALF_WIDTH; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                for (int dy = HEIGHT_ABOVE; dy >= -DEPTH_BELOW; dy--) {
                    cursor.set(matrix.getX() + dx, matrix.getY() + dy, matrix.getZ() + dz);
                    if (!isStabiliser(level, cursor) || settled.contains(cursor)) {
                        continue;
                    }
                    BlockPos pos = cursor.immutable();
                    BlockPos mirror = new BlockPos(matrix.getX() - dx, pos.getY(), matrix.getZ() - dz);
                    settled.add(pos);
                    settled.add(mirror);
                    total += scorePair(level, pos, mirror, ranks);
                }
            }
        }
        return new Result(total);
    }

    public static boolean isStabiliser(Level level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return state.is(TTBlockTags.INFUSION_STABILISERS) || state.getBlock() instanceof AltarStabiliser behaviour && behaviour.stabilisesNow(level, pos);
    }

    private static float scorePair(Level level, BlockPos pos, BlockPos mirror, Map<Block, Integer> ranks) {
        Profile near = profile(level, pos);
        Profile far = profile(level, mirror);
        if (near.identity() != far.identity() || near.amount() != far.amount()) {
            return -Math.max(near.amount(), far.amount());
        }
        if (penalised(level, near, pos, mirror) || penalised(level, far, mirror, pos)) {
            return -Math.max(penaltyOf(level, near, pos), penaltyOf(level, far, mirror));
        }
        int rank = ranks.merge(near.identity(), 1, Integer::sum) - 1;
        return near.amount() * (float) Math.pow(DIMINISHING_FACTOR, rank);
    }

    private static boolean penalised(Level level, Profile profile, BlockPos pos, BlockPos other) {
        AltarStabiliser behaviour = profile.behaviour();
        return behaviour != null && behaviour.penalisesPairWith(level, pos, other);
    }

    private static float penaltyOf(Level level, Profile profile, BlockPos pos) {
        AltarStabiliser behaviour = profile.behaviour();
        return behaviour == null ? 0.0F : behaviour.penaltyValue(level, pos);
    }

    private static Profile profile(Level level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) {
            return new Profile(Blocks.AIR, AltarStabiliser.STANDARD_PAIR_VALUE, null);
        }
        Block block = level.getBlockState(pos).getBlock();
        if (block instanceof AltarStabiliser behaviour) {
            return new Profile(normalise(behaviour.countsAs(level, pos)), behaviour.pairValue(level, pos), behaviour);
        }
        return new Profile(normalise(block), AltarStabiliser.STANDARD_PAIR_VALUE, null);
    }

    private static Block normalise(Block block) {
        return block instanceof AbstractSkullBlock ? Blocks.SKELETON_SKULL : block;
    }

    private record Profile(Block identity, float amount, @Nullable AltarStabiliser behaviour) {
    }

    public record Result(float stabilityReplenish) {
    }
}
