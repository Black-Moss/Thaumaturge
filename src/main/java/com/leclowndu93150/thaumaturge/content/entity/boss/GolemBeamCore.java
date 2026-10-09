package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

final class GolemBeamCore {
    private static final int MAX_CHARGE = 150;
    private static final int SHOT_COST = 15;
    private static final int SHOT_COST_SPREAD = 5;
    private static final int PULSE_INTERVAL = 5;
    private static final int ARC_SEGMENTS = 8;
    private static final int ARC_SEGMENT_SPREAD = 5;
    private static final float SPOT_MIN_DISTANCE = 2.0F;
    private static final float SPOT_DISTANCE_SPREAD = 2.0F;
    private static final int SPOT_SCAN_UP = 2;
    private static final int SPOT_SCAN_DOWN = 3;
    private static final int ARC_COLOR = 0xA6FFFF;
    private static final float HUM_VOLUME = 0.8F;
    private static final float HUM_PITCH_VARIATION = 0.05F;
    private static final double BLOCK_CENTER = 0.5;

    private final Mob golem;
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    private int charge;
    private boolean recharging;
    private int pulseClock;
    private int segmentsLeft;
    private @Nullable Vec3 spot;

    GolemBeamCore(Mob golem) {
        this.golem = golem;
    }

    boolean ready() {
        return !recharging && charge > 0;
    }

    void spend(RandomSource random) {
        charge -= SHOT_COST + random.nextInt(SHOT_COST_SPREAD);
    }

    void tick(ServerLevel level) {
        if (!recharging) {
            if (charge <= 0) {
                recharging = true;
                pulseClock = 0;
                segmentsLeft = 0;
            }
            return;
        }
        charge++;
        if (charge >= MAX_CHARGE) {
            charge = MAX_CHARGE;
            recharging = false;
            spot = null;
            return;
        }
        if (++pulseClock >= PULSE_INTERVAL) {
            pulseClock = 0;
            pulse(level);
        }
    }

    private void pulse(ServerLevel level) {
        if (spot == null || segmentsLeft <= 0) {
            spot = findSpot(level);
            if (spot == null) {
                return;
            }
            segmentsLeft = ARC_SEGMENTS + golem.getRandom().nextInt(ARC_SEGMENT_SPREAD);
            level.playSound(null, golem.getX(), golem.getY(), golem.getZ(), TTSounds.JACOBS.get(), SoundSource.HOSTILE, HUM_VOLUME,
                    1.0F + (golem.getRandom().nextFloat() * 2 - 1) * HUM_PITCH_VARIATION);
        }
        Effects.zapArc(level, golem.getBoundingBox().getCenter()).to(spot).color(ARC_COLOR).send();
        segmentsLeft--;
    }

    private @Nullable Vec3 findSpot(ServerLevel level) {
        RandomSource random = golem.getRandom();
        float distance = SPOT_MIN_DISTANCE + random.nextFloat() * SPOT_DISTANCE_SPREAD;
        float angle = random.nextFloat() * Mth.TWO_PI;
        int x = Mth.floor(golem.getX() + Mth.cos(angle) * distance);
        int z = Mth.floor(golem.getZ() + Mth.sin(angle) * distance);
        int top = golem.blockPosition().getY() + SPOT_SCAN_UP;
        for (int y = top; y >= top - SPOT_SCAN_UP - SPOT_SCAN_DOWN; y--) {
            cursor.set(x, y, z);
            if (!level.hasChunkAt(cursor)) {
                return null;
            }
            BlockState state = level.getBlockState(cursor);
            if (!state.getCollisionShape(level, cursor).isEmpty()) {
                cursor.set(x, y + 1, z);
                return level.getBlockState(cursor).getCollisionShape(level, cursor).isEmpty() ? new Vec3(x + BLOCK_CENTER, y + 1.0, z + BLOCK_CENTER) : null;
            }
        }
        return null;
    }
}
