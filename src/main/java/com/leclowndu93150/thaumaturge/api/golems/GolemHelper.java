package com.leclowndu93150.thaumaturge.api.golems;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Static facade for the golem system: seal lookup, task posting and the provisioning queue.
 *
 * <p>Every method forwards to {@link GolemDispatch}.
 *
 * @since 1.0.0
 * @deprecated use {@link GolemDispatch}; this class is removed in 2.0.0
 */
@Deprecated(since = "1.1.0", forRemoval = true)
public final class GolemHelper {
    private GolemHelper() {}

    /**
     * Installs an implementation by adapting it to {@link GolemDispatch.Bindings}.
     *
     * @param bindings the implementation
     * @throws IllegalStateException when an implementation is already bound
     * @deprecated the mod binds {@link GolemDispatch#bind}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void bind(Bindings bindings) {
        GolemDispatch.bind(new LegacyDispatchBindings(bindings));
    }

    /**
     * @param id a seal type id
     * @return the seal type, or empty for an unknown id
     * @deprecated use {@link GolemDispatch#sealType(Identifier)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static Optional<SealType> sealType(Identifier id) {
        return GolemDispatch.sealType(id);
    }

    /**
     * @param id a seal type id
     * @return one placer item of the type, or the empty stack for an unknown id
     * @deprecated use {@link GolemDispatch#sealItem(Identifier)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static ItemStack getSealStack(Identifier id) {
        return GolemDispatch.sealItem(id);
    }

    /**
     * Looks up a placed seal. Works on both logical sides; the client answers from the mirror the server sends.
     *
     * @param level the level
     * @param pos   the seal placement, or null
     * @return the seal, or null when there is none or {@code pos} is null
     * @deprecated use {@link GolemDispatch#sealAt(Level, SealPos)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static @Nullable ISealEntity getSealEntity(Level level, @Nullable SealPos pos) {
        return pos == null ? null : GolemDispatch.sealAt(level, pos).orElse(null);
    }

    /**
     * Posts a task to the level's task board, which gives it a fresh id.
     *
     * @param level the level
     * @param task  the task
     * @deprecated use {@link GolemDispatch#postTask(Level, Task)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void addGolemTask(Level level, Task task) {
        GolemDispatch.postTask(level, task);
    }

    /**
     * Asks provider seals to bring a copy of the stack to a seal. A request equal to a queued one is not added.
     *
     * @param level the level
     * @param seal  the seal that needs the items
     * @param stack the stack; copied
     * @deprecated use {@link GolemDispatch#orderSupply(Level, ISealEntity, ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void requestProvisioning(Level level, ISealEntity seal, ItemStack stack) {
        GolemDispatch.orderSupply(level, seal, stack);
    }

    /**
     * Asks provider seals to bring a copy of the stack to a block face. A request equal to a queued one is not added.
     *
     * @param level the level
     * @param pos   the target block
     * @param side  the face the stack is inserted through
     * @param stack the stack; copied
     * @deprecated use {@link GolemDispatch#orderSupply(Level, BlockPos, Direction, ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void requestProvisioning(Level level, BlockPos pos, Direction side, ItemStack stack) {
        GolemDispatch.orderSupply(level, pos, side, stack);
    }

    /**
     * Asks provider seals to bring a copy of the stack to an entity. A request equal to a queued one is not added.
     *
     * @param level  the level
     * @param entity the target entity
     * @param stack  the stack; copied
     * @deprecated use {@link GolemDispatch#orderSupply(Level, Entity, ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void requestProvisioning(Level level, Entity entity, ItemStack stack) {
        GolemDispatch.orderSupply(level, entity, stack);
    }

    /**
     * Like {@link #requestProvisioning(Level, BlockPos, Direction, ItemStack)} with a batch number, so identical requests of
     * one large order stay separate.
     *
     * @param level the level
     * @param pos   the target block
     * @param side  the face the stack is inserted through
     * @param stack the stack; copied
     * @param batch the batch number
     * @deprecated use {@link GolemDispatch#orderSupply(Level, BlockPos, Direction, ItemStack, int)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void requestProvisioning(Level level, BlockPos pos, Direction side, ItemStack stack, int batch) {
        GolemDispatch.orderSupply(level, pos, side, stack, batch);
    }

    /**
     * Like {@link #requestProvisioning(Level, Entity, ItemStack)} with a batch number, so identical requests of one large
     * order stay separate.
     *
     * @param level  the level
     * @param entity the target entity
     * @param stack  the stack; copied
     * @param batch  the batch number
     * @deprecated use {@link GolemDispatch#orderSupply(Level, Entity, ItemStack, int)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void requestProvisioning(Level level, Entity entity, ItemStack stack, int batch) {
        GolemDispatch.orderSupply(level, entity, stack, batch);
    }

    /**
     * @param level the level
     * @return the level's live provisioning queue in posting order, not a copy; it supports removal by predicate and by
     *         index and concurrent iteration
     * @deprecated use {@link GolemDispatch#supplyQueue(Level)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    @SuppressWarnings("unchecked")
    public static List<ProvisionRequest> getProvisionRequests(Level level) {
        return (List<ProvisionRequest>) (List<?>) GolemDispatch.supplyQueue(level);
    }

    /**
     * The implementation behind {@link GolemHelper}, bound by the mod once.
     *
     * @since 1.0.0
     * @deprecated implement {@link GolemDispatch.Bindings}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public interface Bindings {
        /**
         * @param id a seal type id
         * @return the seal type, or empty
         */
        Optional<SealType> sealType(Identifier id);

        /**
         * @param level the level
         * @param pos   the placement, possibly null
         * @return the seal, or null
         */
        @Nullable
        ISealEntity getSealEntity(Level level, @Nullable SealPos pos);

        /**
         * @param level the level
         * @param task  the task to post
         */
        void addGolemTask(Level level, Task task);

        /**
         * @param level the level
         * @return the level's live provisioning queue
         */
        List<ProvisionRequest> getProvisionRequests(Level level);
    }
}
