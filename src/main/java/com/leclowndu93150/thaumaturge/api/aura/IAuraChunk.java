package com.leclowndu93150.thaumaturge.api.aura;

import net.minecraft.world.level.ChunkPos;

/**
 * Read-only view of a single chunk's aura record (base / vis / flux).
 *
 * <p>
 * The record is per-chunk and lives on the server. Clients receive a copy through the underlying attachment sync.
 * Mutating the record is internal; addons read through {@link AuraHelper}.
 *
 * @since 1.0.0
 */
public interface IAuraChunk {
    /**
     * Returns the chunk's maximum aura, sampled at generation time from biome modifiers.
     *
     * @return base aura, clamped to {@code [0, 500]}
     */
    short getBase();

    /**
     * Returns the current pure vis in the chunk.
     *
     * @return vis amount, clamped to {@code [0, 32766]}
     */
    float getVis();

    /**
     * Returns the current flux in the chunk.
     *
     * @return flux amount, clamped to {@code [0, 32766]}
     */
    float getFlux();

    /**
     * Returns the chunk's position.
     *
     * @return the chunk position
     */
    ChunkPos getChunkPos();
}
