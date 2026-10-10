package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.spell.CastStyle;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.TTSpellParts;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.api.taint.TaintApi;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.warp.WarpNotices;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class RiftEvents {
    private static final int WISP_WEIGHT = 50;
    private static final int SEED_WEIGHT = 10;
    private static final int PHAGE_WEIGHT = 20;
    private static final int CLOUD_WEIGHT = 20;
    private static final int COLLAPSE_WEIGHT = 1;
    private static final float WISP_CALMING = 5.0F;
    private static final float PHAGE_CALMING = 10.0F;
    private static final float CLOUD_CALMING = 10.0F;
    private static final float NO_CALMING = 0.0F;

    private static final double SCATTER_DEVIATION = 5.0;
    private static final int FLUX_WISP_ONE_IN = 5;
    private static final double BLOCK_CENTRE = 0.5;
    private static final float FULL_TURN_DEGREES = 360.0F;
    private static final double SEED_CROWD_FRACTION = 0.8;
    private static final float SEED_FLUX_SHARE = 0.5F;

    private static final double PHAGE_REACH = 16.0;
    private static final int PHAGE_DURATION = 3000;
    private static final int PHAGE_AMPLIFIER = 2;
    private static final String PHAGE_NOTICE = "warp.thaumaturge.fluxevent.2";

    private static final double CLOUD_REACH = 16.0;
    private static final int CLOUD_MIN_RADIUS = 1;
    private static final int CLOUD_MAX_RADIUS = 3;
    private static final int CLOUD_SHORT_CAP = 30;
    private static final int CLOUD_LONG_CAP = 120;
    private static final int CLOUD_MIN_SECONDS = 5;
    private static final int CLOUD_MAX_SECONDS = 30;
    private static final int FLUX_LOWEST_POWER = 1;
    private static final float CAST_POWER = 1.0F;
    private static final String RADIUS_SETTING = "radius";
    private static final String DURATION_SETTING = "duration";
    private static final String POWER_SETTING = "power";

    private static final List<RiftEvent> EVENTS = List.of(new RiftEvent(WISP_WEIGHT, WISP_CALMING, false, RiftEvents::releaseWisp), new RiftEvent(SEED_WEIGHT, NO_CALMING, true, RiftEvents::sowSeed),
            new RiftEvent(PHAGE_WEIGHT, PHAGE_CALMING, false, RiftEvents::spreadPhage), new RiftEvent(CLOUD_WEIGHT, CLOUD_CALMING, false, RiftEvents::castCloud),
            new RiftEvent(COLLAPSE_WEIGHT, NO_CALMING, false, RiftEvents::collapse));
    private static final int TOTAL_WEIGHT = EVENTS.stream().mapToInt(RiftEvent::weight).sum();

    private RiftEvents() {}

    static void roll(ServerLevel level, EntityFluxRift rift) {
        RiftEvent event = choose(rift.getRandom());
        if (event.blockedNearSeed() && TaintApi.isNearTaintSeed(level, rift.blockPosition())) {
            return;
        }
        if (event.outcome().fire(level, rift) && !rift.isRemoved()) {
            rift.adjustStability(event.calming());
        }
    }

    private static RiftEvent choose(RandomSource random) {
        int ticket = random.nextInt(TOTAL_WEIGHT);
        for (RiftEvent event : EVENTS) {
            ticket -= event.weight();
            if (ticket < 0) {
                return event;
            }
        }
        return EVENTS.getLast();
    }

    private static Vec3 scatter(EntityFluxRift rift) {
        RandomSource random = rift.getRandom();
        return rift.position().add(random.nextGaussian() * SCATTER_DEVIATION, random.nextGaussian() * SCATTER_DEVIATION, random.nextGaussian() * SCATTER_DEVIATION);
    }

    private static boolean releaseWisp(ServerLevel level, EntityFluxRift rift) {
        Vec3 spot = scatter(rift);
        if (!level.hasChunkAt(BlockPos.containing(spot))) {
            return false;
        }
        WispEntity wisp = TTEntities.WISP.get().create(level, EntitySpawnReason.EVENT);
        if (wisp == null) {
            return false;
        }
        wisp.snapTo(spot.x, spot.y, spot.z, rift.getRandom().nextFloat() * FULL_TURN_DEGREES, 0.0F);
        if (!wisp.checkSpawnRules(level, EntitySpawnReason.EVENT) || !level.noCollision(wisp)) {
            wisp.discard();
            return false;
        }
        if (rift.getRandom().nextInt(FLUX_WISP_ONE_IN) == 0) {
            wisp.setAspect(TTAspects.VITIUM.identifier());
        }
        return level.addFreshEntity(wisp);
    }

    private static boolean sowSeed(ServerLevel level, EntityFluxRift rift) {
        BlockPos cell = BlockPos.containing(scatter(rift));
        if (level.getDifficulty() == Difficulty.PEACEFUL || !level.hasChunkAt(cell)) {
            return false;
        }
        EntityTaintSeedPrime seed = TTEntities.TAINT_SEED_PRIME.get().create(level, EntitySpawnReason.EVENT);
        if (seed == null) {
            return false;
        }
        seed.snapTo(cell.getX() + BLOCK_CENTRE, cell.getY(), cell.getZ() + BLOCK_CENTRE, rift.getRandom().nextFloat() * FULL_TURN_DEGREES, 0.0F);
        if (!seedFits(level, rift, seed) || !level.addFreshEntity(seed)) {
            seed.discard();
            return false;
        }
        int size = rift.currentSize();
        seed.grantHeadStart(size);
        AuraHelper.polluteAura(level, rift.blockPosition(), size * SEED_FLUX_SHARE, true);
        rift.discard();
        return true;
    }

    private static boolean seedFits(ServerLevel level, EntityFluxRift rift, EntityTaintSeedPrime seed) {
        AABB body = seed.getBoundingBox();
        if (level.containsAnyLiquid(body) || !level.noBlockCollision(seed, body)) {
            return false;
        }
        if (!level.getEntities(seed, body, other -> other != rift).isEmpty()) {
            return false;
        }
        AABB crowd = body.inflate(TaintHelper.spreadArea() * SEED_CROWD_FRACTION);
        return level.getEntitiesOfClass(AbstractTaintSeed.class, crowd).isEmpty();
    }

    private static boolean spreadPhage(ServerLevel level, EntityFluxRift rift) {
        List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, rift.getBoundingBox().inflate(PHAGE_REACH), LivingEntity::isAlive);
        for (LivingEntity victim : victims) {
            victim.addEffect(new MobEffectInstance(TTMobEffects.INFECTIOUS_VIS_EXHAUST, PHAGE_DURATION, PHAGE_AMPLIFIER));
            if (victim instanceof ServerPlayer player) {
                WarpNotices.send(player, PHAGE_NOTICE);
            }
        }
        return !victims.isEmpty();
    }

    private static boolean castCloud(ServerLevel level, EntityFluxRift rift) {
        Player player = level.getNearestPlayer(rift, CLOUD_REACH);
        if (player == null) {
            return false;
        }
        RandomSource random = rift.getRandom();
        int size = rift.currentSize();
        int radius = Mth.nextInt(random, CLOUD_MIN_RADIUS, CLOUD_MAX_RADIUS);
        int shortest = Math.min(size / 2, CLOUD_SHORT_CAP);
        int longest = Math.min(size, CLOUD_LONG_CAP);
        int seconds = Mth.clamp(Mth.nextInt(random, shortest, Math.max(shortest, longest)), CLOUD_MIN_SECONDS, CLOUD_MAX_SECONDS);
        SpellNode effect = SpellNode.of(TTSpellParts.FLUX).withSetting(POWER_SETTING, FLUX_LOWEST_POWER);
        SpellNode cloud = SpellNode.of(TTSpellParts.CLOUD).withSetting(RADIUS_SETTING, radius).withSetting(DURATION_SETTING, seconds).then(effect);
        Spell spell = new Spell(CastStyle.INSTANT, SpellNode.of(Spell.ORIGIN).then(cloud));
        Spells.cast(player, ItemStack.EMPTY, spell, List.of(SpellTarget.origin(player)), CAST_POWER);
        return true;
    }

    private static boolean collapse(ServerLevel level, EntityFluxRift rift) {
        if (rift.isCollapsing()) {
            return false;
        }
        rift.beginCollapse();
        return true;
    }

    @FunctionalInterface
    private interface Outcome {
        boolean fire(ServerLevel level, EntityFluxRift rift);
    }

    private record RiftEvent(int weight, float calming, boolean blockedNearSeed, Outcome outcome) {
    }
}
