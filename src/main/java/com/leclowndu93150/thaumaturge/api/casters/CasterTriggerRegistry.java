package com.leclowndu93150.thaumaturge.api.casters;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Static registry mapping block states and block tags to {@link ICasterTriggerManager}
 * handlers that run when a caster is used on a matching block.
 *
 * <p>State triggers match by equality of the complete block state and carry a group name. Tag triggers match every state whose block belongs to the tag, resolved at click
 * time against the currently loaded tags, and belong to no group.
 *
 * <p>Registration is intended for mod construction or common setup and is never removed or
 * replaced. The registry performs no synchronisation, so registering concurrently with lookups
 * is unsupported. Lookups run on both the logical client and the logical server.
 *
 * <p>Since 1.1.0 every method forwards to {@link CasterBlockActions}, which holds the only copy of the registrations.
 * A manager registered here runs as a {@link CasterBlockAction} that passes its event number back to it.
 *
 * @since 1.0.0
 * @deprecated use {@link CasterBlockActions}; it is removed in 2.0.0.
 */
@Deprecated(since = "1.1.0", forRemoval = true)
@SuppressWarnings("removal")
public final class CasterTriggerRegistry {
    private CasterTriggerRegistry() {}

    /**
     * Registers a trigger for every block state whose block belongs to a tag. Triggers for the
     * same tag keep registration order, duplicates are kept, and tags are consulted in the order
     * they were first registered.
     *
     * @param handler the manager that performs the trigger
     * @param eventId the opaque event number handed back to the manager
     * @param blockTag the block tag to match
     * @deprecated use {@link CasterBlockActions#onTag}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void registerCasterBlockTagTrigger(ICasterTriggerManager handler, int eventId, TagKey<Block> blockTag) {
        CasterBlockActions.onTag(blockTag, asAction(handler, eventId));
    }

    /**
     * Registers a trigger for one exact block state inside a named group. The group exists once
     * a trigger is registered under its name. Triggers for the same group and state keep
     * registration order and duplicates are kept.
     *
     * @param handler the manager that performs the trigger
     * @param eventId the opaque event number handed back to the manager
     * @param target  the exact block state to match
     * @param groupName the group name, typically the registering mod id
     * @deprecated use {@link CasterBlockActions#onState(BlockState, String, CasterBlockAction)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void registerCasterBlockTrigger(ICasterTriggerManager handler, int eventId, BlockState target, String groupName) {
        CasterBlockActions.onState(target, groupName, asAction(handler, eventId));
    }

    /**
     * Registers a trigger for one exact block state in the group named {@code thaumaturge}.
     *
     * @param handler the manager that performs the trigger
     * @param eventId the opaque event number handed back to the manager
     * @param target  the exact block state to match
     * @deprecated use {@link CasterBlockActions#onState(BlockState, CasterBlockAction)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void registerCasterBlockTrigger(ICasterTriggerManager handler, int eventId, BlockState target) {
        CasterBlockActions.onState(target, asAction(handler, eventId));
    }

    /**
     * Tests whether anything is registered for a state in any group or through any tag the
     * state's block belongs to. A true result does not guarantee that a trigger would succeed.
     *
     * @param candidate the block state to test
     * @return true when an exact state trigger or a matching tag trigger exists
     * @deprecated use {@link CasterBlockActions#hasAction(BlockState)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean hasTrigger(BlockState candidate) {
        return CasterBlockActions.hasAction(candidate);
    }

    /**
     * Tests whether an exact state is registered in one group. Tag triggers are never considered.
     *
     * @param candidate the block state to test
     * @param groupName the group name
     * @return true when the group exists and contains the exact state
     * @deprecated use {@link CasterBlockActions#hasAction(BlockState, String)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean hasTrigger(BlockState candidate, String groupName) {
        return CasterBlockActions.hasAction(candidate, groupName);
    }

    /**
     * Runs the triggers matching a state until one reports success. Exact state triggers of every
     * group run first, in overall registration order, followed by tag triggers for each tag
     * containing the state. Exceptions thrown by a manager propagate to the caller, and a manager
     * reporting failure may still have caused side effects.
     *
     * @param world       the level of the click
     * @param casterStack the caster stack used
     * @param user        the clicking player
     * @param clicked     the clicked position
     * @param face        the clicked face
     * @param hit         the clicked block state
     * @return true when a trigger reported success, false when none matched or succeeded
     * @deprecated use {@link CasterBlockActions#dispatch(CasterBlockUse)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean performTrigger(Level world, ItemStack casterStack, Player user, BlockPos clicked, Direction face, BlockState hit) {
        return CasterBlockActions.dispatch(new CasterBlockUse(world, casterStack, user, clicked, face, hit));
    }

    /**
     * Runs the exact state triggers of one group until one reports success. Tag triggers are
     * never run.
     *
     * @param world       the level of the click
     * @param casterStack the caster stack used
     * @param user        the clicking player
     * @param clicked     the clicked position
     * @param face        the clicked face
     * @param hit         the clicked block state
     * @param groupName   the group name
     * @return true when a trigger reported success, false for an unknown group, a state without
     *         triggers or when none succeeded
     * @deprecated use {@link CasterBlockActions#dispatch(CasterBlockUse, String)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean performTrigger(Level world, ItemStack casterStack, Player user, BlockPos clicked, Direction face, BlockState hit, String groupName) {
        return CasterBlockActions.dispatch(new CasterBlockUse(world, casterStack, user, clicked, face, hit), groupName);
    }

    @SuppressWarnings("removal")
    private static CasterBlockAction asAction(ICasterTriggerManager manager, int eventId) {
        return use -> manager.performTrigger(use.level(), use.caster(), use.player(), use.pos(), use.face(), eventId);
    }
}
