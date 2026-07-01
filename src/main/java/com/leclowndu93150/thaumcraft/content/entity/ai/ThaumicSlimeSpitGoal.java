package com.leclowndu93150.thaumcraft.content.entity.ai;

import com.leclowndu93150.thaumcraft.content.entity.ThaumicSlime;
import com.leclowndu93150.thaumcraft.registry.TCEntities;
import com.leclowndu93150.thaumcraft.registry.TCSounds;
import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class ThaumicSlimeSpitGoal extends Goal {
    private static final int COOLDOWN_TICKS = 100;
    private static final double MIN_RANGE_SQ = 16.0;
    private static final double MAX_RANGE_SQ = 256.0;
    private static final int MIN_SPIT_SIZE = 3;
    private static final float CHILD_SPEED = 0.45F;

    private final ThaumicSlime slime;
    private int cooldown;

    public ThaumicSlimeSpitGoal(ThaumicSlime slime) {
        this.slime = slime;
        this.setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (slime.getSize() < MIN_SPIT_SIZE) {
            return false;
        }
        Player nearest = slime.level().getNearestPlayer(slime, Math.sqrt(MAX_RANGE_SQ));
        if (nearest == null) {
            return false;
        }
        double distSq = slime.distanceToSqr(nearest);
        return distSq >= MIN_RANGE_SQ && distSq <= MAX_RANGE_SQ;
    }

    @Override
    public void start() {
        cooldown = COOLDOWN_TICKS;
        if (!(slime.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Player target = serverLevel.getNearestPlayer(slime, Math.sqrt(MAX_RANGE_SQ));
        if (target == null) {
            return;
        }
        ThaumicSlime child = TCEntities.THAUMIC_SLIME.get().create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
        if (child == null) {
            return;
        }
        child.setSize(1, true);
        child.setPos(slime.getX(), slime.getY() + 0.5, slime.getZ());
        Vec3 toTarget = target.position().subtract(slime.position()).normalize();
        child.setDeltaMovement(toTarget.x * CHILD_SPEED, 0.35, toTarget.z * CHILD_SPEED);
        child.markLaunched();
        serverLevel.addFreshEntity(child);
        serverLevel.playSound(null, slime.blockPosition(), TCSounds.GORE.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
        slime.setSize(Math.max(1, slime.getSize() - 1), true);
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }
}
