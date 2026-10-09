package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.network.ClientboundSealPayload;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public final class SealHandler {
    private static final int VALIDATION_INTERVAL_TICKS = 20;
    private static final double BLOCK_CENTER = 0.5;
    private static final double DROP_OFFSET = 1.0 / 1.7;

    private SealHandler() {}

    public static @Nullable SealEntity lookup(Level level, @Nullable SealPos pos) {
        if (pos != null) {
            if (level instanceof ServerLevel serverLevel) {
                return index(serverLevel).seals().get(pos);
            }
            if (level.isClientSide()) {
                return ClientSealHolder.get(pos);
            }
        }
        return null;
    }

    public static void withdraw(ServerLevel level, SealPos pos, boolean quiet) {
        SealEntity seal = index(level).seals().remove(pos);
        if (seal == null) {
            return;
        }
        BlockPos block = pos.pos();
        LevelChunk chunk = loadedChunk(level, block);
        if (chunk != null) {
            chunkSeals(chunk).remove(seal);
            chunk.markUnsaved();
        }
        seal.behavior().onRemoved(level, seal);
        TaskBoard.of(level).endAllFrom(pos);
        PacketDistributor.sendToPlayersTrackingChunk(level, ChunkPos.containing(block), ClientboundSealPayload.remove(pos));
        if (!quiet) {
            dropPlacer(level, pos, seal);
        }
    }

    private static void dropPlacer(ServerLevel level, SealPos pos, SealEntity seal) {
        BlockPos block = pos.pos();
        Direction face = pos.face();
        ItemEntity item = new ItemEntity(level, block.getX() + BLOCK_CENTER + face.getStepX() * DROP_OFFSET, block.getY() + BLOCK_CENTER + face.getStepY() * DROP_OFFSET,
                block.getZ() + BLOCK_CENTER + face.getStepZ() * DROP_OFFSET, new ItemStack(seal.type().placer()));
        item.setDefaultPickUpDelay();
        level.addFreshEntity(item);
    }

    public static List<SealEntity> chunkSeals(LevelChunk chunk) {
        return chunk.getData(TTAttachments.SEALS).seals();
    }

    public static boolean register(ServerLevel level, SealEntity seal) {
        Map<SealPos, SealEntity> registry = index(level).seals();
        if (registry.containsKey(seal.pos())) {
            return false;
        }
        registry.put(seal.pos(), seal);
        LevelChunk chunk = level.getChunkAt(seal.pos().pos());
        attachToChunk(chunk, seal);
        chunk.markUnsaved();
        seal.markChanged(level);
        return true;
    }

    private static void attachToChunk(LevelChunk chunk, SealEntity seal) {
        List<SealEntity> held = chunkSeals(chunk);
        if (held.stream().noneMatch(existing -> existing == seal)) {
            held.add(seal);
        }
    }

    public static boolean place(ServerLevel level, SealPos pos, Identifier typeId, SealType type, Player player) {
        boolean free = lookup(level, pos) == null;
        return free && register(level, SealEntity.place(pos, typeId, type, player.getUUID()));
    }

    public static void loadChunkSeals(ServerLevel level, LevelChunk chunk) {
        SealWorldIndex index = index(level);
        for (SealEntity seal : chunkSeals(chunk)) {
            index.seals().putIfAbsent(seal.pos(), seal);
        }
    }

    public static void unloadChunkSeals(ServerLevel level, LevelChunk chunk) {
        SealWorldIndex index = index(level);
        for (SealEntity seal : chunkSeals(chunk)) {
            index.seals().remove(seal.pos(), seal);
        }
    }

    public static void runTicks(ServerLevel level) {
        boolean validate = level.getGameTime() % VALIDATION_INTERVAL_TICKS == 0;
        for (SealEntity seal : index(level).seals().values()) {
            SealPos pos = seal.pos();
            if (!level.hasChunkAt(pos.pos())) {
                continue;
            }
            try {
                if (validate && !seal.type().placement().allows(level, pos.pos(), pos.face())) {
                    withdraw(level, pos, false);
                    continue;
                }
                seal.tick(level);
            } catch (RuntimeException exception) {
                Thaumaturge.LOGGER.error("Seal at {} failed and was removed", pos, exception);
                withdraw(level, pos, false);
            }
        }
    }

    public static List<SealEntity> within(ServerLevel level, BlockPos origin, int range) {
        long limitSqr = (long) range * range;
        List<SealEntity> found = new ArrayList<>();
        index(level).seals().forEach((key, candidate) -> {
            if (key.pos().distSqr(origin) <= limitSqr) {
                found.add(candidate);
            }
        });
        return found;
    }

    public static void flagUnsaved(ServerLevel level, BlockPos pos) {
        Optional.ofNullable(loadedChunk(level, pos)).ifPresent(LevelChunk::markUnsaved);
    }

    public static boolean isSealOwner(SealEntity seal, UUID id) {
        UUID owner = seal.owner();
        return owner != null && owner.equals(id);
    }

    private static SealWorldIndex index(ServerLevel level) {
        return level.getData(TTAttachments.SEAL_INDEX);
    }

    private static @Nullable LevelChunk loadedChunk(ServerLevel level, BlockPos pos) {
        return level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }
}
