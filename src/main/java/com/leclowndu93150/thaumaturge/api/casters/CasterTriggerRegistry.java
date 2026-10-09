package com.leclowndu93150.thaumaturge.api.casters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
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
 * @since 1.0.0
 */
public final class CasterTriggerRegistry {
    private static final String DEFAULT_GROUP = "thaumaturge";

    private static final Index<BlockState> STATES = Index.empty();
    private static final Index<TagKey<Block>> TAGS = Index.empty();

    private CasterTriggerRegistry() {}

    /**
     * Registers a trigger for every block state whose block belongs to a tag. Triggers for the
     * same tag keep registration order, duplicates are kept, and tags are consulted in the order
     * they were first registered.
     *
     * @param handler the manager that performs the trigger
     * @param eventId the opaque event number handed back to the manager
     * @param blockTag the block tag to match
     */
    public static void registerCasterBlockTagTrigger(ICasterTriggerManager handler, int eventId, TagKey<Block> blockTag) {
        TAGS.put(blockTag, new Entry(null, handler, eventId));
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
     */
    public static void registerCasterBlockTrigger(ICasterTriggerManager handler, int eventId, BlockState target, String groupName) {
        STATES.put(target, new Entry(groupName, handler, eventId));
    }

    /**
     * Registers a trigger for one exact block state in the group named {@code thaumaturge}.
     *
     * @param handler the manager that performs the trigger
     * @param eventId the opaque event number handed back to the manager
     * @param target  the exact block state to match
     */
    public static void registerCasterBlockTrigger(ICasterTriggerManager handler, int eventId, BlockState target) {
        STATES.put(target, new Entry(DEFAULT_GROUP, handler, eventId));
    }

    /**
     * Tests whether anything is registered for a state in any group or through any tag the
     * state's block belongs to. A true result does not guarantee that a trigger would succeed.
     *
     * @param candidate the block state to test
     * @return true when an exact state trigger or a matching tag trigger exists
     */
    public static boolean hasTrigger(BlockState candidate) {
        return TAGS.matching(candidate::is).findAny().isPresent() || STATES.contains(candidate);
    }

    /**
     * Tests whether an exact state is registered in one group. Tag triggers are never considered.
     *
     * @param candidate the block state to test
     * @param groupName the group name
     * @return true when the group exists and contains the exact state
     */
    public static boolean hasTrigger(BlockState candidate, String groupName) {
        return STATES.at(candidate).stream().map(Entry::group).anyMatch(found -> Objects.equals(found, groupName));
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
     */
    public static boolean performTrigger(Level world, ItemStack casterStack, Player user, BlockPos clicked, Direction face, BlockState hit) {
        Click click = new Click(world, casterStack, user, clicked, face);
        return Stream.concat(Stream.of(STATES.at(hit)), TAGS.matching(hit::is)).anyMatch(click::runAny);
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
     */
    public static boolean performTrigger(Level world, ItemStack casterStack, Player user, BlockPos clicked, Direction face, BlockState hit, String groupName) {
        Click click = new Click(world, casterStack, user, clicked, face);
        return click.runAny(STATES.at(hit).stream().filter(entry -> entry.inGroup(groupName)).toList());
    }

    private record Entry(String group, ICasterTriggerManager manager, int event) {
        boolean fireOn(Click click) {
            return manager.performTrigger(click.level(), click.casterStack(), click.player(), click.pos(), click.side(), event);
        }

        boolean inGroup(String other) {
            return Objects.equals(group, other);
        }
    }

    private static final class Index<K> {
        private final Map<K, List<Entry>> lists = new LinkedHashMap<>();

        static <T> Index<T> empty() {
            return new Index<>();
        }

        void put(K key, Entry entry) {
            lists.computeIfAbsent(key, unused -> new ArrayList<>()).add(entry);
        }

        boolean contains(K key) {
            return lists.containsKey(key);
        }

        List<Entry> at(K key) {
            return lists.getOrDefault(key, Collections.emptyList());
        }

        Stream<List<Entry>> matching(Predicate<K> test) {
            return lists.keySet().stream().filter(test).map(lists::get);
        }
    }

    private record Click(Level level, ItemStack casterStack, Player player, BlockPos pos, Direction side) {
        boolean runAny(List<Entry> entries) {
            return entries.stream().anyMatch(this::run);
        }

        private boolean run(Entry entry) {
            return entry.fireOn(this);
        }
    }
}
