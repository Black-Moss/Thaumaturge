package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.warding.WardHandler;
import com.leclowndu93150.thaumaturge.content.wands.WandChargingEvents;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

final class NodeHunger {
    private static final int PULL_REFRESH_INTERVAL = 10;
    private static final double GLOBAL_PULL_RANGE = 15.0;
    private static final double ITEM_RANGE_PADDING = 0.5;
    private static final double MINIMUM_PULL_DISTANCE = 1.0E-4;
    private static final float DEVOUR_DAMAGE = 1.0F;
    private static final float RANGE_PALE_DIVISOR = 3.0F;
    private static final float RANGE_AVERAGE_NUMERATOR = 2.0F;
    private static final double SIDEWAYS_DRAG = 0.15;
    private static final double UPRIGHT_DRAG = 0.25;
    private static final double MAW_RADIUS_SQUARED = 2.0;
    private static final float GROWTH_CAPACITY_WEIGHT = 2.0F;
    private static final int FRAGMENTS_PER_GLIMPSE = 3;
    private static final double FRAGMENT_SPEED = 0.3;
    private static final double FRAGMENT_LIFT = 0.05;

    private final BlockEntityNode node;
    private final List<Entity> pulled = new ArrayList<>();

    NodeHunger(BlockEntityNode node) {
        this.node = node;
    }

    static int eatRange(@Nullable NodeModifier modifier) {
        int fixed = ThaumaturgeCommonConfig.HUNGRY_NODE_BLOCK_EAT_RANGE.get();
        if (!ThaumaturgeCommonConfig.SCALE_HUNGRY_NODE_RANGE_BY_MODIFIER.get()) {
            return fixed;
        }
        int minimum = ThaumaturgeCommonConfig.HUNGRY_NODE_MINIMUM_BLOCK_EAT_RANGE.get();
        int maximum = Math.max(minimum, ThaumaturgeCommonConfig.HUNGRY_NODE_MAXIMUM_BLOCK_EAT_RANGE.get());
        int span = maximum - minimum;
        if (modifier == NodeModifier.BRIGHT) {
            return maximum;
        }
        if (modifier == NodeModifier.FADING) {
            return minimum;
        }
        return modifier == NodeModifier.PALE ? minimum + Math.round(span / RANGE_PALE_DIVISOR) : minimum + Math.round(span * RANGE_AVERAGE_NUMERATOR / RANGE_PALE_DIVISOR);
    }

    static boolean eatDue(int counter) {
        return counter % ThaumaturgeCommonConfig.HUNGRY_NODE_BLOCK_EAT_INTERVAL.get() == 0;
    }

