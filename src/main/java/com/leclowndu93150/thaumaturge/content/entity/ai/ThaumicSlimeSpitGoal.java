package com.leclowndu93150.thaumaturge.content.entity.ai;

import com.leclowndu93150.thaumaturge.content.entity.ThaumicSlime;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class ThaumicSlimeSpitGoal extends Goal {
    private static final double PLAYER_RANGE = 16.0;
    private static final int INITIAL_COOLDOWN = 100;
    private static final int RESET_COOLDOWN = 101;
    private static final int CHILD_SIZE = 1;
    private static final double LAUNCH_SPEED = 1.5;
    private static final double SPREAD = 0.0075;
    private static final double ARC_PER_BLOCK = 0.2;
    private static final double OVERHEAD_EPSILON = 1.0E-4;
    private static final float TORSO_FRACTION = 0.5F;
    private static final float GORE_VOLUME = 1.0F;
    private static final float GORE_PITCH_FACTOR = 0.8F;
    private static final float GORE_PITCH_SPREAD = 0.2F;

    private final ThaumicSlime slime;
    private int cooldown = INITIAL_COOLDOWN;
    private @Nullable Player victim;

    public ThaumicSlimeSpitGoal(ThaumicSlime slime) {
        this.slime = slime;
    }

    @Override
    public boolean canUse() {
        Player player = slime.level().getNearestPlayer(slime, PLAYER_RANGE);
        if (player == null) {
            return false;
        }
        if (cooldown > 0) {
            cooldown--;
        }
        if (cooldown > 0 || !slime.canSpitAt(player)) {
            return false;
        }
        victim = player;
        return true;
    }

    @Override
    public void start() {
        cooldown = RESET_COOLDOWN;
        if (victim == null || !(slime.level() instanceof ServerLevel level)) {
            return;
        }
        ThaumicSlime child = TTEntities.THAUMIC_SLIME.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (child == null) {
            return;
        }
        RandomSource random = slime.getRandom();
        child.setSize(CHILD_SIZE, true);
        child.setPos(slime.getX(), slime.getY() + slime.getBbHeight() / 2.0, slime.getZ());
        launch(child, victim, random);
        level.addFreshEntity(child);
        slime.playSound(TTSounds.GORE.get(), GORE_VOLUME, GORE_PITCH_FACTOR * (1.0F + (random.nextFloat() - random.nextFloat()) * GORE_PITCH_SPREAD));
        slime.setSize(slime.getSize() - 1, true);
        victim = null;
    }

    private void launch(ThaumicSlime child, Player target, RandomSource random) {
        double dx = target.getX() - child.getX();
        double dy = target.getY(TORSO_FRACTION) - child.getY();
        double dz = target.getZ() - child.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        if (horizontal < OVERHEAD_EPSILON) {
            return;
        }
        Vec3 velocity = new Vec3(dx, dy + horizontal * ARC_PER_BLOCK, dz).normalize().add(random.nextGaussian() * SPREAD, random.nextGaussian() * SPREAD, random.nextGaussian() * SPREAD)
                .scale(LAUNCH_SPEED);
        float yaw = (float) (Mth.atan2(-velocity.x, velocity.z) * Mth.RAD_TO_DEG);
        child.setDeltaMovement(velocity);
        child.setYRot(yaw);
        child.yRotO = yaw;
        child.yHeadRot = yaw;
        child.yBodyRot = yaw;
    }
}
