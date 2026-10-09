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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

final class RingPhase {
    private static final int PHASE_TICKS = 150;
    private static final int FIRST_BURST = 120;
    private static final int LAST_BURST = 10;
    private static final int BURST_STEP = 10;
    private static final int[] SPOKE_DEGREES = {32, 28, 26, 24, 22, 18, 16, 14, 12, 8, 6, 4};
    private static final int FULL_CIRCLE_DEGREES = 360;
    private static final float RADIUS_DIVISOR = 8.0F;
    private static final int SCAN_UP = 2;
    private static final int SCAN_DOWN = 3;
    private static final float BOLT_CHANCE = 0.3F;
    private static final int BOLT_COLOR = 0xBB44BB;
    private static final int SAP_DECAY_BASE = 250;
    private static final int SAP_DECAY_SPREAD = 150;
    private static final float ZAP_VOLUME = 1.0F;
    private static final float ZAP_PITCH = 0.9F;
    private static final float ZAP_PITCH_SPREAD = 0.1F;
    private static final int RECALL_ATTEMPTS = 24;
    private static final int RECALL_RADIUS = 6;
    private static final int RECALL_SCAN = 4;
    private static final int PORTAL_PARTICLES = 128;
    private static final double PORTAL_WIDTH_SCALE = 2.0;
    private static final double PORTAL_DRIFT = 0.2;
    private static final float RECALL_VOLUME = 1.0F;
    private static final float RECALL_PITCH = 1.0F;
    private static final double CENTER = 0.5;

    private final Mob caster;
    private final CastingArms arms;
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    private int remaining;
    private boolean spent;

    RingPhase(Mob caster, CastingArms arms) {
        this.caster = caster;
        this.arms = arms;
    }

    boolean active() {
        return remaining > 0;
    }

    void ignite() {
        if (spent || !(caster.level() instanceof ServerLevel level)) {
            return;
        }
        spent = true;
        remaining = PHASE_TICKS;
        recall(level);
    }

    void tick(ServerLevel level) {
        if (remaining <= 0) {
            return;
        }
        remaining--;
        if (remaining <= FIRST_BURST && remaining >= LAST_BURST && remaining % BURST_STEP == 0) {
            burst(level, remaining);
        }
    }

    private void burst(ServerLevel level, int left) {
        arms.raiseBoth();
        RandomSource random = caster.getRandom();
        int degrees = SPOKE_DEGREES[(FIRST_BURST - left) / BURST_STEP];
        int spokes = FULL_CIRCLE_DEGREES / degrees;
        float radius = (PHASE_TICKS - left) / RADIUS_DIVISOR;
        BlockPos center = caster.blockPosition();
        Vec3 origin = caster.getBoundingBox().getCenter();
        for (int spoke = 0; spoke < spokes; spoke++) {
            double angle = Math.toRadians(spoke * degrees);
            int x = center.getX() + Mth.floor(Math.cos(angle) * radius);
            int z = center.getZ() + Mth.floor(Math.sin(angle) * radius);
            if (laySap(level, x, center.getY(), z, random) && random.nextFloat() < BOLT_CHANCE) {
                Effects.boltStrike(level, origin).to(Vec3.atCenterOf(cursor)).color(BOLT_COLOR).send();
            }
        }
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), TTSounds.ZAP.get(), SoundSource.HOSTILE, ZAP_VOLUME, ZAP_PITCH + random.nextFloat() * ZAP_PITCH_SPREAD);
    }

    private boolean laySap(ServerLevel level, int x, int y, int z, RandomSource random) {
        for (int scan = y + SCAN_UP; scan >= y - SCAN_DOWN; scan--) {
            cursor.set(x, scan, z);
            if (!level.hasChunkAt(cursor)) {
                return false;
            }
            if (!level.getBlockState(cursor).isAir()) {
                continue;
            }
            cursor.set(x, scan - 1, z);
            if (level.getBlockState(cursor).isSolidRender()) {
                cursor.set(x, scan, z);
                Block sap = TTBlocks.EFFECT_SAP.get();
                level.setBlock(cursor, sap.defaultBlockState(), Block.UPDATE_ALL);
                level.scheduleTick(cursor, sap, SAP_DECAY_BASE + random.nextInt(SAP_DECAY_SPREAD));
                return true;
            }
        }
        return false;
    }

    private void recall(ServerLevel level) {
        BlockPos home = caster.hasHome() ? caster.getHomePosition() : caster.blockPosition();
        RandomSource random = caster.getRandom();
        Vec3 from = caster.position();
        for (int attempt = 0; attempt < RECALL_ATTEMPTS; attempt++) {
            int x = home.getX() + random.nextInt(RECALL_RADIUS * 2 + 1) - RECALL_RADIUS;
            int z = home.getZ() + random.nextInt(RECALL_RADIUS * 2 + 1) - RECALL_RADIUS;
            Vec3 to = standingSpot(level, x, home.getY(), z);
            if (to != null && level.noCollision(caster, caster.getBoundingBox().move(to.subtract(from)))) {
                relocate(level, from, to);
                return;
            }
        }
    }

    private @Nullable Vec3 standingSpot(ServerLevel level, int x, int y, int z) {
        for (int scan = y + RECALL_SCAN; scan >= y - RECALL_SCAN; scan--) {
            cursor.set(x, scan, z);
            if (!level.hasChunkAt(cursor)) {
                return null;
            }
            if (!level.getBlockState(cursor).getCollisionShape(level, cursor).isEmpty()) {
                continue;
            }
            BlockPos floor = cursor.below();
            if (level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) {
                return Vec3.atBottomCenterOf(cursor);
            }
        }
        return null;
    }

    private void relocate(ServerLevel level, Vec3 from, Vec3 to) {
        RandomSource random = caster.getRandom();
        caster.getNavigation().stop();
        caster.teleportTo(to.x, to.y, to.z);
        double spreadX = caster.getBbWidth() * PORTAL_WIDTH_SCALE;
        for (int particle = 0; particle < PORTAL_PARTICLES; particle++) {
            double along = (double) particle / (PORTAL_PARTICLES - 1);
            level.sendParticles(ParticleTypes.PORTAL, Mth.lerp(along, from.x, to.x) + centered(random, spreadX), Mth.lerp(along, from.y, to.y) + random.nextDouble() * caster.getBbHeight(),
                    Mth.lerp(along, from.z, to.z) + centered(random, spreadX), 0, centered(random, PORTAL_DRIFT), centered(random, PORTAL_DRIFT), centered(random, PORTAL_DRIFT), 1.0);
        }
        level.playSound(null, to.x, to.y, to.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, RECALL_VOLUME, RECALL_PITCH);
    }

    private static double centered(RandomSource random, double span) {
        return (random.nextDouble() - CENTER) * span;
    }
}
