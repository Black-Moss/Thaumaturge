package com.leclowndu93150.thaumaturge.content.aura.relay;

import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.VisRelayHelper;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityJarNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.transfer.transaction.RootCommitJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class VisRelayNetwork implements VisRelayHelper.Bindings {
    public static final int CONSUMER_RANGE = 8;

    @Override
    public int drainCentivis(ServerLevel level, BlockPos consumerPos, ResourceKey<IAspect> primal, int amount, boolean simulate) {
        Tap tap = tap(level, consumerPos, primal, amount);
        if (tap == null) {
            return 0;
        }
        if (simulate) {
            return clamp(tap.source().source().availableCentivis(primal), amount);
        }
        int drained = drainNow(tap.source(), primal, amount);
        if (drained > 0) {
            tap.relay().triggerConsumeEffect(level, tap.aspect());
        }
        return drained;
    }

    @Override
    public int drainCentivis(ServerLevel level, BlockPos consumerPos, ResourceKey<IAspect> primal, int amount, TransactionContext transaction) {
        Tap tap = tap(level, consumerPos, primal, amount);
        if (tap == null) {
            return 0;
        }
        int drained = clamp(tap.source().source().drainCentivis(primal, amount, transaction), amount);
        if (drained > 0) {
            new RootCommitJournal(() -> tap.relay().triggerConsumeEffect(level, tap.aspect())).updateSnapshots(transaction);
        }
        return drained;
    }

    public static int drainNow(LinkedRelaySource source, ResourceKey<IAspect> primal, int amount) {
        if (amount <= 0) {
            return 0;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int drained = clamp(source.source().drainCentivis(primal, amount, transaction), amount);
            transaction.commit();
            return drained;
        }
    }

    public static int drainEverySourceNear(ServerLevel level, BlockPos consumerPos, ResourceKey<IAspect> primal, int amount) {
        if (amount <= 0) {
            return 0;
        }
        Holder<IAspect> aspect = Aspects.resolve(level.registryAccess(), primal);
        Set<BlockPos> drainedSources = new HashSet<>();
        int drained = 0;
        for (BlockEntityVisRelay relay : blockEntitiesNear(level, consumerPos, BlockEntityVisRelay.class)) {
            if (drained >= amount) {
                break;
            }
            if (!relay.isLinked()) {
                continue;
            }
            LinkedRelaySource source = relay.resolveSource(level);
            if (source == null || !drainedSources.add(source.position())) {
                continue;
            }
            int taken = drainNow(source, primal, amount - drained);
            if (taken > 0) {
                drained += taken;
                if (aspect != null) {
                    relay.triggerConsumeEffect(level, aspect);
                }
            }
        }
        return drained;
    }

    public static int drainNodesNear(ServerLevel level, BlockPos consumerPos, ResourceKey<IAspect> primal, int amount) {
        int drained = 0;
        for (BlockEntityNode node : blockEntitiesNear(level, consumerPos, BlockEntityNode.class)) {
            if (drained >= amount) {
                break;
            }
            if (node instanceof BlockEntityJarNode) {
                continue;
            }
            drained += drainNow(new LinkedRelaySource(node.getBlockPos(), node.relaySource()), primal, amount - drained);
        }
        return drained;
    }

    private static int clamp(int supplied, int amount) {
        return Math.max(0, Math.min(amount, supplied));
    }

    private static @Nullable Tap tap(ServerLevel level, BlockPos consumerPos, ResourceKey<IAspect> primal, int amount) {
        if (amount <= 0) {
            return null;
        }
        BlockEntityVisRelay relay = findRelayNear(level, consumerPos);
        LinkedRelaySource source = relay == null ? null : relay.resolveSource(level);
        if (source == null) {
            return null;
        }
        Holder<IAspect> aspect = Aspects.resolve(level.registryAccess(), primal);
        return aspect == null ? null : new Tap(relay, source, aspect);
    }

    public static @Nullable BlockEntityVisRelay findRelayNear(ServerLevel level, BlockPos consumerPos) {
        BlockEntityVisRelay best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockEntityVisRelay relay : blockEntitiesNear(level, consumerPos, BlockEntityVisRelay.class)) {
            if (!relay.isLinked()) {
                continue;
            }
            double distance = relay.getBlockPos().distSqr(consumerPos);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = relay;
            }
        }
        return best;
    }

    private static <T extends BlockEntity> List<T> blockEntitiesNear(ServerLevel level, BlockPos center, Class<T> type) {
        List<T> found = new ArrayList<>();
        int minChunkX = SectionPos.blockToSectionCoord(center.getX() - CONSUMER_RANGE);
        int maxChunkX = SectionPos.blockToSectionCoord(center.getX() + CONSUMER_RANGE);
        int minChunkZ = SectionPos.blockToSectionCoord(center.getZ() - CONSUMER_RANGE);
        int maxChunkZ = SectionPos.blockToSectionCoord(center.getZ() + CONSUMER_RANGE);
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    BlockPos pos = blockEntity.getBlockPos();
                    if (type.isInstance(blockEntity) && !blockEntity.isRemoved() && Math.abs(pos.getX() - center.getX()) <= CONSUMER_RANGE && Math.abs(pos.getY() - center.getY()) <= CONSUMER_RANGE
                            && Math.abs(pos.getZ() - center.getZ()) <= CONSUMER_RANGE) {
                        found.add(type.cast(blockEntity));
                    }
                }
            }
        }
        return found;
    }

    private record Tap(BlockEntityVisRelay relay, LinkedRelaySource source, Holder<IAspect> aspect) {
    }
}
