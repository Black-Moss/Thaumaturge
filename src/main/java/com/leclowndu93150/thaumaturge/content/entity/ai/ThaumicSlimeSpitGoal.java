package com.leclowndu93150.thaumaturge.content.entity.ai;

import com.leclowndu93150.thaumaturge.content.entity.ThaumicSlime;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class ThaumicSlimeSpitGoal extends Goal {
    private static final double DETECTION_RANGE = 16.0;
    private static final int FIRST_COOLDOWN = 100;
    private static final int COOLDOWN = 101;
    private static final int NOT_COUNTING = -1;
    private static final float FACE_TURN = 10.0F;
    private static final int CHILD_SIZE = 1;
    private static final double MID_HEIGHT = 0.5;
    private static final double LEAD_DISTANCE = 1.0;
    private static final double AIM_HEIGHT_FRACTION = 1.0 / 3.0;
    private static final double ARC_PER_BLOCK = 0.2;
    private static final double THROW_SPEED = 1.5;
    private static final double WOBBLE = 0.0075;
    private static final double RADIANS_TO_DEGREES = 180.0 / Math.PI;
    private static final float YAW_OFFSET_DEGREES = 90.0F;
    private static final float GORE_VOLUME = 1.0F;
    private static final float GORE_PITCH_SPREAD = 0.2F;
    private static final float GORE_PITCH_SCALE = 0.8F;

    private final ThaumicSlime slime;
    private int cooldown = FIRST_COOLDOWN;
    private int lastCountedTick = NOT_COUNTING;
    private @Nullable Player mark;

    public ThaumicSlimeSpitGoal(ThaumicSlime slime) {
        this.slime = slime;
    }

    @Override
    public boolean canUse() {
        Player nearest = slime.level().getNearestPlayer(slime.getX(), slime.getY(), slime.getZ(), DETECTION_RANGE, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
        int now = slime.tickCount;
        if (nearest == null) {
            lastCountedTick = NOT_COUNTING;
            return false;
        }
        cooldown -= lastCountedTick == NOT_COUNTING ? 1 : now - lastCountedTick;
        lastCountedTick = now;
        slime.lookAt(nearest, FACE_TURN, FACE_TURN);
        mark = nearest;
        return cooldown <= 0 && slime.canSpitAt(nearest);
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        Player target = mark;
        if (target != null && slime.level() instanceof ServerLevel level) {
            spit(level, target);
        }
        mark = null;
    }

    private void spit(ServerLevel level, Player target) {
        ThaumicSlime child = TTEntities.THAUMIC_SLIME.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (child == null) {
            return;
        }
        RandomSource random = slime.getRandom();
        Vec3 origin = new Vec3(slime.getX(), slime.getY(MID_HEIGHT), slime.getZ());
        Vec3 flat = new Vec3(target.getX() - origin.x, 0.0, target.getZ() - origin.z);
        Vec3 lead = flat.lengthSqr() > 0.0 ? flat.normalize().scale(LEAD_DISTANCE) : Vec3.ZERO;
        Vec3 start = origin.add(lead);
        child.setSize(CHILD_SIZE, true);
        child.snapTo(start.x, start.y, start.z, 0.0F, 0.0F);
        double reach = Math.sqrt(flat.lengthSqr());
        Vec3 aim = new Vec3(target.getX() - origin.x, target.getY() + target.getBbHeight() * AIM_HEIGHT_FRACTION - origin.y + reach * ARC_PER_BLOCK, target.getZ() - origin.z);
        Vec3 heading = aim.normalize().add(random.nextGaussian() * WOBBLE, random.nextGaussian() * WOBBLE, random.nextGaussian() * WOBBLE);
        Vec3 velocity = heading.scale(THROW_SPEED);
        child.setDeltaMovement(velocity);
        faceTravel(child, velocity);
        child.markLaunched();
        level.addFreshEntity(child);
        float pitch = ((random.nextFloat() - random.nextFloat()) * GORE_PITCH_SPREAD + 1.0F) * GORE_PITCH_SCALE;
        level.playSound(null, slime.getX(), slime.getY(), slime.getZ(), TTSounds.GORE.get(), SoundSource.HOSTILE, GORE_VOLUME, pitch);
        slime.setSize(slime.getSize() - 1, true);
        cooldown = COOLDOWN;
    }

    private static void faceTravel(ThaumicSlime child, Vec3 velocity) {
        double horizontal = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        float yaw = Mth.wrapDegrees((float) (Mth.atan2(velocity.z, velocity.x) * RADIANS_TO_DEGREES) - YAW_OFFSET_DEGREES);
        float pitch = Mth.wrapDegrees((float) (-(Mth.atan2(velocity.y, horizontal) * RADIANS_TO_DEGREES)));
        child.setYRot(yaw);
        child.yRotO = yaw;
        child.setXRot(pitch);
        child.xRotO = pitch;
        child.setYBodyRot(yaw);
        child.yBodyRotO = yaw;
        child.setYHeadRot(yaw);
        child.yHeadRotO = yaw;
    }
}
