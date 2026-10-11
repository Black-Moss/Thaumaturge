package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.eldritch.CastingArms;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import org.jspecify.annotations.Nullable;

final class RingPhase {
    private static final int DURATION = 150;
    private static final int FIRST_RING_TICK = 30;
    private static final int RING_INTERVAL = 10;
    private static final int RING_COUNT = 12;
    private static final double FIRST_RING_RADIUS = 3.75;
    private static final double RING_GROWTH = 1.25;
    private static final double POINT_SPACING = 2.0;
    private static final int FIELD_LIFE_MIN = 250;
    private static final int FIELD_LIFE_SPREAD = 151;
    private static final float ARC_CHANCE = 0.3F;
    private static final int ARC_COLOR = 0xA050E8;
    private static final double ARC_SOURCE_HEIGHT = 0.6;
    private static final double ARC_TARGET_LIFT = 0.1;
    private static final float ZAP_VOLUME = 2.0F;
    private static final float ZAP_PITCH_BASE = 0.8F;
    private static final float ZAP_PITCH_SPREAD = 0.3F;
    private static final int LANDING_ATTEMPTS = 20;
    private static final double LANDING_REACH = 7.0;
    private static final double BLOCK_CENTRE = 0.5;
    private static final double FLOOR_PROBE = 0.5;
    private static final double TRAIL_POINTS_PER_BLOCK = 2.0;
    private static final int TRAIL_MIN_POINTS = 8;
    private static final double TRAIL_SPREAD = 0.2;
    private static final double TRAIL_SPEED = 0.1;
    private static final float TELEPORT_VOLUME = 1.0F;
    private static final float TELEPORT_PITCH = 1.0F;

    private final EntityEldritchWarden warden;
    private final CastingArms arms;
    private int remaining;

    RingPhase(EntityEldritchWarden warden, CastingArms arms) {
        this.warden = warden;
        this.arms = arms;
    }

    boolean active() {
        return remaining > 0;
    }

    void begin(ServerLevel level) {
        remaining = DURATION;
        recall(level);
    }

    void tick(ServerLevel level) {
        if (remaining <= 0) {
            return;
        }
        int elapsed = DURATION - remaining;
        remaining--;
        int sinceFirst = elapsed - FIRST_RING_TICK;
        if (sinceFirst < 0 || sinceFirst % RING_INTERVAL != 0) {
            return;
        }
        int ring = sinceFirst / RING_INTERVAL;
        if (ring < RING_COUNT) {
            sendRing(level, ring);
        }
    }

    private void sendRing(ServerLevel level, int ring) {
        RandomSource random = warden.getRandom();
        arms.raiseBoth();
        level.playSound(null, warden.getX(), warden.getY(), warden.getZ(), TTSounds.ZAP.get(), SoundSource.HOSTILE, ZAP_VOLUME, ZAP_PITCH_BASE + random.nextFloat() * ZAP_PITCH_SPREAD);
        double radius = FIRST_RING_RADIUS + RING_GROWTH * ring;
        int points = Mth.ceil(Mth.TWO_PI * radius / POINT_SPACING);
        double step = Mth.TWO_PI / points;
        double offset = random.nextDouble() * step;
        for (int point = 0; point < points; point++) {
            double bearing = offset + step * point;
            BlockPos spot = BlockPos.containing(warden.getX() + Math.cos(bearing) * radius, warden.getY(), warden.getZ() + Math.sin(bearing) * radius);
            if (plantField(level, spot) && random.nextFloat() < ARC_CHANCE) {
                Vec3 source = warden.position().add(0.0, warden.getBbHeight() * ARC_SOURCE_HEIGHT, 0.0);
                Effects.zapArc(level, source).to(Vec3.atBottomCenterOf(spot).add(0.0, ARC_TARGET_LIFT, 0.0)).color(ARC_COLOR).send();
            }
        }
    }

    private boolean plantField(ServerLevel level, BlockPos spot) {
        BlockPos floor = spot.below();
        if (!level.isEmptyBlock(spot) || !level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) {
            return false;
        }
        Block field = TTBlocks.EFFECT_SAP.get();
        if (!level.setBlockAndUpdate(spot, field.defaultBlockState())) {
            return false;
        }
        level.scheduleTick(spot, field, FIELD_LIFE_MIN + warden.getRandom().nextInt(FIELD_LIFE_SPREAD));
        return true;
    }

    private void recall(ServerLevel level) {
        BlockPos home = warden.hasHome() ? warden.getHomePosition() : warden.blockPosition();
        Vec3 landing = findLanding(level, home);
        if (landing == null) {
            return;
        }
        EntityTeleportEvent.EnderEntity event = EventHooks.onEnderTeleport(warden, landing.x, landing.y, landing.z);
        if (event.isCanceled()) {
            return;
        }
        Vec3 from = warden.position();
        warden.teleportTo(event.getTargetX(), event.getTargetY(), event.getTargetZ());
        warden.getNavigation().stop();
        Vec3 to = warden.position();
        drawTrail(level, from, to);
        level.playSound(null, from.x, from.y, from.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, TELEPORT_VOLUME, TELEPORT_PITCH);
        level.playSound(null, to.x, to.y, to.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, TELEPORT_VOLUME, TELEPORT_PITCH);
    }

    private @Nullable Vec3 findLanding(ServerLevel level, BlockPos home) {
        Vec3 centre = Vec3.atBottomCenterOf(home);
        if (fits(level, centre)) {
            return centre;
        }
        RandomSource random = warden.getRandom();
        for (int attempt = 0; attempt < LANDING_ATTEMPTS; attempt++) {
            double x = Mth.floor(centre.x + (random.nextDouble() * 2.0 - 1.0) * LANDING_REACH) + BLOCK_CENTRE;
            double z = Mth.floor(centre.z + (random.nextDouble() * 2.0 - 1.0) * LANDING_REACH) + BLOCK_CENTRE;
            Vec3 candidate = new Vec3(x, centre.y, z);
            if (fits(level, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean fits(ServerLevel level, Vec3 feet) {
        BlockPos floor = BlockPos.containing(feet.x, feet.y - FLOOR_PROBE, feet.z);
        if (!level.hasChunkAt(floor) || !level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) {
            return false;
        }
        AABB body = warden.getDimensions(warden.getPose()).makeBoundingBox(feet);
        return level.noCollision(warden, body) && !level.containsAnyLiquid(body);
    }

    private void drawTrail(ServerLevel level, Vec3 from, Vec3 to) {
        int points = Math.max(TRAIL_MIN_POINTS, Mth.ceil(from.distanceTo(to) * TRAIL_POINTS_PER_BLOCK));
        RandomSource random = warden.getRandom();
        for (int point = 0; point <= points; point++) {
            Vec3 at = from.lerp(to, (double) point / points);
            double y = at.y + random.nextDouble() * warden.getBbHeight();
            level.sendParticles(ParticleTypes.PORTAL, at.x, y, at.z, 1, TRAIL_SPREAD, TRAIL_SPREAD, TRAIL_SPREAD, TRAIL_SPEED);
        }
    }
}
