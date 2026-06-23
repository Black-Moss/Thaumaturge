package com.leclowndu93150.thaumcraft.api.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/**
 * Static accessor for per-chunk aura records.
 *
 * <p>The accessor is bound at mod init by the implementation. Addons use {@link #of(ServerLevel, ChunkPos)}
 * or {@link #of(Level, BlockPos)} to read a chunk's aura without depending on the internal attachment
 * registry.
 *
 * @since 1.0.0
 */
public final class AuraAccess {
    private static ChunkBinding chunkBinding;
    private static BlockBinding blockBinding;

    private AuraAccess() {}

    /**
     * Returns the aura record for the given chunk on the server side.
     *
     * @param level the server level
     * @param pos   the chunk position
     * @return the aura record, never null; freshly-created entries report base/vis/flux as zero
     *         until generation has populated them
     * @throws IllegalStateException when called before the implementation has bound the accessor
     */
    public static IAuraChunk of(ServerLevel level, ChunkPos pos) {
        if (chunkBinding == null) {
            throw new IllegalStateException("AuraAccess accessed before binding");
        }
        return chunkBinding.apply(level, pos);
    }

    /**
     * Returns the aura record for the chunk containing the given block position. Resolves the
     * chunk by right-shifting the block coordinates by four.
     *
     * @param level the level; must be a server level at runtime
     * @param pos   a block position inside the target chunk
     * @return the aura record, never null
     * @throws IllegalStateException when called before the implementation has bound the accessor
     */
    public static IAuraChunk of(Level level, BlockPos pos) {
        if (blockBinding == null) {
            throw new IllegalStateException("AuraAccess accessed before binding");
        }
        return blockBinding.apply(level, pos);
    }

    /**
     * Binds the accessor's implementation. Called once at mod init by the implementation; addons
     * must not call this.
     *
     * @param chunk implementation for the chunk-pos variant
     * @param block implementation for the block-pos variant
     * @throws IllegalStateException when already bound
     */
    public static void bind(ChunkBinding chunk, BlockBinding block) {
        if (chunkBinding != null) {
            throw new IllegalStateException("AuraAccess already bound");
        }
        chunkBinding = chunk;
        blockBinding = block;
    }

    /**
     * Implementation hook for {@link #of(ServerLevel, ChunkPos)}.
     *
     * @since 1.0.0
     */
    @FunctionalInterface
    public interface ChunkBinding {
        /**
         * Resolves the aura record for a chunk.
         *
         * @param level the server level
         * @param pos   the chunk position
         * @return the aura record
         */
        IAuraChunk apply(ServerLevel level, ChunkPos pos);
    }

    /**
     * Implementation hook for {@link #of(Level, BlockPos)}.
     *
     * @since 1.0.0
     */
    @FunctionalInterface
    public interface BlockBinding {
        /**
         * Resolves the aura record for the chunk containing a block position.
         *
         * @param level the level
         * @param pos   the block position
         * @return the aura record
         */
        IAuraChunk apply(Level level, BlockPos pos);
    }
}
