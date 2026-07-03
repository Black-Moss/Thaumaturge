package com.leclowndu93150.thaumcraft.api.research.scan;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/**
 * A single scannable subject known to the thaumometer. Implementations decide whether an
 * arbitrary scan target matches and which research key a successful scan grants.
 *
 * <p>The scan target passed to {@link #checkThing checkThing} is one of: an
 * {@link net.minecraft.world.entity.Entity}, an {@link net.minecraft.world.item.ItemStack},
 * a {@link net.minecraft.core.BlockPos} in the scanning player's level, or {@code null} when the
 * player scanned empty air (used by sky scans).
 *
 * <p>Register implementations through {@link ScanningManager#addScannableThing}.
 *
 * @since 1.0.0
 */
public interface IScanThing {
    /**
     * Returns whether this scan subject matches the given target.
     *
     * @param player the scanning player
     * @param target the scan target; entity, stack, position, or {@code null}
     * @return {@code true} when this subject applies to the target
     */
    boolean checkThing(Player player, @Nullable Object target);

    /**
     * The research key granted when this subject is scanned. A {@code null} key marks a subject
     * that only produces side effects through {@link #onSuccess onSuccess} and never reports
     * scan feedback of its own.
     *
     * @param player the scanning player
     * @param target the scan target
     * @return the research key, or {@code null} when this subject grants no key
     */
    @Nullable Identifier getResearchKey(Player player, @Nullable Object target);

    /**
     * Called after the research key was granted (or immediately when the key is {@code null}).
     * Runs on the server.
     *
     * @param player the scanning player
     * @param target the scan target
     */
    default void onSuccess(Player player, @Nullable Object target) {
    }
}
