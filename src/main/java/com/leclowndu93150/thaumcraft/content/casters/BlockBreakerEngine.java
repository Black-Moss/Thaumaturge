package com.leclowndu93150.thaumcraft.content.casters;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aura.AuraHelper;
import com.leclowndu93150.thaumcraft.content.casters.BlockWorkQueues.BreakerTask;
import com.leclowndu93150.thaumcraft.content.casters.BlockWorkQueues.SwapContext;
import com.leclowndu93150.thaumcraft.content.casters.BlockWorkQueues.SwapperTask;
import com.leclowndu93150.thaumcraft.content.entity.EntitySpecialItem;
import com.leclowndu93150.thaumcraft.content.fx.FX;
import com.leclowndu93150.thaumcraft.registry.TCAttachments;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TCIds.MODID)
public final class BlockBreakerEngine {
    public static final Predicate<SwapContext> DEFAULT_PREDICATE = context -> true;

    private static final int BREAK_PROGRESS_STEPS = 10;
    private static final int LEVEL_EVENT_BLOCK_BREAK = 2001;
    private static final float COLOR_DIVISOR = 255.0F;

    private BlockBreakerEngine() {}

    public static void addBreaker(ServerLevel level, BlockPos pos, BlockState source, Player player, boolean fx,
            boolean silk, int fortune, float strength, float durabilityCurrent, float durabilityMax, int delay,
            float visCost) {
        level.getData(TCAttachments.BLOCK_WORK_QUEUES).breakers().add(new BreakerTask(
                pos.immutable(), source, player.getUUID(), fx, silk, fortune,
                strength, durabilityCurrent, durabilityMax, delay, visCost));
    }

    public static void addSwapper(ServerLevel level, BlockPos pos, @Nullable BlockState source,
            @Nullable BlockState target, boolean consumeTarget, int lifespan, Player player, boolean fx,
            boolean fancy, int color, boolean pickup, boolean silk, int fortune,
            Predicate<SwapContext> allowSwap, float visCost) {
        level.getData(TCAttachments.BLOCK_WORK_QUEUES).swappers().add(new SwapperTask(
                pos.immutable(), source, target, consumeTarget, lifespan, player.getUUID(), fx, fancy, color,
                pickup, silk, fortune, allowSwap, visCost));
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockWorkQueues queues = level.getData(TCAttachments.BLOCK_WORK_QUEUES);
        tickSwappers(level, queues);
        tickBreakers(level, queues);
    }

    private static void tickBreakers(ServerLevel level, BlockWorkQueues queues) {
        if (queues.breakers().isEmpty()) {
            return;
        }
        List<BreakerTask> pending = new ArrayList<>(queues.breakers());
        queues.breakers().clear();
        for (BreakerTask task : pending) {
            if (task.delay > 0) {
                task.delay--;
                queues.breakers().add(task);
                continue;
            }
            BlockState current = level.getBlockState(task.pos);
            if (current != task.source) {
                if (task.fx) {
                    level.destroyBlockProgress(task.pos.hashCode(), task.pos, -1);
                }
                continue;
            }
            Player player = level.getPlayerByUUID(task.playerId);
            if (player == null) {
                continue;
            }
            if (task.visCost > 0.0F && AuraHelper.getVis(level, task.pos) < task.visCost) {
                continue;
            }
            if (!player.mayInteract(level, task.pos) || current.getDestroySpeed(level, task.pos) < 0.0F) {
                continue;
            }
            if (task.fx) {
                level.destroyBlockProgress(task.pos.hashCode(), task.pos,
                        (int) ((1.0F - task.durabilityCurrent / task.durabilityMax) * BREAK_PROGRESS_STEPS));
            }
            task.durabilityCurrent -= task.strength;
            if (task.durabilityCurrent <= 0.0F) {
                harvestBlock(level, player, task.pos, task.silk, task.fortune);
                if (task.fx) {
                    level.destroyBlockProgress(task.pos.hashCode(), task.pos, -1);
                }
                if (task.visCost > 0.0F) {
                    AuraHelper.drainVis(level, task.pos, task.visCost, false);
                }
            } else {
                queues.breakers().add(task);
            }
        }
    }

