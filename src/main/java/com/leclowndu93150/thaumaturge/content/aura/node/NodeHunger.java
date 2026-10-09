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
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
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
    private static final double HORIZONTAL_PULL = 0.15;
    private static final double VERTICAL_PULL = 0.25;
    private static final double MINIMUM_PULL_DISTANCE = 1.0E-4;
    private static final double DEVOUR_DISTANCE = 1.4;
    private static final float DEVOUR_DAMAGE = 1.0F;
    private static final int RANGE_PALE_DIVISOR = 3;
    private static final int RANGE_AVERAGE_NUMERATOR = 2;
    private static final float BASE_GROWTH_FACTOR = 2.0F;
    private static final int PARTICLE_COUNT = 3;
    private static final double PARTICLE_SPEED = 0.3;
    private static final double PARTICLE_LIFT = 0.05;

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
        return modifier == NodeModifier.PALE ? minimum + span / RANGE_PALE_DIVISOR : minimum + span * RANGE_AVERAGE_NUMERATOR / RANGE_PALE_DIVISOR;
    }

    void tick(ServerLevel level, BlockPos pos, int counter) {
        double itemRange = eatRange(node.trait()) + ITEM_RANGE_PADDING;
        if (counter % PULL_REFRESH_INTERVAL == 0) {
            refresh(level, pos, Math.max(itemRange, GLOBAL_PULL_RANGE));
        }
        pullAndDevour(level, pos, itemRange);
    }

    static boolean eatDue(int counter) {
        return counter % ThaumaturgeCommonConfig.HUNGRY_NODE_BLOCK_EAT_INTERVAL.get() == 0;
    }

    static void predict(Level level, BlockPos pos, @Nullable NodeModifier modifier) {
        RandomSource random = level.getRandom();
        BlockPos target = findTarget(level, pos, modifier, random);
        if (target == null) {
            return;
        }
        ParticleOptions fragment = new BlockParticleOption(ParticleTypes.BLOCK, level.getBlockState(target));
        Vec3 push = Vec3.atCenterOf(pos).subtract(Vec3.atCenterOf(target)).normalize().scale(PARTICLE_SPEED);
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            level.addParticle(fragment, target.getX() + random.nextDouble(), target.getY() + random.nextDouble(), target.getZ() + random.nextDouble(), push.x, push.y + PARTICLE_LIFT, push.z);
        }
    }

    static @Nullable BlockPos findTarget(Level level, BlockPos origin, @Nullable NodeModifier modifier, RandomSource random) {
        double hardnessCap = ThaumaturgeCommonConfig.HUNGRY_NODE_BLOCK_HARDNESS.get();
        if (hardnessCap <= 0.0) {
            return null;
        }
        int range = eatRange(modifier);
        BlockPos candidate = origin.offset(spread(random, range), spread(random, range), spread(random, range));
        if (!level.hasChunkAt(candidate) || candidate.getY() >= level.getHeight(Heightmap.Types.WORLD_SURFACE, candidate.getX(), candidate.getZ())) {
            return null;
        }
        ClipContext context = new SourceIgnoringClipContext(origin, Vec3.atCenterOf(origin), Vec3.atCenterOf(candidate), ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY);
        BlockHitResult hit = level.clip(context);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos found = hit.getBlockPos();
        if (found.distSqr(origin) > (double) range * range || WardHandler.isWarded(level, found)) {
            return null;
        }
        BlockState state = level.getBlockState(found);
        float hardness = state.getDestroySpeed(level, found);
        return state.isAir() || hardness < 0.0F || hardness >= hardnessCap ? null : found;
    }

    private static int spread(RandomSource random, int range) {
        return random.nextInt(range) - random.nextInt(range);
    }

    private static boolean isPullable(Entity entity) {
        return !entity.isRemoved() && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    private void refresh(ServerLevel level, BlockPos pos, double range) {
        pulled.clear();
        pulled.addAll(level.getEntities((Entity) null, new AABB(pos).inflate(range), NodeHunger::isPullable));
    }

    private void pullAndDevour(ServerLevel level, BlockPos pos, double itemRange) {
        Vec3 center = Vec3.atCenterOf(pos);
        DamageSource source = level.damageSources().fellOutOfWorld();
        for (int i = pulled.size() - 1; i >= 0; i--) {
            Entity entity = pulled.get(i);
            if (!isPullable(entity)) {
                if (entity.isRemoved()) {
                    pulled.remove(i);
                }
                continue;
            }
            double dx = center.x - entity.getX();
            double dy = center.y - entity.getY();
            double dz = center.z - entity.getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double strength = 1.0 - distance / (entity instanceof ItemEntity ? itemRange : GLOBAL_PULL_RANGE);
            if (strength <= 0.0) {
                continue;
            }
            if (distance > MINIMUM_PULL_DISTANCE) {
                double weight = strength * strength;
                entity.setDeltaMovement(entity.getDeltaMovement().add(dx / distance * weight * HORIZONTAL_PULL, dy / distance * weight * VERTICAL_PULL, dz / distance * weight * HORIZONTAL_PULL));
                if (entity instanceof Player) {
                    entity.hurtMarked = true;
                }
            }
            if (distance < DEVOUR_DISTANCE && entity.hurtServer(level, source, DEVOUR_DAMAGE)) {
                if (entity instanceof ItemEntity item && item.isRemoved()) {
                    devour(level, AspectIndexAccess.of(item.getItem().copyWithCount(1)));
                } else if (entity instanceof LivingEntity living && living.isDeadOrDying()) {
                    devour(level, EntityAspects.of(living));
                }
            }
        }
    }

    private void devour(ServerLevel level, AspectList victim) {
        Map<ResourceKey<IAspect>, Integer> primals = WandChargingEvents.reduceToPrimals(victim);
        if (primals.isEmpty()) {
            return;
        }
        RandomSource random = level.getRandom();
        int index = random.nextInt(primals.size());
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : primals.entrySet()) {
            if (index-- > 0) {
                continue;
            }
            Holder<IAspect> primal = Aspects.resolve(level, entry.getKey());
            if (primal == null) {
                return;
            }
            int base = node.aspectsBase.amountOf(primal);
            if (node.held.amountOf(primal) < base) {
                node.held = node.held.add(primal, 1);
            } else if (random.nextFloat() < Math.min(1.0F, entry.getValue() / (1.0F + BASE_GROWTH_FACTOR * base))) {
                node.aspectsBase = node.aspectsBase.add(primal, 1);
            }
            node.invalidateRefill();
            return;
        }
    }
}
