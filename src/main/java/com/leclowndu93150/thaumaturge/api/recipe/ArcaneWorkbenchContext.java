package com.leclowndu93150.thaumaturge.api.recipe;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/**
 * Describes where and for whom an arcane craft runs. Vis and aura sources receive it so they can
 * decide what to supply, and the craft transaction uses it to find the placed workbench whose aura
 * pays the craft.
 *
 * <p>A {@link Kind#PLACED} context names a block position, normally a Thaumaturge arcane
 * workbench or an addon block that acts as one. A {@link Kind#VIRTUAL} context has no position,
 * for example a portable crafting terminal; its aura cost is paid by
 * {@link IWorkbenchAuraSource}s.
 *
 * @param level        the server level the craft runs in
 * @param hostIdentity a stable identity for the crafting host, equal across calls for the same
 *                     workbench
 * @param position     the host's block position, present exactly when {@code kind} is
 *                     {@link Kind#PLACED}
 * @param owner        the identity that owns the host, when the host has an owner
 * @param actingPlayer the UUID of the player performing the craft
 * @param kind         whether the host is placed in the world or virtual
 * @since 1.0.0
 */
public record ArcaneWorkbenchContext(ServerLevel level, UUID hostIdentity, @Nullable BlockPos position, @Nullable UUID owner, UUID actingPlayer, Kind kind) {
    /**
     * Validates the components and stores an immutable copy of the position.
     *
     * @throws NullPointerException     when a required component is null
     * @throws IllegalArgumentException when a placed context has no position or a virtual context
     *                                  has one
     */
    public ArcaneWorkbenchContext {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(hostIdentity, "hostIdentity");
        Objects.requireNonNull(actingPlayer, "actingPlayer");
        Objects.requireNonNull(kind, "kind");
        position = position == null ? null : position.immutable();
        if (kind == Kind.PLACED && position == null) {
            throw new IllegalArgumentException("A placed workbench requires a position");
        }
        if (kind == Kind.VIRTUAL && position != null) {
            throw new IllegalArgumentException("A virtual workbench cannot have a block position");
        }
    }

    /**
     * Creates a context for a workbench placed in the player's level.
     *
     * @param player       the crafting player
     * @param position     the workbench position
     * @param hostIdentity a stable identity for the workbench
     * @param owner        the workbench owner, or null
     * @return the context
     */
    public static ArcaneWorkbenchContext placed(ServerPlayer player, BlockPos position, UUID hostIdentity, @Nullable UUID owner) {
        return new ArcaneWorkbenchContext(player.level(), hostIdentity, position, owner, player.getUUID(), Kind.PLACED);
    }

    /**
     * Creates a context for a workbench with no block position.
     *
     * @param player       the crafting player
     * @param hostIdentity a stable identity for the virtual workbench
     * @param owner        the workbench owner, or null
     * @return the context
     */
    public static ArcaneWorkbenchContext virtual(ServerPlayer player, UUID hostIdentity, @Nullable UUID owner) {
        return new ArcaneWorkbenchContext(player.level(), hostIdentity, null, owner, player.getUUID(), Kind.VIRTUAL);
    }

    /**
     * The host's block position.
     *
     * @return the position, empty for a virtual workbench
     */
    public Optional<BlockPos> blockPosition() {
        return Optional.ofNullable(position);
    }

    /**
     * The identity that owns the host.
     *
     * @return the owner, empty when the host has none
     */
    public Optional<UUID> ownerIdentity() {
        return Optional.ofNullable(owner);
    }

    /**
     * Whether a crafting host exists as a block in the world.
     *
     * @since 1.0.0
     */
    public enum Kind {
        /** The host is a block at {@link ArcaneWorkbenchContext#position()}. */
        PLACED,
        /** The host has no block position. */
        VIRTUAL
    }
}
