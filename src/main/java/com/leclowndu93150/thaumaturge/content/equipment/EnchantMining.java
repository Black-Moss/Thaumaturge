package com.leclowndu93150.thaumaturge.content.equipment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.EventHooks;

public final class EnchantMining {
    private static final int LEVEL_EVENT_BLOCK_BREAK = 2001;
    private static final int SEARCH_RADIUS_HORIZONTAL = 24;
    private static final int SEARCH_RADIUS_VERTICAL = 48;
    private static final int SEARCH_MAX_POSITIONS = 1024;
    private static final int TICK_CUBE_RADIUS = 3;
    private static final int TICK_DELAY_BASE = 50;
    private static final int TICK_DELAY_SPREAD = 75;
    private static final int SILK_TOUCH_LEVEL = 1;
    private static final int NEIGHBOUR_RANGE = 1;
    private static final int NEIGHBOUR_SIDE = NEIGHBOUR_RANGE * 2 + 1;
    private static final int NEIGHBOUR_CELLS = NEIGHBOUR_SIDE * NEIGHBOUR_SIDE * NEIGHBOUR_SIDE;
    private static final ThreadLocal<Boolean> HARVESTING_FURTHEST = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final List<Vec3i> NEIGHBOUR_OFFSETS = neighbourOffsets();

    private EnchantMining() {}

    public static boolean isHarvestingFurthest() {
        return Boolean.TRUE.equals(HARVESTING_FURTHEST.get());
    }

