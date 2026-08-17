package com.leclowndu93150.thaumaturge.api.golems.seals;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * A placed seal in the world: the seal behavior instance plus per-placement state such as priority, color, lock,
 * redstone sensitivity and working area.
 *
 * @since 1.0.0
 */
public interface ISealEntity {
    /**
     * Ticks the seal, honoring redstone suppression.
     *
     * @param level the level the seal lives in
     */
    void tickSealEntity(Level level);

    /**
     * @return the seal behavior instance owned by this placement
     */
    ISeal getSeal();

    /**
     * @return where the seal is placed
     */
    SealPos getSealPos();

    byte getPriority();

    void setPriority(byte priority);

    /**
     * Sends this seal's full state to all clients watching its dimension.
     *
     * @param level the level the seal lives in
     */
    void syncToClient(Level level);

    /**
     * @return the working area extents; each axis counts blocks outward from the seal
     */
    BlockPos getArea();

    void setArea(BlockPos area);

    /**
     * @return whether only golems owned by the seal owner may work this seal
     */
    boolean isLocked();

    void setLocked(boolean locked);

    boolean isRedstoneSensitive();

    void setRedstoneSensitive(boolean redstoneSensitive);

    /**
     * @return the placing player's id, or null when unknown
     */
    @Nullable
    UUID getOwner();

    void setOwner(@Nullable UUID owner);

    /**
     * @return the seal's assigned dye color index, or 0 when uncolored
     */
    byte getColor();

    void setColor(byte color);

    /**
     * @param level the level the seal lives in
     * @return whether a redstone signal currently suppresses this seal
     */
    boolean isStoppedByRedstone(Level level);
}
