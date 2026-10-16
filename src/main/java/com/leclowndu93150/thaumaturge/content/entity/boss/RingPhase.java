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
    private static final int SPOKE_BUDGET_BASE = 120;
    private static final int SPOKE_BUDGET_STEP = 10;
    private static final int SPOKE_STRIDE_DIVISOR = 8;
    private static final int HALF_TURN_DEGREES = 180;
    private static final int FIELD_LIFE_MIN = 250;
    private static final int FIELD_LIFE_SPREAD = 150;
    private static final float BOLT_CHANCE = 0.3F;
    private static final int BOLT_COLOR = 0xBB44BB;
    private static final double BOLT_SOURCE_HEIGHT = 0.5;
    private static final float ZAP_VOLUME = 1.0F;
    private static final float ZAP_PITCH_BASE = 0.9F;
    private static final float ZAP_PITCH_SPREAD = 0.1F;
    private static final int LANDING_ATTEMPTS = 20;
    private static final double LANDING_REACH = 7.0;
    private static final double BLOCK_CENTRE = 0.5;
    private static final double FLOOR_PROBE = 0.5;
    private static final int TRAIL_POINTS = 128;
    private static final double TRAIL_WIDTH_FACTOR = 2.0;
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
        if (!warden.isSilent()) {
            level.playSound(null, warden.getX(), warden.getY(), warden.getZ(), TTSounds.ZAP.get(), SoundSource.HOSTILE, ZAP_VOLUME, ZAP_PITCH_BASE + random.nextFloat() * ZAP_PITCH_SPREAD);
        }
        double radius = FIRST_RING_RADIUS + RING_GROWTH * ring;
        int budget = SPOKE_BUDGET_BASE - SPOKE_BUDGET_STEP * ring;
        int stride = 1 + budget / SPOKE_STRIDE_DIVISOR;
        int spokes = HALF_TURN_DEGREES / stride;
        int stepDegrees = 2 * stride;
        int centreX = Mth.floor(warden.getX());
        int centreZ = Mth.floor(warden.getZ());
        int height = Mth.floor(warden.getY());
        Vec3 source = warden.position().add(0.0, warden.getBbHeight() * BOLT_SOURCE_HEIGHT, 0.0);
        for (int spoke = 0; spoke < spokes; spoke++) {
            float bearing = spoke * stepDegrees * Mth.DEG_TO_RAD;
            BlockPos spot = BlockPos.containing(centreX + Mth.cos(bearing) * radius, height, centreZ + Mth.sin(bearing) * radius);
            if (plantField(level, spot) && random.nextFloat() < BOLT_CHANCE) {
                Effects.boltStrike(level, source).to(Vec3.atCenterOf(spot)).color(BOLT_COLOR).send();
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
        if (!warden.isSilent()) {
            level.playSound(null, to.x, to.y, to.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, TELEPORT_VOLUME, TELEPORT_PITCH);
        }
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
        Vec3 path = to.subtract(from);
        double width = warden.getBbWidth() * TRAIL_WIDTH_FACTOR;
        double height = warden.getBbHeight();
        RandomSource random = warden.getRandom();
        for (int point = 0; point < TRAIL_POINTS; point++) {
            double progress = (double) point / (TRAIL_POINTS - 1);
            double x = from.x + path.x * progress + (random.nextDouble() - BLOCK_CENTRE) * width;
            double z = from.z + path.z * progress + (random.nextDouble() - BLOCK_CENTRE) * width;
            double y = from.y + path.y * progress + random.nextDouble() * height;
            level.sendParticles(ParticleTypes.PORTAL, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
