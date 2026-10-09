package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealArea;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.casters.BlockBreakerEngine;
import com.leclowndu93150.thaumaturge.content.golem.CropUtils;
import com.leclowndu93150.thaumaturge.content.golem.GolemInteractionHelper;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.server.TTFakePlayer;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jspecify.annotations.Nullable;

public final class HarvestBehavior implements ISealBehavior {
    public static final SealSetting REPLANT = new SealSetting("replant_crops", "gui.thaumaturge.seal.setting.replant", true);
    public static final SealSetting REQUEST_SEEDS = new SealSetting("request_seeds", "gui.thaumaturge.seal.setting.provision", false);
    private static final String SITES_KEY = "replant_sites";
    private static final String LEGACY_SITES_KEY = "replant";
    public static final MapCodec<HarvestBehavior> CODEC = RecordCodecBuilder
            .mapCodec(
                    instance -> instance
                            .group(ReplantSite.CODEC.codec().listOf().optionalFieldOf(SITES_KEY).forGetter(HarvestBehavior::storedSites),
                                    ReplantSite.LEGACY_CODEC.codec().listOf().lenientOptionalFieldOf(LEGACY_SITES_KEY).forGetter(behavior -> Optional.empty()))
                            .apply(instance, HarvestBehavior::restore));

    private static final int STAGGER = 33;
    private static final int SCAN_PERIOD = 5;
    private static final int MAINTENANCE_PERIOD = 100;
    private static final int REPLANT_LIFE = 300;
    private static final int HARVEST_XP = 1;
    private static final int LEVEL_EVENT_BLOCK_BREAK = 2001;
    private static final int NO_FORTUNE = 0;

    private final SealClock clock = new SealClock(STAGGER);
    private final TaskLedger<BlockPos> harvests = new TaskLedger<>();
    private final Map<BlockPos, ReplantSite> sites = new LinkedHashMap<>();
    private int walk;

    public HarvestBehavior() {}

    private static HarvestBehavior restore(Optional<List<ReplantSite>> current, Optional<List<ReplantSite>> legacy) {
        return new HarvestBehavior(current.or(() -> legacy).orElse(List.of()));
    }

