package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealArea;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.AgeableWaterCreature;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import org.jspecify.annotations.Nullable;

public final class ButcherBehavior extends HuntBehavior {
    private static final int SCAN_PERIOD = 200;
    private static final int MIN_HERD = 3;

    private final SealClock clock = new SealClock(SCAN_PERIOD);
    private boolean hunting;

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        boolean due = clock.advance() % SCAN_PERIOD == 0;
        if (hunting || !due) {
            return;
        }
        List<LivingEntity> stock = level.getEntitiesOfClass(LivingEntity.class, SealArea.bounds(seal), ButcherBehavior::isLivestock);
        LivingEntity chosen = firstInHerd(stock);
        if (chosen != null) {
            mark(level, seal, chosen);
            hunting = true;
        }
    }

    private static @Nullable LivingEntity firstInHerd(List<LivingEntity> stock) {
        for (LivingEntity animal : stock) {
            if (herdSize(stock, animal) >= MIN_HERD) {
                return animal;
            }
        }
        return null;
    }

    private static int herdSize(List<LivingEntity> stock, LivingEntity member) {
        Class<?> kind = member.getClass();
        int size = 0;
        for (LivingEntity other : stock) {
            size += kind.isInstance(other) ? 1 : 0;
        }
        return size;
    }

    private static boolean isLivestock(LivingEntity entity) {
        if (entity.isBaby() || entity instanceof Enemy || entity instanceof IGolemAPI) {
            return false;
        }
        if (entity instanceof OwnableEntity pet && pet.getOwnerReference() != null) {
            return false;
        }
        return entity instanceof Animal || entity instanceof WaterAnimal || entity instanceof AgeableWaterCreature;
    }

    @Override
    protected boolean isQuarry(ServerLevel level, ISealEntity seal, LivingEntity target) {
        return isLivestock(target);
    }

    @Override
    protected void onHuntOver() {
        hunting = false;
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        return true;
    }
}
