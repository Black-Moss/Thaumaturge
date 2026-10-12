package com.leclowndu93150.thaumaturge.content.device.bore;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityLampArcane;
import com.leclowndu93150.thaumaturge.content.device.BlockLamp;
import com.leclowndu93150.thaumaturge.content.equipment.RefiningResults;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jspecify.annotations.Nullable;

public final class ArcaneBoreCore {
    public static final float MAX_CHARGE = 10.0F;
    public static final float IDLE_YAW_STEP = 10.0F;
    public static final float IDLE_PITCH_STEP = 33.0F;
    public static final float DIG_PITCH_STEP = 90.0F;

    private static final int LEVEL_EVENT_BLOCK_BREAK = 2001;
    private static final int RECHARGE_INTERVAL = 10;
    private static final float DIG_COST = 0.25F;
    private static final int WEAR_INTERVAL = 50;
    private static final int SOUND_SPACING = 24;
    private static final int SOUND_JITTER_RANGE = 2;
    private static final float SOUND_VOLUME = 0.25F;
    private static final float SOUND_PITCH_MIN = 0.9F;
    private static final float SOUND_PITCH_SPREAD = 0.2F;
    private static final double DROP_RANGE = 1.5;
    private static final float REFINE_CHANCE_PER_STEP = 0.125F;
    private static final int INACTIVE_AIM_DISTANCE = 1;
    private static final int ACTIVE_AIM_DISTANCE = 2;
    private static final double TARGET_CENTER = 0.5;
    private static final int BASE_DELAY = 10;
    private static final int HARDNESS_DELAY_FACTOR = 2;
    private static final int SPEED_DELAY_FACTOR = 2;
    private static final int MIN_DELAY = 1;
    private static final float UNBREAKABLE = -1.0F;
    private static final int SCAN_FULL_TURN = 360;
    private static final int SCAN_BASE_STEP = 3;
    private static final int SCAN_STEP_RADIUS_SPAN = 10;
    private static final int SCAN_STEP_FACTOR = 2;
    private static final double SCAN_RESTART_MARGIN = 1.0;
    private static final int FULL_LIGHT = 15;
    private static final int LIGHT_PHASES = 4;
    private static final int LIGHT_PHASE_SPAN = 2;
    private static final int LIGHT_MAX_SPREAD = 3;
    private static final int LIGHT_LOWERING = 2;
    private static final int LIGHT_PHASE_LOWERED = 3;
    private static final int LIGHT_PHASE_RIGHT = 0;
    private static final int LIGHT_PHASE_LEFT = 2;
    private static final Direction[] DIRECTIONS = Direction.values();

    private float charge;
    private @Nullable BlockPos target;
    private int delay;
    private int angle;
    private int radius;
    private @Nullable BlockPos lastCell;
    private int nextSoundTick;
    private int wear;

    public @Nullable BlockPos digTarget() {
        return target;
    }

    public float charge() {
        return charge;
    }

    public void setCharge(float charge) {
        this.charge = charge;
    }

    public void serverTick(ArcaneBoreHost host, ServerLevel level, int tickCount) {
        if (tickCount % RECHARGE_INTERVAL == 0) {
            recharge(host, level);
        }
        ItemStack tool = host.boreTool();
        boolean active = host.boreActive();
        if (!active || !host.boreValid() || !ArcaneBoreTool.valid(tool)) {
            target = null;
            delay = 0;
            host.setBoreDigging(false);
            if (!active) {
                aimRest(host);
            }
            return;
        }
        if (target != null && charge >= DIG_COST) {
            if (delay > 0) {
                delay--;
            } else {
                dig(host, level, tool, tickCount);
            }
        }
        if (target == null && charge >= DIG_COST) {
            findTarget(host, level, tool);
        }
        host.setBoreDigging(target != null);
        if (target != null) {
            aimDig(host, target);
        } else {
            aimSearch(host);
        }
    }

    public static void aimDig(ArcaneBoreHost host, BlockPos digTarget) {
        host.aimBore(digTarget.getX() + TARGET_CENTER, digTarget.getY() + TARGET_CENTER, digTarget.getZ() + TARGET_CENTER, IDLE_YAW_STEP, DIG_PITCH_STEP);
    }