    private HarvestBehavior(List<ReplantSite> stored) {
        for (ReplantSite site : stored) {
            sites.put(site.pos(), site);
        }
    }

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        int step = clock.advance();
        if (step % MAINTENANCE_PERIOD == 0) {
            pruneSites(seal);
            harvests.dropFinished(level);
        }
        if (step % SCAN_PERIOD != 0) {
            return;
        }
        BlockPos cell = SealArea.cell(seal, walk++);
        if (!level.hasChunkAt(cell)) {
            return;
        }
        if (CropUtils.isGrownCrop(level, cell)) {
            if (!harvests.tracks(cell)) {
                Task task = Task.atBlock(seal.pos(), cell);
                task.setPriority(seal.priority());
                GolemHelper.addGolemTask(level, task);
                harvests.record(task, cell);
            }
            return;
        }
        ReplantSite site = sites.get(cell);
        if (site != null && seal.setting(REPLANT) && level.getBlockState(cell).isAir() && !hasLiveTask(level, site)) {
            Task task = Task.atBlock(seal.pos(), cell);
            task.setPriority(seal.priority());
            GolemHelper.addGolemTask(level, task);
            sites.put(cell, site.withTask(task));
        }
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        ReplantSite site = siteFor(task);
        if (site != null) {
            replant(level, golem, site);
        } else {
            harvest(level, seal, golem, task);
        }
        harvests.forget(task);
        task.end();
        return true;
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        ReplantSite site = siteFor(task);
        if (site == null) {
            return true;
        }
        boolean holdsSeed = golem.hands().holds(site.seed());
        if (!holdsSeed && seal.setting(REQUEST_SEEDS)) {
            GolemHelper.requestProvisioning(golem.level(), seal, site.seed());
        }
        return holdsSeed;
    }

    private Optional<List<ReplantSite>> storedSites() {
        return sites.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(sites.values()));
    }

    private @Nullable ReplantSite siteFor(Task task) {
        ReplantSite site = sites.get(task.pos());
        return site != null && task.equals(site.task()) ? site : null;
    }

    private static boolean hasLiveTask(ServerLevel level, ReplantSite site) {
        Task task = site.task();
        return task != null && !task.isEnded() && TaskBoard.of(level).isLive(task.id());
    }

    private void pruneSites(ISealEntity seal) {
        AABB area = SealArea.bounds(seal);
        sites.values().removeIf(site -> isOutside(area, site));
    }

    private static boolean isOutside(AABB area, ReplantSite site) {
        if (area.contains(Vec3.atCenterOf(site.pos()))) {
            return false;
        }
        Task task = site.task();
        if (task != null) {
            task.end();
        }
        return true;
    }

    private void harvest(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        BlockPos pos = task.pos();
        BlockState state = level.getBlockState(pos);
        if (!CropUtils.isGrownCrop(level, pos)) {
            return;
        }
        if (CropUtils.isClickableCrop(state)) {
            pick(level, golem, pos, state);
            return;
        }
        GolemInteractionHelper.golemClick(level, golem, pos, Direction.UP, ItemStack.EMPTY, false, false);
        if (!CropUtils.isGrownCrop(level, pos)) {
            return;
        }
        BlockState grown = level.getBlockState(pos);
        ItemStack seed = CropUtils.getSeed(level, pos, grown);
        BlockBreakerEngine.harvestBlock(level, TTFakePlayer.GOLEM.at(level, golem.asEntity()), pos, false, NO_FORTUNE);
        golem.addRankXp(HARVEST_XP);
        golem.swingArm();
        if (seal.setting(REPLANT) && !seed.isEmpty() && level.getBlockState(pos).isAir()) {
            planReplant(level, task, pos, grown, seed);
        }
    }

    private static void pick(ServerLevel level, IGolemAPI golem, BlockPos pos, BlockState state) {
        FakePlayer player = TTFakePlayer.GOLEM.at(level, golem.asEntity());
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        state.useWithoutItem(level, player, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        golem.addRankXp(HARVEST_XP);
        golem.swingArm();
    }

    private void planReplant(ServerLevel level, Task reaped, BlockPos pos, BlockState crop, ItemStack seed) {
        BlockState soil = level.getBlockState(pos.below());
        boolean cocoa = crop.getBlock() instanceof CocoaBlock;
        boolean farmland = soil.is(Blocks.FARMLAND);
        Direction face;
        if (!cocoa && soil.is(BlockTags.SUBSTRATE_OVERWORLD) || farmland) {
            face = Direction.DOWN;
        } else if (cocoa) {
            face = crop.getValue(CocoaBlock.FACING);
        } else {
            return;
        }
        Task task = Task.atBlock(reaped.origin(), pos);
        task.setPriority(reaped.priority());
        task.setLife(REPLANT_LIFE);
        GolemHelper.addGolemTask(level, task);
        BlockPos key = pos.immutable();
        sites.put(key, new ReplantSite(key, face, seed.copyWithCount(1), farmland, task));
    }

    private void replant(ServerLevel level, IGolemAPI golem, ReplantSite site) {
        BlockPos pos = site.pos();
        if (!level.getBlockState(pos).isAir() || !golem.hands().holds(site.seed())) {
            return;
        }
        BlockPos soilPos = pos.below();
        BlockState soil = level.getBlockState(soilPos);
        if (site.tilled() && soil.is(BlockTags.SUBSTRATE_OVERWORLD) && !soil.is(Blocks.FARMLAND)) {
            till(level, golem, soilPos);
        }
        ItemStack planting = golem.hands().release(site.seed().copyWithCount(1));
        if (planting.isEmpty()) {
            return;
        }
        GolemInteractionHelper.golemClick(level, golem, pos.relative(site.face()), site.face().getOpposite(), planting, false, false);
        BlockState planted = level.getBlockState(pos);
        if (!planted.isAir()) {
            level.levelEvent(LEVEL_EVENT_BLOCK_BREAK, pos, Block.getId(planted));
        }
    }

    private static void till(ServerLevel level, IGolemAPI golem, BlockPos soilPos) {
        FakePlayer player = TTFakePlayer.GOLEM.at(level, golem.asEntity());
        ItemStack hoe = new ItemStack(Items.DIAMOND_HOE);
        player.setItemInHand(InteractionHand.MAIN_HAND, hoe);
        try {
            hoe.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(soilPos), Direction.UP, soilPos, false)));
        } finally {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }
}
