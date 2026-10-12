package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.api.recipe.DustTrigger;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerPlacement;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class DustTriggerFx {
    public static final double SPARKLE_TICKS_PER_BLOCK = Effects.SHELL_TICKS_PER_BLOCK;

    private static final double HAND_HEIGHT = 0.25;
    private static final double HAND_SIDE_OFFSET = 0.3;
    private static final double HAND_LOOK_OFFSET = 0.3;

    private static final int BURST_COUNT = 50;
    private static final int BURST_FLOATY_COUNT = 16;
    private static final double BURST_VELOCITY_DIVISOR = 6.0;
    private static final double BURST_SCATTER = 0.05;
    private static final double BURST_FLOATY_LIFT = 0.05;
    private static final double BURST_ARC_LIFT = 0.15;
    private static final float BURST_FLOATY_DECAY_MIN = 0.3F;
    private static final float BURST_FLOATY_DECAY_SPAN = 0.5F;
    private static final float BURST_ARC_DECAY = 0.85F;
    private static final float BURST_FLOATY_GRAVITY = 0.2F;
    private static final float BURST_ARC_GRAVITY = 0.5F;
    private static final float BURST_SCALE = 0.5F;
    private static final int BURST_BASE_AGE = 16;
    private static final int BURST_DELAY_BOUND = 5;

    private DustTriggerFx() {}

    public static Vec3 posToHand(ServerPlayer player, InteractionHand hand) {
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        float bodyYaw = player.yBodyRot * Mth.DEG_TO_RAD;
        double sideSign = arm == HumanoidArm.RIGHT ? 1.0 : -1.0;
        double rightX = -Mth.cos(bodyYaw);
        double rightZ = -Mth.sin(bodyYaw);
        Vec3 look = player.getLookAngle();
        double x = player.getX() + rightX * HAND_SIDE_OFFSET * sideSign + look.x * HAND_LOOK_OFFSET;
        double y = player.getY() + player.getBbHeight() / 2.0 + HAND_HEIGHT + look.y * HAND_LOOK_OFFSET;
        double z = player.getZ() + rightZ * HAND_SIDE_OFFSET * sideSign + look.z * HAND_LOOK_OFFSET;
        return new Vec3(x, y, z);
    }

    public static void emitUseBurst(ServerLevel level, ServerPlayer player, InteractionHand hand, BlockPos pos) {
        RandomSource random = level.getRandom();
        Vec3 handPos = posToHand(player, hand);
        Vec3 toBlock = Vec3.atCenterOf(pos).subtract(handPos);
        Vec3 drift = toBlock.scale(1.0 / BURST_VELOCITY_DIVISOR);
        for (int i = 0; i < BURST_COUNT; i++) {
            boolean floaty = i < BURST_FLOATY_COUNT;
            float decay = floaty ? BURST_FLOATY_DECAY_MIN + random.nextFloat() * BURST_FLOATY_DECAY_SPAN : BURST_ARC_DECAY;
            double lift = floaty ? BURST_FLOATY_LIFT : BURST_ARC_LIFT;
            float gravity = floaty ? BURST_FLOATY_GRAVITY : BURST_ARC_GRAVITY;
            Vec3 velocity = drift.add(random.nextGaussian() * BURST_SCATTER, lift + random.nextGaussian() * BURST_SCATTER, random.nextGaussian() * BURST_SCATTER);
            int delay = random.nextInt(BURST_DELAY_BOUND);
            SparkleParticleOptions options = new SparkleParticleOptions(Effects.dustSparkleColor(random), BURST_SCALE, delay, decay, gravity, BURST_BASE_AGE, true);
            Effects.spawn(level, options, handPos.x, handPos.y, handPos.z, velocity.x, velocity.y, velocity.z);
        }
    }

    public static void emitBlockSparkles(ServerLevel level, BlockPos pos, Vec3 source) {
        Effects.blockSparkles(level, pos).from(source).send();
    }

    public static void emitTriggerSparkles(ServerLevel level, ServerPlayer player, BlockPos clicked, DustTrigger trigger, Vec3 start, @Nullable DustTriggerPlacement placement) {
        DustTriggerPlacement resolved = placement != null ? placement : DustTriggerPlacement.origin();
        List<BlockPos> positions = trigger.sparkle(level, player, clicked, resolved);
        if (positions == null || positions.isEmpty()) {
            return;
        }
        for (BlockPos position : positions) {
            emitBlockSparkles(level, position, start);
        }
    }
}