    public static void harvestBlock(ServerLevel level, Player player, BlockPos pos, boolean silk, int fortune) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        BreakBlockEvent event = CommonHooks.fireBlockBreak(level,
                serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer, pos, state);
        if (event.isCanceled()) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        level.levelEvent(null, LEVEL_EVENT_BLOCK_BREAK, pos, Block.getId(state));
        if (!level.removeBlock(pos, false)) {
            return;
        }
        if (!player.hasInfiniteMaterials()) {
            Block.dropResources(state, level, pos, blockEntity, player, harvestTool(level, player, silk, fortune));
        }
    }

    private static ItemStack harvestTool(ServerLevel level, Player player, boolean silk, int fortune) {
        ItemStack tool = player.getMainHandItem().copy();
        if (tool.isEmpty() && (silk || fortune > 0)) {
            tool = new ItemStack(Items.NETHERITE_PICKAXE);
        }
        if (!tool.isEmpty() && (silk || fortune > 0)) {
            Registry<Enchantment> enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            if (silk) {
                tool.enchant(enchantments.getOrThrow(Enchantments.SILK_TOUCH), 1);
            }
            if (fortune > 0) {
                tool.enchant(enchantments.getOrThrow(Enchantments.FORTUNE), fortune);
            }
        }
        return tool;
    }

    private static void tickSwappers(ServerLevel level, BlockWorkQueues queues) {
        if (queues.swappers().isEmpty()) {
            return;
        }
        List<SwapperTask> pending = new ArrayList<>(queues.swappers());
        queues.swappers().clear();
        for (SwapperTask task : pending) {
            Player player = level.getPlayerByUUID(task.playerId);
            if (player == null) {
                continue;
            }
            BlockState current = level.getBlockState(task.pos);
            boolean allow = current.getDestroySpeed(level, task.pos) >= 0.0F;
            if (task.source != null && task.source != current) {
                allow = false;
            }
            if (task.visCost > 0.0F && AuraHelper.getVis(level, task.pos) < task.visCost) {
                allow = false;
            }
            if (!allow || !player.mayInteract(level, task.pos)) {
                continue;
            }
            if (task.target != null && current.is(task.target.getBlock())) {
                continue;
            }
            if (EventHooks.onBlockPlace(player, BlockSnapshot.create(level.dimension(), level, task.pos), Direction.UP)) {
                continue;
            }
            if (!task.allowSwap.test(new SwapContext(level, player, task.pos))) {
                continue;
            }
            ItemStack targetItem = task.target != null ? new ItemStack(task.target.getBlock()) : ItemStack.EMPTY;
            int slot = 1;
            if (task.consumeTarget && !targetItem.isEmpty() && !player.hasInfiniteMaterials()) {
                slot = findInventorySlot(player, targetItem);
            }
            if (slot < 0) {
                continue;
            }
            if (!player.hasInfiniteMaterials()) {
                if (task.consumeTarget && !targetItem.isEmpty()) {
                    player.getInventory().removeItem(slot, 1);
                }
                if (task.pickup) {
                    List<ItemStack> drops = Block.getDrops(current, level, task.pos, level.getBlockEntity(task.pos),
                            player, harvestTool(level, player, task.silk, task.fortune));
                    for (ItemStack drop : drops) {
                        if (!player.getInventory().add(drop)) {
                            level.addFreshEntity(new EntitySpecialItem(level,
                                    task.pos.getX() + 0.5, task.pos.getY() + 0.5, task.pos.getZ() + 0.5, drop));
                        }
                    }
                }
                if (task.visCost > 0.0F) {
                    AuraHelper.drainVis(level, task.pos, task.visCost, false);
                }
            }
            if (task.target != null) {
                level.setBlock(task.pos, task.target, Block.UPDATE_ALL);
            } else {
                level.removeBlock(task.pos, false);
            }
            if (task.fx) {
                FX.Bamf bamf = FX.bamf(level, task.pos)
                        .color(((task.color >> 16) & 0xFF) / COLOR_DIVISOR,
                                ((task.color >> 8) & 0xFF) / COLOR_DIVISOR,
                                (task.color & 0xFF) / COLOR_DIVISOR)
                        .withSound();
                if (task.fancy) {
                    bamf = bamf.fancy();
                }
                bamf.send();
            }
            if (task.lifespan > 0) {
                spread(level, queues, task);
            }
        }
    }

    private static void spread(ServerLevel level, BlockWorkQueues queues, SwapperTask task) {
        for (int xx = -1; xx <= 1; xx++) {
            for (int yy = -1; yy <= 1; yy++) {
                for (int zz = -1; zz <= 1; zz++) {
                    if (xx == 0 && yy == 0 && zz == 0) {
                        continue;
                    }
                    BlockPos neighbour = task.pos.offset(xx, yy, zz);
                    if (task.source != null && level.getBlockState(neighbour) == task.source
                            && isExposed(level, neighbour)) {
                        queues.swappers().add(new SwapperTask(neighbour, task.source, task.target,
                                task.consumeTarget, task.lifespan - 1, task.playerId, task.fx, task.fancy,
                                task.color, task.pickup, task.silk, task.fortune, task.allowSwap, task.visCost));
                    }
                }
            }
        }
    }

    static boolean isExposed(ServerLevel level, BlockPos pos) {
        for (Direction face : Direction.values()) {
            if (!level.getBlockState(pos.relative(face)).isSolidRender()) {
                return true;
            }
        }
        return false;
    }

    private static int findInventorySlot(Player player, ItemStack target) {
        NonNullList<ItemStack> main = player.getInventory().getNonEquipmentItems();
        for (int slot = 0; slot < main.size(); slot++) {
            ItemStack stack = main.get(slot);
            if (!stack.isEmpty() && stack.is(target.getItem())) {
                return slot;
            }
        }
        return -1;
    }
}
