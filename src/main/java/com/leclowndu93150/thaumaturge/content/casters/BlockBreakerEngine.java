package com.leclowndu93150.thaumaturge.content.casters;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.casters.BlockWorkQueues.BreakerTask;
import com.leclowndu93150.thaumaturge.content.casters.BlockWorkQueues.SwapContext;
import com.leclowndu93150.thaumaturge.content.casters.BlockWorkQueues.SwapperTask;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntitySpecialItem;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID)
public final class BlockBreakerEngine {
    private static final int LEVEL_EVENT_BLOCK_BREAK = 2001;
    private static final int CRACK_STAGES = 10;
    private static final int CRACK_CLEARED = -1;
    private static final int SILK_TOUCH_LEVEL = 1;
    private static final int COLOR_CHANNEL_MASK = 0xFF;
    private static final float COLOR_CHANNEL_MAX = 255.0F;
    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;
    private static final int BLUE_SHIFT = 0;
    private static final double BLOCK_CENTER = 0.5;
    private static final int SPREAD_REACH = 1;
    private static final Direction[] DIRECTIONS = Direction.values();

    private BlockBreakerEngine() {}

    public static BreakerTask.Builder breaker(BlockPos pos, BlockState state, Player player) {
        return new BreakerTask.Builder(pos, state, player.getUUID());
    }

    public static SwapperTask.Builder swapper(BlockPos pos, @Nullable BlockState source, @Nullable BlockState target, Player player) {
        return new SwapperTask.Builder(pos, source, target, player.getUUID());
    }

    public static void harvestBlock(ServerLevel level, Player player, BlockPos pos, boolean silk, int fortune) {
        ServerPlayer actor = player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        BlockState state = level.getBlockState(pos);
        if (actor == null || isVetoed(level, actor, pos, state)) {
            return;
        }
        removeAndDrop(level, actor, pos, state, dropTool(level, actor, silk, fortune));
    }