    private static boolean isPullable(Entity entity) {
        return entity.isAlive() && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    void tick(ServerLevel level, BlockPos pos, int counter) {
        Vec3 centre = Vec3.atCenterOf(pos);
        double itemReach = eatRange(node.trait()) + ITEM_RANGE_PADDING;
        if (counter % PULL_REFRESH_INTERVAL == 0) {
            gather(level, pos, Math.max(itemReach, GLOBAL_PULL_RANGE));
        }
        if (pulled.isEmpty()) {
            return;
        }
        RandomSource random = level.getRandom();
        for (Entity entity : pulled) {
            if (!isPullable(entity) || entity.level() != level) {
                continue;
            }
            Vec3 toCentre = centre.subtract(entity.position());
            double distance = toCentre.length();
            double reach = entity instanceof ItemEntity ? itemReach : GLOBAL_PULL_RANGE;
            if (distance * distance < MAW_RADIUS_SQUARED) {
                swallow(level, entity, random);
            }
            if (distance > MINIMUM_PULL_DISTANCE && distance < reach) {
                drag(entity, toCentre, distance, reach);
            }
        }
    }

    private void gather(ServerLevel level, BlockPos pos, double reach) {
        pulled.clear();
        AABB area = new AABB(pos).inflate(reach);
        pulled.addAll(level.getEntitiesOfClass(Entity.class, area, NodeHunger::isPullable));
    }

    private static void drag(Entity entity, Vec3 toCentre, double distance, double reach) {
        double closeness = 1.0 - distance / reach;
        double strength = closeness * closeness / distance;
        entity.push(toCentre.x * SIDEWAYS_DRAG * strength, toCentre.y * UPRIGHT_DRAG * strength, toCentre.z * SIDEWAYS_DRAG * strength);
        if (entity instanceof Player) {
            entity.hurtMarked = true;
        }
    }

    private void swallow(ServerLevel level, Entity entity, RandomSource random) {
        AspectList essence = essenceOf(entity);
        boolean wasPresent = entity.isAlive();
        entity.hurtServer(level, level.damageSources().fellOutOfWorld(), DEVOUR_DAMAGE);
        if (wasPresent && !entity.isAlive() && !essence.isEmpty()) {
            feed(level, essence, random);
        }
    }

    private static AspectList essenceOf(Entity entity) {
        if (entity instanceof ItemEntity item) {
            return AspectIndexAccess.of(item.getItem().copyWithCount(1));
        }
        if (entity instanceof LivingEntity) {
            return EntityAspects.of(entity);
        }
        return AspectList.EMPTY;
    }

    private void feed(ServerLevel level, AspectList essence, RandomSource random) {
        List<Map.Entry<ResourceKey<IAspect>, Integer>> primals = new ArrayList<>(WandChargingEvents.reduceToPrimals(essence).entrySet());
        if (primals.isEmpty()) {
            return;
        }
        Map.Entry<ResourceKey<IAspect>, Integer> chosen = primals.get(random.nextInt(primals.size()));
        Holder<IAspect> primal = Aspects.resolve(level, chosen.getKey());
        if (primal == null) {
            return;
        }
        int capacity = node.aspectsBase.amountOf(primal);
        if (node.held.amountOf(primal) < capacity) {
            node.held = node.held.add(primal, 1);
        } else if (random.nextFloat() < Math.min(1.0F, chosen.getValue() / (1.0F + GROWTH_CAPACITY_WEIGHT * capacity))) {
            node.aspectsBase = node.aspectsBase.add(primal, 1);
        } else {
            return;
        }
        node.invalidateRefill();
    }

    static @Nullable BlockPos findTarget(Level level, BlockPos pos, @Nullable NodeModifier modifier, RandomSource random) {
        int reach = eatRange(modifier);
        BlockPos scattered = NodeRules.scatter(pos, reach, random);
        if (!level.hasChunkAt(scattered)) {
            return null;
        }
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING, scattered.getX(), scattered.getZ());
        BlockPos aim = new BlockPos(scattered.getX(), Math.min(scattered.getY(), surface), scattered.getZ());
        BlockHitResult hit = level.clip(new SourceIgnoringClipContext(pos, Vec3.atCenterOf(pos), Vec3.atCenterOf(aim), ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos struck = hit.getBlockPos();
        return isEdible(level, pos, struck, reach) ? struck : null;
    }

    private static boolean isEdible(Level level, BlockPos pos, BlockPos struck, int reach) {
        if (struck.distSqr(pos) > (double) reach * reach || WardHandler.isWarded(level, struck)) {
            return false;
        }
        BlockState state = level.getBlockState(struck);
        if (state.isAir()) {
            return false;
        }
        float hardness = state.getDestroySpeed(level, struck);
        return hardness >= 0.0F && hardness < ThaumaturgeCommonConfig.HUNGRY_NODE_BLOCK_HARDNESS.get();
    }

    static void predict(Level level, BlockPos pos, @Nullable NodeModifier modifier) {
        RandomSource random = level.getRandom();
        BlockPos target = findTarget(level, pos, modifier, random);
        if (target == null) {
            return;
        }
        BlockParticleOption shard = new BlockParticleOption(ParticleTypes.BLOCK, level.getBlockState(target));
        Vec3 stream = Vec3.atCenterOf(pos).subtract(Vec3.atCenterOf(target)).normalize().scale(FRAGMENT_SPEED);
        for (int fragment = 0; fragment < FRAGMENTS_PER_GLIMPSE; fragment++) {
            level.addParticle(shard, target.getX() + random.nextFloat(), target.getY() + random.nextFloat(), target.getZ() + random.nextFloat(), stream.x, stream.y + FRAGMENT_LIFT, stream.z);
        }
    }
}