    private static void aimRest(ArcaneBoreHost host) {
        Direction facing = host.boreFacing();
        Vec3 base = host.borePosition();
        host.aimBore(base.x + facing.getStepX() * (double) INACTIVE_AIM_DISTANCE, base.y, base.z + facing.getStepZ() * (double) INACTIVE_AIM_DISTANCE, IDLE_YAW_STEP, IDLE_PITCH_STEP);
    }

    private static void aimSearch(ArcaneBoreHost host) {
        Direction facing = host.boreFacing();
        Vec3 base = host.borePosition();
        host.aimBore(base.x + facing.getStepX() * (double) ACTIVE_AIM_DISTANCE, base.y + facing.getStepY() * (double) ACTIVE_AIM_DISTANCE + host.boreEyeHeight(), base.z + facing.getStepZ() * (double) ACTIVE_AIM_DISTANCE, IDLE_YAW_STEP, IDLE_PITCH_STEP);
    }

    private void recharge(ArcaneBoreHost host, ServerLevel level) {
        float missing = MAX_CHARGE - charge;
        if (missing <= 0.0F) {
            return;
        }
        charge += AuraHelper.drainVis(level, host.borePos(), missing, false);
    }

    private void findTarget(ArcaneBoreHost host, ServerLevel level, ItemStack tool) {
        BlockPos candidate = probe(host, level, tool);
        if (candidate == null) {
            return;
        }
        BlockState state = level.getBlockState(candidate);
        int speed = ArcaneBoreTool.digSpeed(level, tool, state);
        float hardness = state.getDestroySpeed(level, candidate);
        delay = Math.max(MIN_DELAY, Math.max(BASE_DELAY - speed, Mth.floor(HARDNESS_DELAY_FACTOR * hardness) - SPEED_DELAY_FACTOR * speed));
        target = candidate;
        host.showBoreDig(level, candidate, delay);
    }