    private static void removeAndDrop(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, ItemStack tool) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        level.levelEvent(null, LEVEL_EVENT_BLOCK_BREAK, pos, Block.getId(state));
        boolean removed = level.removeBlock(pos, false);
        if (removed && !player.hasInfiniteMaterials()) {
            Block.dropResources(state, level, pos, blockEntity, player, tool);
        }
    }

    private static boolean isVetoed(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state) {
        return CommonHooks.fireBlockBreak(level, player.gameMode.getGameModeForPlayer(), player, pos, state).isCanceled();
    }

    static ItemStack dropTool(ServerLevel level, Player player, boolean silk, int fortune) {
        ItemStack held = player.getMainHandItem();
        if (!silk && fortune <= 0) {
            return held;
        }
        ItemStack tool = held.isEmpty() ? new ItemStack(Items.NETHERITE_PICKAXE) : held.copy();
        HolderLookup.RegistryLookup<Enchantment> enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        EnchantmentHelper.updateEnchantments(tool, mutable -> {
            if (silk) {
                mutable.set(enchantments.getOrThrow(Enchantments.SILK_TOUCH), SILK_TOUCH_LEVEL);
            }
            if (fortune > 0) {
                mutable.set(enchantments.getOrThrow(Enchantments.FORTUNE), fortune);
            }
        });
        return tool;
    }

    private static <T> List<T> drain(List<T> queue) {
        if (queue.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> batch = new ArrayList<>(queue);
        queue.clear();
        return batch;
    }

    private static void runBreaks(ServerLevel level, List<BreakerTask> queue) {
        List<BreakerTask> batch = drain(queue);
        if (batch.isEmpty()) {
            return;
        }
        queue.addAll(0, batch.stream().map(task -> advance(level, task)).filter(Objects::nonNull).toList());
    }

    private static @Nullable BreakerTask advance(ServerLevel level, BreakerTask task) {
        if (task.delay() > 0) {
            return task.withDelay(task.delay() - 1);
        }
        BlockPos pos = task.pos();
        if (!level.hasChunkAt(pos)) {
            return null;
        }
        Player player = level.getPlayerByUUID(task.playerId());
        if (!canContinue(level, task, player)) {
            clearCrack(level, task);
            return null;
        }
        float remaining = task.durability() - task.strength();
        if (remaining <= 0) {
            finish(level, task, player);
            return null;
        }
        if (task.fx()) {
            level.destroyBlockProgress(crackId(pos), pos, crackStage(task));
        }
        return task.withDurability(remaining);
    }

    private static boolean canContinue(ServerLevel level, BreakerTask task, @Nullable Player player) {
        if (player == null) {
            return false;
        }
        BlockPos at = task.pos();
        BlockState found = level.getBlockState(at);
        if (!found.equals(task.source())) {
            return false;
        }
        if (!canAfford(player, task.visCost(), task.visAspect())) {
            return false;
        }
        return player.mayInteract(level, at) && found.getDestroySpeed(level, at) >= 0;
    }

    private static boolean canAfford(Player player, float cost, @Nullable ResourceKey<IAspect> aspect) {
        return cost <= 0 || WandVisHelper.consumeVisFromHotbar(player, cost, aspect, false);
    }

    private static void charge(Player player, float cost, @Nullable ResourceKey<IAspect> aspect) {
        if (cost > 0) {
            WandVisHelper.consumeVisFromHotbar(player, cost, aspect, true);
        }
    }

    private static void finish(ServerLevel level, BreakerTask task, Player player) {
        harvestBlock(level, player, task.pos(), task.silk(), task.fortune());
        clearCrack(level, task);
        charge(player, task.visCost(), task.visAspect());
    }

    private static int crackStage(BreakerTask task) {
        float progress = 1.0F - task.durability() / task.durabilityMax();
        return Mth.clamp(Mth.floor(progress * CRACK_STAGES), 0, CRACK_STAGES - 1);
    }

    private static int crackId(BlockPos pos) {
        return pos.hashCode();
    }

    private static void clearCrack(ServerLevel level, BreakerTask task) {
        if (task.fx()) {
            level.destroyBlockProgress(crackId(task.pos()), task.pos(), CRACK_CLEARED);
        }
    }

    private static void runSwaps(ServerLevel level, List<SwapperTask> queue) {
        for (SwapperTask task : drain(queue)) {
            swap(level, task, queue);
        }
    }

    private static void swap(ServerLevel level, SwapperTask task, List<SwapperTask> queue) {
        Player player = level.getPlayerByUUID(task.playerId());
        BlockPos pos = task.pos();
        if (player == null || !level.hasChunkAt(pos)) {
            return;
        }
        BlockState current = level.getBlockState(pos);
        BlockState target = task.target();
        if (current.getDestroySpeed(level, pos) < 0 || task.source() != null && !current.equals(task.source())) {
            return;
        }
        if (!canAfford(player, task.visCost(), task.visAspect())) {
            return;
        }
        if (!player.mayInteract(level, pos) || target != null && current.is(target.getBlock())) {
            return;
        }
        if (EventHooks.onBlockPlace(player, BlockSnapshot.create(level.dimension(), level, pos), Direction.UP) || !task.allowSwap().test(new SwapContext(level, player, pos))) {
            return;
        }
        boolean pays = !player.hasInfiniteMaterials();
        boolean consumes = pays && task.consumeTarget() && target != null;
        OptionalInt consumeSlot = consumes ? findSlot(player.getInventory(), target.getBlock().asItem()) : OptionalInt.empty();
        if (consumes && consumeSlot.isEmpty()) {
            return;
        }
        List<ItemStack> drops = pays && task.pickup()
                ? Block.getDrops(current, level, pos, level.getBlockEntity(pos), player, dropTool(level, player, task.silk(), task.fortune()))
                : Collections.emptyList();
        boolean swapped = target == null ? level.removeBlock(pos, false) : level.setBlock(pos, target, Block.UPDATE_ALL);
        if (!swapped) {
            return;
        }
        consumeSlot.ifPresent(slot -> player.getInventory().getItem(slot).shrink(1));
        if (pays) {
            charge(player, task.visCost(), task.visAspect());
        }
        drops.forEach(drop -> give(level, player, pos, drop));
        if (task.fx()) {
            burst(level, task);
        }
        if (task.lifespan() > 0) {
            spread(level, task, queue);
        }
    }

    private static OptionalInt findSlot(Inventory inventory, Item item) {
        return IntStream.range(0, Inventory.INVENTORY_SIZE).filter(slot -> holds(inventory.getItem(slot), item)).findFirst();
    }

    private static boolean holds(ItemStack stack, Item item) {
        return !stack.isEmpty() && stack.is(item);
    }

    private static void give(ServerLevel level, Player player, BlockPos pos, ItemStack drop) {
        player.getInventory().add(drop);
        if (!drop.isEmpty()) {
            level.addFreshEntity(new EntitySpecialItem(level, pos.getX() + BLOCK_CENTER, pos.getY() + BLOCK_CENTER, pos.getZ() + BLOCK_CENTER, drop));
        }
    }

    private static void burst(ServerLevel level, SwapperTask task) {
        int color = task.color();
        Effects.Bamf bamf = Effects.bamf(level, task.pos()).color(channel(color, RED_SHIFT), channel(color, GREEN_SHIFT), channel(color, BLUE_SHIFT)).withSound();
        if (task.fancy()) {
            bamf = bamf.fancy();
        }
        bamf.send();
    }

    private static float channel(int color, int shift) {
        return ((color >> shift) & COLOR_CHANNEL_MASK) / COLOR_CHANNEL_MAX;
    }

    private static void spread(ServerLevel level, SwapperTask task, List<SwapperTask> queue) {
        BlockState source = task.source();
        if (source == null) {
            return;
        }
        BlockPos.MutableBlockPos candidate = new BlockPos.MutableBlockPos();
        for (int dx = -SPREAD_REACH; dx <= SPREAD_REACH; dx++) {
            for (int dy = -SPREAD_REACH; dy <= SPREAD_REACH; dy++) {
                for (int dz = -SPREAD_REACH; dz <= SPREAD_REACH; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    candidate.setWithOffset(task.pos(), dx, dy, dz);
                    if (level.hasChunkAt(candidate) && level.getBlockState(candidate).equals(source) && isExposed(level, candidate)) {
                        queue.add(task.propagatedTo(candidate.immutable()));
                    }
                }
            }
        }
    }

    private static boolean isExposed(ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
        for (Direction direction : DIRECTIONS) {
            neighbor.setWithOffset(pos, direction);
            if (level.hasChunkAt(neighbor) && !level.getBlockState(neighbor).isSolidRender()) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            processQueues(level, level.getData(TTAttachments.BLOCK_WORK_QUEUES));
        }
    }

    private static void processQueues(ServerLevel level, BlockWorkQueues queues) {
        runSwaps(level, queues.swappers());
        runBreaks(level, queues.breakers());
    }
}