    public static boolean harvestBlock(ServerLevel level, Player player, BlockPos pos, boolean skipPermission) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (!skipPermission && isBreakCancelled(level, serverPlayer, pos, state)) {
            return false;
        }
        List<ItemStack> drops = collectDrops(level, player, pos, state);
        level.levelEvent(null, LEVEL_EVENT_BLOCK_BREAK, pos, Block.getId(state));
        if (!level.removeBlock(pos, false)) {
            return false;
        }
        drops.forEach(drop -> Block.popResource(level, pos, drop));
        return true;
    }

    public static boolean breakFurthest(ServerLevel level, BlockPos start, BlockState state, Player player) {
        BlockPos target = new FurthestSearch(level, start, state.getBlock()).run();
        boolean broke = harvestFlagged(level, player, target);
        if (broke && isLog(level, start)) {
            scheduleTicks(level, target);
        }
        return broke;
    }

    public static boolean isLog(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(BlockTags.LOGS);
    }

    public static boolean isOre(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(Tags.Blocks.ORES);
    }

    public static ItemStack fortuneTool(ServerLevel level, Player player, boolean silkTouch, int fortune) {
        ItemStack held = player.getMainHandItem();
        Map<ResourceKey<Enchantment>, Integer> wanted = wantedEnchantments(silkTouch, fortune);
        return wanted.isEmpty() ? held.copy() : applyEnchantments(level, toolBase(held), wanted);
    }

    private static ItemStack toolBase(ItemStack held) {
        return held.isEmpty() ? new ItemStack(Items.NETHERITE_PICKAXE) : held.copy();
    }

    private static Map<ResourceKey<Enchantment>, Integer> wantedEnchantments(boolean silkTouch, int fortune) {
        Map<ResourceKey<Enchantment>, Integer> wanted = new LinkedHashMap<>();
        if (silkTouch) {
            wanted.put(Enchantments.SILK_TOUCH, SILK_TOUCH_LEVEL);
        }
        if (fortune > 0) {
            wanted.put(Enchantments.FORTUNE, fortune);
        }
        return wanted;
    }

    private static ItemStack applyEnchantments(ServerLevel level, ItemStack tool, Map<ResourceKey<Enchantment>, Integer> wanted) {
        HolderLookup.RegistryLookup<Enchantment> registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Map<Holder<Enchantment>, Integer> resolved = new LinkedHashMap<>();
        wanted.forEach((key, enchantLevel) -> resolved.put(registry.getOrThrow(key), enchantLevel));
        EnchantmentHelper.updateEnchantments(tool, mutable -> resolved.forEach(mutable::set));
        return tool;
    }

    private static boolean isBreakCancelled(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state) {
        GameType mode = player.gameMode.getGameModeForPlayer();
        return CommonHooks.fireBlockBreak(level, mode, player, pos, state).isCanceled();
    }

    private static List<ItemStack> collectDrops(ServerLevel level, Player player, BlockPos pos, BlockState state) {
        if (!yieldsDrops(level, player, pos, state)) {
            return List.of();
        }
        return Block.getDrops(state, level, pos, level.getBlockEntity(pos), player, player.getMainHandItem());
    }

    private static boolean yieldsDrops(ServerLevel level, Player player, BlockPos pos, BlockState state) {
        return !player.hasInfiniteMaterials() && EventHooks.doPlayerHarvestCheck(player, state, level, pos);
    }

    private static boolean harvestFlagged(ServerLevel level, Player player, BlockPos target) {
        try (FlagScope ignored = new FlagScope()) {
            return harvestBlock(level, player, target, false);
        }
    }

    private static List<Vec3i> neighbourOffsets() {
        List<Vec3i> offsets = new ArrayList<>(NEIGHBOUR_CELLS - 1);
        int centre = NEIGHBOUR_CELLS / 2;
        for (int cell = 0; cell < NEIGHBOUR_CELLS; cell++) {
            if (cell == centre) {
                continue;
            }
            int dx = cell / (NEIGHBOUR_SIDE * NEIGHBOUR_SIDE) - NEIGHBOUR_RANGE;
            int dy = cell / NEIGHBOUR_SIDE % NEIGHBOUR_SIDE - NEIGHBOUR_RANGE;
            int dz = cell % NEIGHBOUR_SIDE - NEIGHBOUR_RANGE;
            offsets.add(new Vec3i(dx, dy, dz));
        }
        return List.copyOf(offsets);
    }

    private static void scheduleTicks(ServerLevel level, BlockPos center) {
        int reach = TICK_CUBE_RADIUS;
        RandomSource random = level.getRandom();
        Iterable<BlockPos> cube = BlockPos.betweenClosed(center.offset(-reach, -reach, -reach), center.offset(reach, reach, reach));
        cube.forEach(cell -> scheduleOne(level, cell, random));
    }

    private static void scheduleOne(ServerLevel level, BlockPos cell, RandomSource random) {
        Block occupant = level.getBlockState(cell).getBlock();
        level.scheduleTick(cell.immutable(), occupant, TICK_DELAY_BASE + random.nextInt(TICK_DELAY_SPREAD));
    }

    private static final class FlagScope implements AutoCloseable {
        private FlagScope() {
            HARVESTING_FURTHEST.set(Boolean.TRUE);
        }

        @Override
        public void close() {
            HARVESTING_FURTHEST.remove();
        }
    }

    private static final class FurthestSearch {
        private final Set<Long> visited = new HashSet<>();
        private final BlockPos origin;
        private final ServerLevel level;
        private final Block block;

        private FurthestSearch(ServerLevel level, BlockPos start, Block block) {
            this.origin = start.immutable();
            this.level = level;
            this.block = block;
            visited.add(origin.asLong());
        }

        private BlockPos run() {
            List<BlockPos> outermost = List.of(origin);
            for (List<BlockPos> ring = expand(outermost); !ring.isEmpty(); ring = expand(outermost)) {
                outermost = ring;
            }
            return Collections.max(outermost, Comparator.comparingDouble(pos -> pos.distSqr(origin)));
        }

        private List<BlockPos> expand(List<BlockPos> inner) {
            List<BlockPos> found = new ArrayList<>();
            BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
            for (BlockPos member : inner) {
                for (Vec3i offset : NEIGHBOUR_OFFSETS) {
                    if (visited.size() >= SEARCH_MAX_POSITIONS) {
                        return found;
                    }
                    probe.setWithOffset(member, offset);
                    if (qualifies(probe) && visited.add(probe.asLong())) {
                        found.add(probe.immutable());
                    }
                }
            }
            return found;
        }

        private boolean qualifies(BlockPos pos) {
            if (insideBox(pos) && level.hasChunkAt(pos)) {
                BlockState found = level.getBlockState(pos);
                return found.is(block) && found.getDestroySpeed(level, pos) >= 0.0F;
            }
            return false;
        }

        private boolean insideBox(BlockPos pos) {
            int flat = Math.max(Math.abs(pos.getX() - origin.getX()), Math.abs(pos.getZ() - origin.getZ()));
            return flat <= SEARCH_RADIUS_HORIZONTAL && Math.abs(pos.getY() - origin.getY()) <= SEARCH_RADIUS_VERTICAL;
        }
    }
}