    private void dig(ArcaneBoreHost host, ServerLevel level, ItemStack tool, int tickCount) {
        BlockPos pos = target;
        target = null;
        if (pos == null || !level.hasChunkAt(pos)) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return;
        }
        AABB area = dropArea(pos);
        Set<ItemEntity> preexisting = new HashSet<>(level.getEntitiesOfClass(ItemEntity.class, area));
        if (!breakAsPlayer(host, level, tool, pos)) {
            return;
        }
        List<ItemStack> drops = collectDrops(level, area, preexisting);
        refine(drops, state, tool, level, host.boreRandom());
        drops.forEach(drop -> deliver(host, level, drop));
        charge = Math.max(0.0F, charge - DIG_COST);
        wear(host);
        playSound(host, tickCount);
        lightTunnel(host, level, tool);
    }

    private void wear(ArcaneBoreHost host) {
        if (++wear >= WEAR_INTERVAL) {
            wear = 0;
            host.hurtBoreTool();
        }
    }

    private void playSound(ArcaneBoreHost host, int tickCount) {
        if (tickCount < nextSoundTick) {
            return;
        }
        RandomSource random = host.boreRandom();
        host.playBoreSound(TTSounds.RUMBLE.get(), SOUND_VOLUME, SOUND_PITCH_MIN + random.nextFloat() * SOUND_PITCH_SPREAD);
        nextSoundTick = tickCount + SOUND_SPACING + random.nextInt(SOUND_JITTER_RANGE);
    }

    private static void lightTunnel(ArcaneBoreHost host, ServerLevel level, ItemStack tool) {
        if (!hasLitArcaneLamp(level, host.borePos())) {
            return;
        }
        BlockPos spot = pickGlimmerSpot(host, tool);
        if (level.hasChunkAt(spot) && level.getBlockState(spot).isAir() && level.getBrightness(LightLayer.BLOCK, spot) < FULL_LIGHT) {
            level.setBlock(spot, TTBlocks.EFFECT_GLIMMER.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private static BlockPos pickGlimmerSpot(ArcaneBoreHost host, ItemStack tool) {
        Direction facing = host.boreFacing();
        int depth = ArcaneBoreTool.digDepth(tool);
        int distance = LIGHT_PHASE_SPAN * host.boreRandom().nextInt(Math.max(1, depth / LIGHT_PHASE_SPAN));
        BlockPos spot = host.borePos().relative(facing, 1 + distance);
        int shift = Math.min(LIGHT_MAX_SPREAD, ArcaneBoreTool.digRadius(tool));
        Direction.Axis sideAxis = facing.getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        Direction side = Direction.fromAxisAndDirection(sideAxis, Direction.AxisDirection.POSITIVE);
        int phase = distance / LIGHT_PHASE_SPAN % LIGHT_PHASES;
        if (phase == LIGHT_PHASE_RIGHT) {
            return spot.relative(side, shift);
        }
        if (phase == LIGHT_PHASE_LEFT) {
            return spot.relative(side.getOpposite(), shift);
        }
        if (phase == LIGHT_PHASE_LOWERED && facing.getAxis() != Direction.Axis.Y) {
            return spot.below(LIGHT_LOWERING);
        }
        return spot;
    }

    private @Nullable BlockPos probe(ArcaneBoreHost host, ServerLevel level, ItemStack tool) {
        int digRadius = ArcaneBoreTool.digRadius(tool);
        int depth = ArcaneBoreTool.digDepth(tool);
        Direction facing = host.boreFacing();
        BlockPos bore = host.borePos();
        BlockPos cell = lastCell == null ? bore : lastCell;
        double reach = digRadius + SCAN_RESTART_MARGIN;
        if (Vec3.atCenterOf(cell).distanceToSqr(host.borePosition()) > reach * reach) {
            cell = bore;
        }
        lastCell = cell;
        BlockPos found = trace(host, level, cell, facing, depth);
        if (found == null) {
            lastCell = nextCell(host, facing, cell, digRadius);
        }
        return found;
    }

    private @Nullable BlockPos trace(ArcaneBoreHost host, ServerLevel level, BlockPos cell, Direction facing, int depth) {
        BlockPos endCell = cell.relative(facing, depth);
        if (!level.hasChunkAt(cell) || !level.hasChunkAt(endCell)) {
            return null;
        }
        BlockHitResult hit = level.clip(new ClipContext(Vec3.atCenterOf(cell), Vec3.atCenterOf(endCell), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, host.boreCollisionContext()));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        Vec3 from = host.boreEye().add(facing.getStepX(), facing.getStepY(), facing.getStepZ());
        if (!level.hasChunkAt(BlockPos.containing(from))) {
            return null;
        }
        BlockHitResult second = level.clip(new ClipContext(from, Vec3.atCenterOf(hit.getBlockPos()), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, host.boreCollisionContext()));
        if (second.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos pos = second.getBlockPos();
        if (pos.equals(host.borePos()) || pos.equals(host.borePos().below())) {
            return null;
        }
        BlockState state = level.getBlockState(pos);
        if (state.getCollisionShape(level, pos).isEmpty() || state.getDestroySpeed(level, pos) <= UNBREAKABLE) {
            return null;
        }
        return pos;
    }

    private BlockPos nextCell(ArcaneBoreHost host, Direction facing, BlockPos probed, int digRadius) {
        BlockPos next;
        do {
            if (radius > digRadius) {
                radius = digRadius;
            }
            angle += (int) (SCAN_BASE_STEP + Math.max(0, (SCAN_STEP_RADIUS_SPAN - radius) * SCAN_STEP_FACTOR));
            if (angle >= SCAN_FULL_TURN) {
                angle -= SCAN_FULL_TURN;
                radius++;
                if (radius > digRadius) {
                    radius = 0;
                }
            }
            next = BlockPos.containing(scanPoint(host, facing, radius, angle));
        } while (next.equals(probed));
        return next;
    }

    private static Vec3 scanPoint(ArcaneBoreHost host, Direction facing, int scanRadius, int degrees) {
        Vec3 base = host.borePosition();
        double x = base.x + facing.getStepX();
        double y = base.y + facing.getStepY() + host.boreEyeHeight();
        double z = base.z + facing.getStepZ();
        double theta = Math.toRadians(degrees);
        double cos = scanRadius * Math.cos(theta);
        double sin = scanRadius * Math.sin(theta);
        return switch (facing) {
            case NORTH, SOUTH -> new Vec3(x - sin, y + cos, z);
            case EAST -> new Vec3(x, y + cos, z + sin);
            case WEST -> new Vec3(x, y + cos, z - sin);
            case UP -> new Vec3(x - sin, y, z + cos);
            case DOWN -> new Vec3(x - sin, y, z - cos);
        };
    }

    private boolean breakAsPlayer(ArcaneBoreHost host, ServerLevel level, ItemStack tool, BlockPos pos) {
        FakePlayer digger = host.boreDigger(level);
        ItemStack held = tool.copy();
        int fortune = ArcaneBoreTool.fortune(level, tool);
        if (fortune > 0) {
            Holder<Enchantment> holder = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
            if (fortune > EnchantmentHelper.getItemEnchantmentLevel(holder, held)) {
                EnchantmentHelper.updateEnchantments(held, enchantments -> enchantments.set(holder, fortune));
            }
        }
        digger.setItemInHand(InteractionHand.MAIN_HAND, held);
        BlockState state = level.getBlockState(pos);
        boolean removed = false;
        if (!CommonHooks.fireBlockBreak(level, digger.gameMode.getGameModeForPlayer(), digger, pos, state).isCanceled()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            level.levelEvent(null, LEVEL_EVENT_BLOCK_BREAK, pos, Block.getId(state));
            removed = level.removeBlock(pos, false);
            if (removed) {
                Block.dropResources(state, level, pos, blockEntity, digger, held);
            }
        }
        digger.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return removed;
    }

    private static AABB dropArea(BlockPos pos) {
        Vec3 lower = Vec3.atLowerCornerOf(pos);
        return new AABB(lower.subtract(DROP_RANGE, DROP_RANGE, DROP_RANGE), lower.add(1.0 + DROP_RANGE, 1.0 + DROP_RANGE, 1.0 + DROP_RANGE));
    }

    private static List<ItemStack> collectDrops(ServerLevel level, AABB area, Set<ItemEntity> preexisting) {
        List<ItemEntity> fresh = level.getEntitiesOfClass(ItemEntity.class, area, candidate -> !preexisting.contains(candidate));
        List<ItemStack> drops = fresh.stream().map(entity -> entity.getItem().copy()).collect(Collectors.toCollection(ArrayList::new));
        fresh.forEach(ItemEntity::discard);
        return drops;
    }

    private static void refine(List<ItemStack> drops, BlockState broken, ItemStack tool, ServerLevel level, RandomSource random) {
        int refining = ArcaneBoreTool.refining(tool);
        if (refining <= 0 || ArcaneBoreTool.silkTouch(level, tool)) {
            return;
        }
        Item cluster = RefiningResults.clusterFor(broken);
        if (cluster == null) {
            return;
        }
        for (int i = 0; i < drops.size(); i++) {
            if (random.nextFloat() < (refining + 1) * REFINE_CHANCE_PER_STEP) {
                drops.set(i, new ItemStack(cluster, drops.get(i).getCount()));
            }
        }
    }

    private static void deliver(ArcaneBoreHost host, ServerLevel level, ItemStack drop) {
        ItemStack rest = drop;
        for (Direction direction : DIRECTIONS) {
            if (rest.isEmpty()) {
                return;
            }
            BlockPos neighbor = host.borePos().relative(direction);
            if (level.hasChunkAt(neighbor)) {
                rest = InvHelper.insertAt(level, neighbor, direction.getOpposite(), rest, false);
            }
        }
        if (!rest.isEmpty()) {
            host.dropBoreOutput(level, rest);
        }
    }

    private static boolean hasLitArcaneLamp(ServerLevel level, BlockPos origin) {
        return Arrays.stream(DIRECTIONS).map(origin::relative).filter(level::hasChunkAt).anyMatch(pos -> isLitArcaneLamp(level, pos));
    }

    private static boolean isLitArcaneLamp(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof BlockLamp && state.getValue(BlockStateProperties.ENABLED) && level.getBlockEntity(pos) instanceof BlockEntityLampArcane;
    }
}
