package com.leclowndu93150.thaumaturge.api.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Convenience lookups for the sided essentia transport capability.
 *
 * <p>
 * These wrap {@link EssentiaCapabilities#TRANSPORT} so that tooltip, JEI, and integration code can ask "does this block
 * expose essentia transport on this face" without repeating the capability query. The {@code face} argument is the side
 * of the queried block that the caller is approaching from, matching the sided-capability convention.
 *
 * @since 1.0.0
 */
public final class EssentiaAccess {
    private EssentiaAccess() {}

    /**
     * Returns the essentia transport exposed by the block at {@code pos} on the given face.
     *
     * @param level the level
     * @param pos the block position
     * @param face the side of the block being approached
     * @return the transport, or {@code null} when the block exposes none on that face
     */
    public static @Nullable IEssentiaTransport transport(Level level, BlockPos pos, Direction face) {
        return level.getCapability(EssentiaCapabilities.TRANSPORT, pos, face);
    }

    /**
     * Whether the block at {@code pos} exposes essentia transport on the given face.
     *
     * @param level the level
     * @param pos the block position
     * @param face the side of the block being approached
     * @return {@code true} when a transport capability is present on that face
     */
    public static boolean isEssentiaTransport(Level level, BlockPos pos, Direction face) {
        return transport(level, pos, face) != null;
    }
}
