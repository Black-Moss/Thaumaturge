package com.leclowndu93150.thaumaturge.api.casters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Registry of {@link CasterBlockAction actions} that run when a caster is used on particular blocks.
 *
 * <p>An action is registered either for one exact block state, inside a named group, or for every state of the blocks
 * in a block tag. State actions match by equality of the whole state. Tag actions match at click time against the tags
 * currently loaded, and belong to no group. Groups let a mod run or test only its own state actions; they are created
 * by registering the first action under their name.
 *
 * <p>Actions are registered during mod construction or common setup and are never removed or replaced. The registry
 * performs no synchronisation, so registering while other threads look up actions is not supported. Lookups run on both
 * logical sides.
 *
 * @since 1.1.0
 */
public final class CasterBlockActions {
    /**
     * Group that {@link #onState(BlockState, CasterBlockAction)} registers into.
     *
     * @since 1.1.0
     */
    public static final String DEFAULT_GROUP = "thaumaturge";

    private static final Map<BlockState, List<GroupedAction>> BY_STATE = new LinkedHashMap<>();
    private static final Map<TagKey<Block>, List<CasterBlockAction>> BY_TAG = new LinkedHashMap<>();

    private CasterBlockActions() {}

    /**
     * Registers an action for one exact block state inside a group. Actions for the same state run in registration
     * order, and registering the same action twice runs it twice.
     *
     * @param state the exact block state to match
     * @param group the group name, usually the registering mod's id
     * @param action the action to run
     * @since 1.1.0
     */
    public static void onState(BlockState state, String group, CasterBlockAction action) {
        BY_STATE.computeIfAbsent(state, unused -> new ArrayList<>()).add(new GroupedAction(group, Objects.requireNonNull(action, "action")));
    }

    /**
     * Registers an action for one exact block state in {@link #DEFAULT_GROUP}.
     *
     * @param state the exact block state to match
     * @param action the action to run
     * @since 1.1.0
     */
    public static void onState(BlockState state, CasterBlockAction action) {
        onState(state, DEFAULT_GROUP, action);
    }

    /**
     * Registers an action for every state of every block in a tag. Actions for the same tag run in registration order,
     * and tags are tried in the order they were first registered.
     *
     * @param tag the block tag to match
     * @param action the action to run
     * @since 1.1.0
     */
    public static void onTag(TagKey<Block> tag, CasterBlockAction action) {
        BY_TAG.computeIfAbsent(tag, unused -> new ArrayList<>()).add(Objects.requireNonNull(action, "action"));
    }

    /**
     * Tests whether any action is registered for a state, either for the exact state in any group or for a tag its
     * block is in. A {@code true} result does not promise that an action will handle the click.
     *
     * @param state the block state to test
     * @return {@code true} when at least one action matches
     * @since 1.1.0
     */
    public static boolean hasAction(BlockState state) {
        return BY_STATE.containsKey(state) || BY_TAG.keySet().stream().anyMatch(state::is);
    }

    /**
     * Tests whether an action is registered for the exact state in one group. Tag actions are not considered.
     *
     * @param state the block state to test
     * @param group the group name
     * @return {@code true} when the group has an action for the exact state
     * @since 1.1.0
     */
    public static boolean hasAction(BlockState state, String group) {
        return stateActions(state).stream().anyMatch(grouped -> Objects.equals(grouped.group(), group));
    }

    /**
     * Runs the actions matching {@link CasterBlockUse#state()} until one handles the click. Exact state actions of every
     * group run first, in registration order, then the actions of each tag the state's block is in.
     *
     * @param use the caster use
     * @return {@code true} when an action handled the click, {@code false} when none matched or none handled it
     * @throws RuntimeException whatever an action throws, unchanged
     * @since 1.1.0
     */
    public static boolean dispatch(CasterBlockUse use) {
        Stream<CasterBlockAction> exact = stateActions(use.state()).stream().map(GroupedAction::action);
        Stream<CasterBlockAction> tagged = BY_TAG.entrySet().stream().filter(entry -> use.state().is(entry.getKey())).flatMap(entry -> entry.getValue().stream());
        return Stream.concat(exact, tagged).anyMatch(action -> action.handle(use));
    }

    /**
     * Runs the exact state actions of one group until one handles the click. Tag actions never run.
     *
     * @param use the caster use
     * @param group the group name
     * @return {@code true} when an action handled the click, {@code false} for an unknown group, a state without
     *     actions in it, or when none handled it
     * @throws RuntimeException whatever an action throws, unchanged
     * @since 1.1.0
     */
    public static boolean dispatch(CasterBlockUse use, String group) {
        return stateActions(use.state()).stream().filter(grouped -> Objects.equals(grouped.group(), group)).anyMatch(grouped -> grouped.action().handle(use));
    }

    private static List<GroupedAction> stateActions(BlockState state) {
        return BY_STATE.getOrDefault(state, Collections.emptyList());
    }

    private record GroupedAction(String group, CasterBlockAction action) {
    }
}
